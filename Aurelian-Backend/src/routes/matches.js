const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');
const mongoose = require('mongoose');

const Match = require('../models/Match');
const User = require('../models/User');
const Interaction = require('../models/Interaction');

/**
 * 获取当前用户所有的匹配列表
 * 必须支持分页和按时间倒序排列，因为在短视频社交中匹配的频率很高
 */
router.get('/', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = 20;
        const skip = (page - 1) * limit;

        // 1. 查询 Match 表，条件：users 数组中包含我，且 status 是 ACTIVE
        const matches = await Match.find({
            users: req.user.id,
            status: 'ACTIVE'
        })
        .sort({ updatedAt: -1 })
        .skip(skip)
        .limit(limit)
        .populate('users', 'name avatarUrl location bio isVerified membershipTier')
        .lean();

        // 2. 数据格式转换：匹配成功时，我们需要向前端返回对方的信息，所以要过滤出对方的 User 对象
        const formattedMatches = matches.map(match => {
            // 找对方：users 是一个数组，其中不等于我的那个就是 target
            const otherUser = match.users.find(u => u._id.toString() !== req.user.id);

            return {
                matchId: match._id.toString(),
                user: {
                    id: otherUser._id.toString(),
                    name: otherUser.name,
                    bio: otherUser.bio,
                    location: otherUser.location,
                    avatarUrl: otherUser.avatarUrl,
                    isVerified: otherUser.isVerified
                },
                matchedAt: match.updatedAt
            };
        });

        res.status(200).json({
            data: formattedMatches
        });

    } catch (error) {
        console.error('[MATCHES] 获取列表失败:', error);
        res.status(500).json({ error: '无法获取匹配列表，请稍后重试' });
    }
});

/**
 * 解除匹配关系
 */
router.post('/:id/unmatch', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { id } = req.params;

        const match = await Match.findOneAndUpdate(
            { _id: id, users: req.user.id, status: 'ACTIVE' },
            { $set: { status: 'UNMATCHED', unmatchedBy: req.user.id } },
            { new: true }
        );

        if (!match) {
            return res.status(404).json({ error: '找不到对应的匹配记录，可能已经被对方解除。' });
        }

        // 可以同时在 Interaction 表中把对对方的点赞去掉，或者标记为屏蔽
        // 也可以同时触发 WebSocket 通知对方列表更新
        res.status(200).json({ success: true, message: '已取消心动连接，并在对方的列表中隐身。' });

    } catch (error) {
        console.error('[MATCHES] 取消匹配失败:', error);
        res.status(500).json({ error: '取消连接失败' });
    }
});

module.exports = router;
