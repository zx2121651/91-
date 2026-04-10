const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const Joi = require('joi');
const { verifyToken, requireAdminRole } = require('../middleware/auth.middleware');

// 加载所有的 Mongoose 模型
const VerificationRequest = require('../models/VerificationRequest');
const User = require('../models/User');
const Video = require('../models/Video');
const Event = require('../models/Event');
const MasqueradeSession = require('../models/MasqueradeSession');
const InviteCode = require('../models/InviteCode');

// ==========================================
// 1. 用户资产与身份严审 (Vetting Management)
// ==========================================

/**
 * 获取待审验证列表
 * 支持按状态过滤，默认拉取 PENDING 的记录
 */
router.get('/vetting-requests', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { status = 'PENDING', page = 1, limit = 20 } = req.query;
        const skip = (parseInt(page) - 1) * parseInt(limit);

        const requests = await VerificationRequest.find({ status })
            .sort({ createdAt: 1 }) // 优先处理最早提交的
            .skip(skip)
            .limit(parseInt(limit))
            .populate('userId', 'name email phone status membershipTier')
            .lean();

        const total = await VerificationRequest.countDocuments({ status });

        res.status(200).json({
            data: requests,
            meta: {
                total,
                page: parseInt(page),
                pages: Math.ceil(total / limit)
            }
        });

    } catch (error) {
        console.error('[ADMIN] 查询审核列表失败:', error);
        res.status(500).json({ error: '系统异常，请检查管理员凭证。' });
    }
});

const reviewSchema = Joi.object({
    action: Joi.string().valid('APPROVE', 'REJECT').required(),
    reason: Joi.string().max(200).allow('').optional(),
    // 审批通过时分配的具体会籍（如果适用）
    membershipTier: Joi.string().valid('STANDARD', 'GOLD', 'BLACK').optional()
});

/**
 * 处理单条验证记录的审批
 * 这是一个核心事务：更新验证表状态的同时，更新所属 User 的状态与等级。
 */
router.post('/vetting-requests/:id/review', verifyToken, requireAdminRole, async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const { id } = req.params;
        const { error, value } = reviewSchema.validate(req.body);
        if (error) {
            await session.abortTransaction();
            return res.status(400).json({ error: error.details[0].message });
        }

        const { action, reason, membershipTier } = value;

        // 1. 查找此审核记录
        const request = await VerificationRequest.findById(id).session(session);
        if (!request) {
            await session.abortTransaction();
            return res.status(404).json({ error: '找不到该条认证记录。' });
        }

        if (request.status !== 'PENDING') {
            await session.abortTransaction();
            return res.status(400).json({ error: `该记录已被处理，当前状态: \${request.status}` });
        }

        // 2. 根据管理员的动作更新 Request 与 User
        const newStatus = action === 'APPROVE' ? 'APPROVED' : 'REJECTED';
        request.status = newStatus;
        request.rejectReason = action === 'REJECT' ? reason : null;
        request.reviewedBy = req.user.id;
        request.reviewedAt = new Date();

        await request.save({ session });

        // 找到申请用户
        let user = await User.findById(request.userId).session(session);
        if (user) {
            if (action === 'APPROVE') {
                user.status = 'ACTIVE';
                user.isVerified = true;
                if (membershipTier) {
                    user.membershipTier = membershipTier;
                }
            } else if (action === 'REJECT') {
                // 如果用户本身还在 PENDING 且资产被全盘否定，可以设为 REJECTED 或降级处理
                user.status = 'REJECTED';
            }
            await user.save({ session });
        }

        // 提交事务
        await session.commitTransaction();

        res.status(200).json({
            success: true,
            message: `成功\${action === 'APPROVE' ? '通过' : '驳回'}该用户的审核请求。`,
            data: {
                requestId: request._id,
                userId: user ? user._id : null,
                newStatus: newStatus
            }
        });

    } catch (error) {
        await session.abortTransaction();
        console.error('[ADMIN] 审批事务失败:', error);
        res.status(500).json({ error: '处理审核时发生系统错误。' });
    } finally {
        session.endSession();
    }
});

// 其他预留的后台功能...
// router.get('/dashboard', verifyToken, requireAdminRole, async (req, res) => { ... });
// router.post('/videos/:id/moderate', verifyToken, requireAdminRole, async (req, res) => { ... });


// ==========================================
// 2. 内容安全与视频审核 (Content Moderation)
// ==========================================

/**
 * 获取待审或被举报的短视频列表
 * 管家团队需在此审核视频的着装、内容是否符合高定社区规范。
 */
