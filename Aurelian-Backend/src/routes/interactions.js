const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const Joi = require('joi');
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const Interaction = require('../models/Interaction');
const User = require('../models/User');
const Video = require('../models/Video');

// (假设有 Match 模型，此处作为示例。如果不存在可以自行补充，这里仅展示相互喜欢的判断逻辑)
// const Match = require('../models/Match');

// 点赞/喜欢 路由
router.post('/like', verifyToken, requireActiveStatus, async (req, res) => {
    // 假设前端不仅传 targetUserId，可能还会传 videoId
    const { targetUserId, videoId } = req.body;

    if (!targetUserId) {
        return res.status(400).json({ error: '请选择一个心动的高定会员。' });
    }

    try {
        // 1. 防止重复点赞 (Upsert 或查询)
        const existingInteraction = await Interaction.findOne({
            userId: req.user.id,
            targetUserId: targetUserId,
            type: 'LIKE'
        });

        if (existingInteraction) {
            return res.status(400).json({ error: '您已经对 Ta 表达过心动了。' });
        }

        // 2. 插入新的心动记录
        const newInteraction = new Interaction({
            userId: req.user.id,
            targetUserId: targetUserId,
            videoId: videoId || null,
            type: 'LIKE'
        });

        // 如果带了视频 ID，我们也可以顺便更新视频的统计数据
        if (videoId) {
            await Video.findByIdAndUpdate(videoId, { $inc: { likesCount: 1 } });
        }

        await newInteraction.save();

        // 3. 检查 Mutual Match (相互喜欢)
        const reciprocalInteraction = await Interaction.findOne({
            userId: targetUserId,
            targetUserId: req.user.id,
            type: 'LIKE'
        });

        if (reciprocalInteraction) {
            // 相互心动了！可以在这里写入 Match 表。
            console.log(`[MATCH] 用户 \${req.user.id} 与 \${targetUserId} 相互心动。`);

            // 假设我们触发 WebSocket 事件给双方
            // req.io.to(targetUserId).emit('new_match', { matchId: 'new_match_id' });

            return res.status(200).json({
                data: {
                    matched: true,
                    matchId: `match_\${Date.now()}`
                },
                message: '缘分在午夜绽放，立刻去打个招呼吧！'
            });
        } else {
            // 单向心动
            return res.status(200).json({
                data: { matched: false },
                message: '您的心动已传达。'
            });
        }
    } catch (error) {
        console.error('[INTERACTION] 点赞失败:', error);
        res.status(500).json({ error: '服务器出了点小问题，请稍后重试' });
    }
});

// 跳过 路由 (用于划掉不感兴趣的视频/用户，记录到 Interaction 防止重复推荐)
router.post('/pass', verifyToken, requireActiveStatus, async (req, res) => {
    const { targetUserId, videoId } = req.body;

    if (!targetUserId && !videoId) {
        return res.status(400).json({ error: '无效的目标。' });
    }

    try {
        const passInteraction = new Interaction({
            userId: req.user.id,
            targetUserId: targetUserId || mongoose.Types.ObjectId(), // 取巧处理 null
            videoId: videoId || null,
            type: 'PASS'
        });

        // 忽略 Duplicate Key 错误
        await passInteraction.save().catch(e => {
            if (e.code !== 11000) throw e;
        });

        res.status(200).json({ success: true });
    } catch (error) {
        res.status(500).json({ error: '服务器错误' });
    }
});

module.exports = router;
