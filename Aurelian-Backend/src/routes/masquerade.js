const express = require('express');
const router = express.Router();
const Joi = require('joi');
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const MasqueradeSession = require('../models/MasqueradeSession');
const MasqueradeAnswer = require('../models/MasqueradeAnswer');

/**
 * 获取今晚假面舞会的状态与问题
 * 如果当前时间在 session 的 startTime 和 endTime 之间，且 status 是 ACTIVE，则开放提交。
 */
router.get('/status', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const now = new Date();

        // 1. 查找符合当前时间的活跃会话
        const activeSession = await MasqueradeSession.findOne({
            status: 'ACTIVE',
            startTime: { $lte: now },
            endTime: { $gte: now }
        }).lean();

        if (!activeSession) {
            return res.status(200).json({
                data: {
                    isOpen: false,
                    endTime: 0,
                    question: "午夜尚未降临，请在 22:00 后揭开面纱。"
                }
            });
        }

        // 2. 检查用户是否已经提交过回答
        const existingAnswer = await MasqueradeAnswer.findOne({
            userId: req.user.id,
            sessionId: activeSession._id
        }).lean();

        if (existingAnswer) {
            return res.status(200).json({
                data: {
                    isOpen: false, // 已提交则不允许再次提交
                    endTime: activeSession.endTime.getTime(),
                    question: "您的灵魂回声已记录，管家正在为您寻觅有缘人。"
                }
            });
        }

        // 3. 返回开启状态及当晚的问题
        res.status(200).json({
            data: {
                isOpen: true,
                endTime: activeSession.endTime.getTime(),
                question: activeSession.question
            },
            message: "戴上面具，回答问题，遇见未知的灵魂。"
        });

    } catch (error) {
        console.error('[MASQUERADE] 获取状态失败:', error);
        res.status(500).json({ error: '假面舞会暂时闭馆。' });
    }
});

const submitAnswerSchema = Joi.object({
    answer: Joi.string().max(500).required().messages({
        'string.empty': '您的灵魂拷问回答不能为空。'
    })
});

/**
 * 提交当晚灵魂拷问的回答
 */
router.post('/submit', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { error, value } = submitAnswerSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const now = new Date();

        // 1. 确认会话是否处于活跃状态
        const activeSession = await MasqueradeSession.findOne({
            status: 'ACTIVE',
            startTime: { $lte: now },
            endTime: { $gte: now }
        });

        if (!activeSession) {
            return res.status(400).json({ error: '假面舞会已经结束，请明日再来。' });
        }

        // 2. 防重提交校验
        const existingAnswer = await MasqueradeAnswer.findOne({
            userId: req.user.id,
            sessionId: activeSession._id
        });

        if (existingAnswer) {
            return res.status(400).json({ error: '您已经递交过面纱下的答卷了。' });
        }

        // 3. 存储用户的回答
        const newAnswer = new MasqueradeAnswer({
            userId: req.user.id,
            sessionId: activeSession._id,
            answerText: value.answer,
            matchStatus: 'PENDING'
        });

        await newAnswer.save();

        res.status(201).json({
            data: { status: 'SUBMITTED' },
            message: '面具已为您戴好，灵魂匹配即将开始，敬请期待管家的通知。'
        });

    } catch (error) {
        console.error('[MASQUERADE] 提交回答失败:', error);
        res.status(500).json({ error: '答卷投递失败，请重试' });
    }
});

module.exports = router;
