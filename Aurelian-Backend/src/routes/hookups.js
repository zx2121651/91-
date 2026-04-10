const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const Joi = require('joi');
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const User = require('../models/User');
const HookupRequest = require('../models/HookupRequest');

const requestSchema = Joi.object({
    targetUserId: Joi.string().required(),
    note: Joi.string().max(200).allow('').default(''),
    safeMode: Joi.boolean().default(true),
    meetingType: Joi.string().valid('DRINK', 'DINNER', 'PARTY', 'CASUAL').required()
});

/**
 * 获取同城活跃的高定约会卡片
 * 需要排除自己，并且剔除掉已经发送过请求、或者相互拒绝过的对象。
 */
router.get('/cards', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const city = req.query.city;
        const intent = req.query.intent;
        const limit = parseInt(req.query.limit) || 10;
        const cursor = req.query.cursor;

        // 1. 查询当前用户已发起或收到的所有活跃请求的目标 ID
        const activeRequests = await HookupRequest.find({
            $or: [{ senderId: req.user.id }, { targetUserId: req.user.id }],
            status: { $in: ['PENDING', 'ACCEPTED'] }
        })
        .select('senderId targetUserId')
        .lean();

        const excludedUserIds = activeRequests.flatMap(req => [req.senderId, req.targetUserId]);
        excludedUserIds.push(req.user.id); // 排除自己

        // 2. 构造聚合查询条件 (基于距离、城市、意图等)
        const query = {
            _id: { $nin: excludedUserIds },
            status: 'ACTIVE' // 必须是已审核会员
        };

        if (city) {
            query.location = { $regex: city, $options: 'i' };
        }

        if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
            query._id = { $lt: cursor };
        }

        // 3. 执行查询
        const hookups = await User.find(query)
            .sort({ _id: -1 })
            .limit(limit)
            .select('name minAgePreference location bio avatarUrl followersCount')
            .lean();

        // 4. 数据映射
        const formattedCards = hookups.map(user => {
            // 随机生成一些展示标签或意图（真实情况下意图应存在于用户的偏好设置里）
            const age = user.minAgePreference || 25;
            const intents = ['DRINK', 'DINNER', 'PARTY', 'CASUAL'];
            const randomIntent = intents[Math.floor(Math.random() * intents.length)];

            return {
                userId: user._id.toString(),
                name: user.name,
                age: age,
                city: user.location,
                bio: user.bio,
                intent: randomIntent,
                tags: ['VIP', `粉丝 \${user.followersCount}`],
                avatarUrl: user.avatarUrl
            };
        });

        res.status(200).json({
            data: formattedCards,
            nextCursor: hookups.length === limit ? hookups[hookups.length - 1]._id.toString() : null,
            meta: { total: hookups.length, city, intent }
        });

    } catch (error) {
        console.error('[HOOKUPS] 获取卡片失败:', error);
        res.status(500).json({ error: '暂时无法雷达搜寻，请稍后再试' });
    }
});

/**
 * 发起速约请求
 * 核心校验：对方不能有正在 PENDING 的对我发起的请求，我也不能重复发送。
 */
router.post('/request', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { error, value } = requestSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const { targetUserId, note, safeMode, meetingType } = value;
        const senderId = req.user.id;

        // 1. 业务校验：是否已有处理中/已接受的邀约
        const existingRequest = await HookupRequest.findOne({
            $or: [
                { senderId, targetUserId, status: { $in: ['PENDING', 'ACCEPTED'] } },
                { senderId: targetUserId, targetUserId: senderId, status: { $in: ['PENDING', 'ACCEPTED'] } }
            ]
        });

        if (existingRequest) {
            return res.status(400).json({ error: '您或对方已经发起过邀约，请先处理当前邀约。' });
        }

        // 2. 插入新邀约
        // 设定过期时间为 24 小时后
        const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000);

        const newRequest = new HookupRequest({
            senderId,
            targetUserId,
            meetingType,
            note,
            safeMode,
            expiresAt
        });

        await newRequest.save();

        res.status(201).json({
            data: {
                requestId: newRequest._id.toString(),
                targetUserId: targetUserId,
                meetingType: meetingType,
                note: note,
                safeMode: safeMode,
                status: newRequest.status,
                createdAt: newRequest.createdAt.getTime()
            },
            message: '您的专属邀约已发送，请静候佳音。'
        });

    } catch (error) {
        console.error('[HOOKUPS] 发送请求失败:', error);
        res.status(500).json({ error: '邀约投递失败，您的管家正在修复' });
    }
});

/**
 * 获取请求状态 (发送后轮询)
 */