router.get('/videos/pending', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { page = 1, limit = 10 } = req.query;
        const skip = (parseInt(page) - 1) * parseInt(limit);

        // 查找处于审核状态的视频
        const videos = await Video.find({ status: 'PENDING_REVIEW' })
            .sort({ createdAt: 1 }) // 按照提交流水线排序，先审早提交的
            .skip(skip)
            .limit(parseInt(limit))
            .populate('userId', 'name avatarUrl status membershipTier')
            .lean();

        const total = await Video.countDocuments({ status: 'PENDING_REVIEW' });

        res.status(200).json({
            data: videos,
            meta: {
                total,
                page: parseInt(page),
                pages: Math.ceil(total / limit)
            }
        });

    } catch (error) {
        console.error('[ADMIN] 获取待审视频失败:', error);
        res.status(500).json({ error: '读取审核池失败。' });
    }
});

const moderateVideoSchema = Joi.object({
    action: Joi.string().valid('APPROVE', 'REJECT', 'BAN_USER').required(),
    reason: Joi.string().max(300).allow('').optional()
});

/**
 * 执行视频内容安全裁决
 * 可以是通过、驳回下架、甚至直接封禁发布者。
 */
router.post('/videos/:id/moderate', verifyToken, requireAdminRole, async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const { id } = req.params;
        const { error, value } = moderateVideoSchema.validate(req.body);
        if (error) {
            await session.abortTransaction();
            return res.status(400).json({ error: error.details[0].message });
        }

        const { action, reason } = value;

        // 1. 查找目标视频
        const video = await Video.findById(id).session(session);
        if (!video) {
            await session.abortTransaction();
            return res.status(404).json({ error: '该视频记录不存在。' });
        }

        // 2. 根据裁决更新视频与用户状态
        if (action === 'APPROVE') {
            video.status = 'APPROVED';
            // 审批通过的瞬间可以增加一条管家内部记录
        } else if (action === 'REJECT') {
            video.status = 'REJECTED';
            video.bio = `[此内容因违反高定社区规范已被折叠。原因: ${reason || '不符合圈层调性'}]`;
        } else if (action === 'BAN_USER') {
            video.status = 'REJECTED';

            // 连带封禁账号，清除害群之马
            await User.findByIdAndUpdate(
                video.userId,
                { $set: { status: 'BANNED' } },
                { session }
            );
        }

        await video.save({ session });
        await session.commitTransaction();

        res.status(200).json({
            success: true,
            message: `管家审核完成：视频已${action === 'APPROVE' ? '上架发布' : '下架处理'}。`,
            data: {
                videoId: video._id,
                newStatus: video.status
            }
        });

    } catch (error) {
        await session.abortTransaction();
        console.error('[ADMIN] 视频安全审核失败:', error);
        res.status(500).json({ error: '处理审核时系统遭遇异常。' });
    } finally {
        session.endSession();
    }
});


// ==========================================
// 3. 沙龙活动与高端运营 (Events Management)
// ==========================================

const createEventSchema = Joi.object({
    title: Joi.string().max(100).required(),
    description: Joi.string().required(),
    type: Joi.string().valid('SALON', 'PARTY', 'TASTING', 'EXHIBITION').default('SALON'),
    city: Joi.string().required(),
    location: Joi.string().required(),
    date: Joi.date().iso().required(),
    capacity: Joi.number().integer().min(1).required(),
    attireProtocol: Joi.string().default('Black Tie / Evening Gown')
});

/**
 * 发布新的高定沙龙或派对
 */
router.post('/events', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { error, value } = createEventSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const newEvent = new Event({
            ...value,
            status: 'UPCOMING'
        });

        await newEvent.save();

        res.status(201).json({
            success: true,
            message: '盛宴已成功筹备并发布。',
            data: newEvent
        });

    } catch (error) {
        console.error('[ADMIN] 发布活动失败:', error);
        res.status(500).json({ error: '管家筹备活动时遇到阻碍。' });
    }
});

/**
 * 获取某场活动的嘉宾名单 (RSVP 列表)
 */
router.get('/events/:id/rsvps', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { id } = req.params;
        const status = req.query.status; // 可选过滤 PENDING 或 WAITLISTED

        const query = { eventId: id };
        if (status) query.status = status;

        const rsvps = await require('../models/EventRSVP').find(query)
            .populate('userId', 'name phone membershipTier isVerified trustScore followersCount')
            .sort({ createdAt: 1 })
            .lean();

        res.status(200).json({ data: rsvps });

    } catch (error) {
        res.status(500).json({ error: '无法获取席位名单。' });
    }
});

const reviewRsvpSchema = Joi.object({
    action: Joi.string().valid('CONFIRM', 'REJECT').required(),
    reason: Joi.string().max(200).allow('').optional()
});

/**
 * 人工确认或拒绝嘉宾的出席申请 (例如从 PENDING/WAITLISTED 变 CONFIRMED)
 */
