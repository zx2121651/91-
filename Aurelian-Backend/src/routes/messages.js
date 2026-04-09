const express = require('express');
const router = express.Router();
const Joi = require('joi');
const mongoose = require('mongoose');
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const Conversation = require('../models/Conversation');
const Message = require('../models/Message');
const User = require('../models/User');

const sendMessageSchema = Joi.object({
    convId: Joi.string().required(),
    content: Joi.string().max(2000).required().messages({
        'string.empty': '私密消息内容不能空白。'
    }),
    type: Joi.string().valid('TEXT', 'IMAGE', 'VIDEO').default('TEXT'),
    isEphemeral: Joi.boolean().default(false)
});

/**
 * 获取会话列表 (Conversations)
 * 这是聊天首页的入口，我们需要查询当前用户参与的所有会话，并按最后活跃时间排序。
 */
router.get('/conversations', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const userId = req.user.id;

        // 1. 查询该用户参与的所有没有被归档（或屏蔽）的会话
        const conversations = await Conversation.find({
            participants: userId,
            isArchived: false
        })
        .sort({ updatedAt: -1 })
        .populate('participants', 'name avatarUrl isVerified')
        .lean();

        // 2. 映射出前端需要的数据：会话 ID、对方名字、最后一条消息、未读数等
        const formattedConvs = conversations.map(conv => {
            const otherParticipant = conv.participants.find(p => p._id.toString() !== userId) || conv.participants[0];

            // 注意：如果 Mongoose Map 取值在 lean() 中是个普通对象
            const unreadCount = conv.unreadCounts ? (conv.unreadCounts[userId] || 0) : 0;

            return {
                convId: conv._id.toString(),
                participantName: otherParticipant.name,
                participantAvatar: otherParticipant.avatarUrl,
                isVerified: otherParticipant.isVerified,
                lastMessage: conv.lastMessageText || '暂无消息，开始你们的高定对话吧',
                lastMessageTime: conv.lastMessageTimestamp,
                unreadCount: unreadCount
            };
        });

        res.status(200).json({ data: formattedConvs });
    } catch (error) {
        console.error('[MESSAGES] 获取会话列表失败:', error);
        res.status(500).json({ error: '获取私密信箱失败' });
    }
});

/**
 * 获取单聊记录 (Messages)
 * 在聊天框内部，通过光标进行历史消息的按需加载。
 */
router.get('/conversations/:id/messages', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const convId = req.params.id;
        const limit = parseInt(req.query.limit) || 30;
        const cursor = req.query.cursor;
        const userId = req.user.id;

        // 验证权限：当前用户必须是这个会话的参与者
        const conversation = await Conversation.findOne({
            _id: convId,
            participants: userId
        });

        if (!conversation) {
            return res.status(403).json({ error: '您无权查看此私密对话。' });
        }

        // 获取消息列表
        const query = { conversationId: convId, status: { $ne: 'RECALLED' } };
        if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
            query._id = { $lt: cursor };
        }

        const messages = await Message.find(query)
            .sort({ _id: -1 }) // 降序：最新在前面，前端反转显示
            .limit(limit)
            .populate('senderId', 'name avatarUrl')
            .lean();

        const formattedMessages = messages.map(msg => ({
            msgId: msg._id.toString(),
            senderId: msg.senderId._id.toString(),
            senderName: msg.senderId.name,
            content: msg.isEphemeral && msg.status === 'READ' ? '[阅后即焚内容已销毁]' : msg.content,
            type: msg.type,
            timestamp: msg.createdAt,
            status: msg.status,
            isMe: msg.senderId._id.toString() === userId
        }));

        res.status(200).json({
            data: formattedMessages,
            nextCursor: messages.length === limit ? messages[messages.length - 1]._id.toString() : null
        });

    } catch (error) {
        console.error('[MESSAGES] 获取历史消息失败:', error);
        res.status(500).json({ error: '无法解密历史记录' });
    }
});

/**
 * 发送新消息 (支持文本、图片、视频)
 * 需要开启事务确保 Message 插入和 Conversation 缓存更新的一致性，但为了演示这里使用并发/连续写入
 */
router.post('/send', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { error, value } = sendMessageSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const { convId, content, type, isEphemeral } = value;
        const senderId = req.user.id;

        // 1. 验证会话权限
        const conversation = await Conversation.findOne({
            _id: convId,
            participants: senderId,
            isArchived: false
        });

        if (!conversation) {
            return res.status(403).json({ error: '对话不存在或已被屏蔽。' });
        }

        // 2. 创建新消息记录
        const newMessage = new Message({
            conversationId: convId,
            senderId: senderId,
            type: type,
            content: content,
            isEphemeral: isEphemeral
        });

        await newMessage.save();

        // 3. 更新会话的缓存和未读数 (原子操作)
        // 找到对方的 ID
        const targetId = conversation.participants.find(p => p.toString() !== senderId)?.toString();

        let updateQuery = {
            $set: {
                lastMessageText: type === 'TEXT' ? content : `[\${type}]`,
                lastMessageTimestamp: new Date(),
                lastSenderId: senderId
            }
        };

        // 对接收方累加未读数量
        if (targetId) {
            updateQuery.$inc = { [`unreadCounts.\${targetId}`]: 1 };
        }

        await Conversation.findByIdAndUpdate(convId, updateQuery);

        // 4. WebSocket 推送
        // if (targetId) {
        //    req.io.to(targetId).emit('receive_message', { convId, msgId: newMessage._id, content });
        // }

        res.status(201).json({
            data: {
                msgId: newMessage._id.toString(),
                timestamp: newMessage.createdAt.getTime()
            },
            message: '消息已通过加密通道发送。'
        });

    } catch (error) {
        console.error('[MESSAGES] 发送消息失败:', error);
        res.status(500).json({ error: '发送信件时通道被干扰，请稍后重试' });
    }
});

module.exports = router;