router.get('/request/:id', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { id } = req.params;
        const userId = req.user.id;

        const request = await HookupRequest.findOne({
            _id: id,
            $or: [{ senderId: userId }, { targetUserId: userId }]
        }).lean();

        if (!request) {
            return res.status(404).json({ error: '邀约不存在或已被系统清理。' });
        }

        res.status(200).json({
            data: {
                requestId: request._id.toString(),
                targetUserId: request.senderId.toString() === userId ? request.targetUserId.toString() : request.senderId.toString(),
                meetingType: request.meetingType,
                note: request.note,
                safeMode: request.safeMode,
                status: request.status,
                createdAt: request.createdAt.getTime()
            }
        });

    } catch (error) {
        console.error('[HOOKUPS] 查询请求状态失败:', error);
        res.status(500).json({ error: '查询失败' });
    }
});


/**
 * 获取当前用户收到或发起的邀约通知列表
 */
router.get('/requests', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const type = req.query.type || 'RECEIVED'; // 'RECEIVED' 或 'SENT'
        const limit = parseInt(req.query.limit) || 20;

        const query = {};
        if (type === 'RECEIVED') {
            query.targetUserId = req.user.id;
        } else {
            query.senderId = req.user.id;
        }

        const requests = await HookupRequest.find(query)
            .sort({ createdAt: -1 })
            .limit(limit)
            // 根据类型 populate 对方的信息
            .populate(type === 'RECEIVED' ? 'senderId' : 'targetUserId', 'name avatarUrl bio location isVerified')
            .lean();

        // 格式化输出
        const data = requests.map(reqData => {
            const otherUser = type === 'RECEIVED' ? reqData.senderId : reqData.targetUserId;
            return {
                requestId: reqData._id.toString(),
                userId: otherUser._id.toString(),
                name: otherUser.name,
                avatarUrl: otherUser.avatarUrl,
                bio: otherUser.bio,
                location: otherUser.location,
                isVerified: otherUser.isVerified,
                meetingType: reqData.meetingType,
                note: reqData.note,
                safeMode: reqData.safeMode,
                status: reqData.status,
                createdAt: reqData.createdAt.getTime(),
                expiresAt: reqData.expiresAt.getTime()
            };
        });

        res.status(200).json({ data });
    } catch (error) {
        console.error('[HOOKUPS] 获取邀约列表失败:', error);
        res.status(500).json({ error: '无法获取邀约信箱内容' });
    }
});

const respondSchema = Joi.object({
    action: Joi.string().valid('ACCEPT', 'REJECT').required()
});

/**
 * 响应收到的邀约请求
 */
router.post('/requests/:id/respond', verifyToken, requireActiveStatus, async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const { id } = req.params;
        const { error, value } = respondSchema.validate(req.body);
        if (error) {
            await session.abortTransaction();
            return res.status(400).json({ error: error.details[0].message });
        }

        const { action } = value;
        const userId = req.user.id;

        // 1. 查找此请求，必须是发给当前用户的，且处于 PENDING 状态
        const hookupRequest = await HookupRequest.findOne({
            _id: id,
            targetUserId: userId,
            status: 'PENDING'
        }).session(session);

        if (!hookupRequest) {
            await session.abortTransaction();
            return res.status(404).json({ error: '该邀约不存在、已过期或已被处理。' });
        }

        if (action === 'REJECT') {
            hookupRequest.status = 'REJECTED';
            await hookupRequest.save({ session });
            await session.commitTransaction();
            return res.status(200).json({ success: true, message: '您已婉拒了该高定邀约。' });
        }

        // 2. 如果是 ACCEPT，需要创建一段私聊会话 Conversation
        hookupRequest.status = 'ACCEPTED';

        const Conversation = require('../models/Conversation');
        let conversation = await Conversation.findOne({
            participants: { $all: [userId, hookupRequest.senderId.toString()] }
        }).session(session);

        if (!conversation) {
            conversation = new Conversation({
                participants: [userId, hookupRequest.senderId.toString()],
                type: 'DIRECT',
                lastMessageText: '【邀约已接受】期待与您的相遇。'
            });
            await conversation.save({ session });
        }

        hookupRequest.conversationId = conversation._id;
        await hookupRequest.save({ session });

        await session.commitTransaction();

        res.status(200).json({
            success: true,
            message: '您已接受邀约，私密通道已为您开启。',
            data: {
                conversationId: conversation._id.toString()
            }
        });

    } catch (error) {
        await session.abortTransaction();
        console.error('[HOOKUPS] 响应邀约失败:', error);
        res.status(500).json({ error: '系统异常，无法回传您的心意。' });
    } finally {
        session.endSession();
    }
});

module.exports = router;