router.post('/events/rsvps/:rsvpId/review', verifyToken, requireAdminRole, async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const { rsvpId } = req.params;
        const { error, value } = reviewRsvpSchema.validate(req.body);
        if (error) {
            await session.abortTransaction();
            return res.status(400).json({ error: error.details[0].message });
        }

        const { action, reason } = value;

        const rsvp = await require('../models/EventRSVP').findById(rsvpId).session(session);
        if (!rsvp) {
            await session.abortTransaction();
            return res.status(404).json({ error: '未找到该 RSVP 记录。' });
        }

        if (rsvp.status === 'CONFIRMED' && action === 'CONFIRM') {
            await session.abortTransaction();
            return res.status(400).json({ error: '该席位已被确认过。' });
        }

        // 如果是确认动作
        if (action === 'CONFIRM') {
            const event = await Event.findById(rsvp.eventId).session(session);

            // 注意：在普通报名 (/events/:id/rsvp) 时，如果是 PENDING 状态，enrolledCount 已经增加了。
            // 所以从 PENDING 转 CONFIRMED 不需要再增加 enrolledCount。
            // 但如果从 WAITLISTED (之前名额满时存为候补) 转 CONFIRMED，则需要检查名额并增加 enrolledCount。
            if (rsvp.status === 'WAITLISTED') {
                 if (event.enrolledCount + rsvp.partySize > event.capacity) {
                     await session.abortTransaction();
                     return res.status(400).json({ error: '该沙龙的贵宾席位已满，无法通过更多申请。' });
                 }
                 event.enrolledCount += rsvp.partySize;
                 await event.save({ session });
            }
            rsvp.status = 'CONFIRMED';

        } else if (action === 'REJECT') {
            // 如果拒绝了 PENDING 或 CONFIRMED (占用名额的状态)，需要释放名额
            if (rsvp.status === 'PENDING' || rsvp.status === 'CONFIRMED') {
                 const event = await Event.findById(rsvp.eventId).session(session);
                 event.enrolledCount = Math.max(0, event.enrolledCount - rsvp.partySize);
                 await event.save({ session });
            }
            rsvp.status = 'CANCELLED';
            rsvp.reason = reason || '由于场地受限，本次无法为您保留席位。';
        }

        await rsvp.save({ session });
        await session.commitTransaction();

        res.status(200).json({ success: true, message: `已成功${action === 'CONFIRM' ? '确认' : '拒绝'}该嘉宾的出席。` });

    } catch (error) {
        await session.abortTransaction();
        console.error('[ADMIN] 处理 RSVP 失败:', error);
        res.status(500).json({ error: '排座系统发生异常。' });
    } finally {
        session.endSession();
    }
});


// ==========================================
// 4. 盲盒与假面舞会运营 (Masquerade Management)
// ==========================================

const createSessionSchema = Joi.object({
    sessionDate: Joi.date().iso().required(),
    question: Joi.string().required(),
    startTime: Joi.date().iso().required(),
    endTime: Joi.date().iso().required()
});

/**
 * 筹办今晚或未来的假面舞会 (Masquerade Session)
 */
router.post('/masquerade/sessions', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { error, value } = createSessionSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const newSession = new MasqueradeSession({
            ...value,
            status: 'SCHEDULED' // 根据 startTime，系统自动或后台手动变更为 ACTIVE
        });

        // 实际场景下，可以使用定时任务 (node-cron) 来自动切换 SCHEDULED 到 ACTIVE
        // 这里提供基础的入库支持
        await newSession.save();

        res.status(201).json({
            success: true,
            message: '今晚的灵魂拷问已设下。',
            data: newSession
        });

    } catch (error) {
        if (error.code === 11000) {
            return res.status(400).json({ error: '该日期已存在规划好的舞会，请勿重复添加。' });
        }
        res.status(500).json({ error: '开启舞会失败。' });
    }
});

/**
 * 获取一场假面舞会的所有回答 (用于离线灵魂匹配)
 */
router.get('/masquerade/sessions/:id/answers', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { id } = req.params;
        const matchStatus = req.query.matchStatus; // PENDING, MATCHED, UNMATCHED

        const query = { sessionId: id };
        if (matchStatus) query.matchStatus = matchStatus;

        const answers = await require('../models/MasqueradeAnswer').find(query)
            .populate('userId', 'name location minAgePreference')
            .lean();

        res.status(200).json({
            data: answers,
            meta: {
                total: answers.length,
                pendingCount: answers.filter(a => a.matchStatus === 'PENDING').length
            }
        });

    } catch (error) {
        res.status(500).json({ error: '无法获取假面舞会的回声。' });
    }
});

module.exports = router;
