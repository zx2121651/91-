const express = require('express');
const router = express.Router();
const Joi = require('joi');
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const User = require('../models/User');

const updateProfileSchema = Joi.object({
    name: Joi.string().max(20).optional(),
    bio: Joi.string().max(300).allow('').optional(),
    location: Joi.string().max(100).optional(),
    avatarUrl: Joi.string().uri().optional()
});

const preferencesSchema = Joi.object({
    stealthMode: Joi.boolean().optional(),
    minAgePreference: Joi.number().min(18).max(100).optional()
});

/**
 * 获取个人信息或他人公开信息
 * 配合前端 userId 路由参数复用 (如果是 'me' 则查询自己，否则查对方)
 */
router.get('/:userId', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { userId } = req.params;
        const targetId = (userId === 'me') ? req.user.id : userId;

        // 根据 targetId 查询详细用户数据
        const user = await User.findById(targetId)
            .select('name location bio avatarUrl status membershipTier isVerified followersCount followingCount trustScore')
            .lean();

        if (!user) {
            return res.status(404).json({ error: '查无此高定会员' });
        }

        // 数据脱敏：如果不是自己，隐私或风控字段需要隐藏
        if (targetId !== req.user.id) {
            delete user.status;
            delete user.trustScore;
        }

        res.status(200).json({
            data: {
                id: user._id.toString(),
                name: user.name,
                membership: user.membershipTier,
                isVerified: user.isVerified,
                bio: user.bio,
                location: user.location,
                avatarUrl: user.avatarUrl,
                followers: user.followersCount,
                following: user.followingCount
            }
        });
    } catch (error) {
        console.error('[PROFILE] 获取资料失败:', error);
        res.status(500).json({ error: '服务器错误' });
    }
});

/**
 * 更新自己的主页资料
 */
router.post('/update', verifyToken, async (req, res) => {
    try {
        const { error, value } = updateProfileSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        // 只更新提供的字段
        const updatedUser = await User.findByIdAndUpdate(
            req.user.id,
            { $set: value },
            { new: true } // 返回更新后的记录
        );

        if (!updatedUser) {
            return res.status(404).json({ error: '当前账号已失效或被冻结。' });
        }

        res.status(200).json({
            success: true,
            message: '您的高定资料已更新',
            data: updatedUser
        });

    } catch (error) {
        console.error('[PROFILE] 资料更新失败:', error);
        res.status(500).json({ error: '保存失败，请稍后重试' });
    }
});

/**
 * 更新偏好设置
 */
router.post('/preferences', verifyToken, async (req, res) => {
    try {
        const { error, value } = preferencesSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        await User.findByIdAndUpdate(req.user.id, { $set: value });

        res.status(200).json({
            success: true,
            message: '隐私与偏好设置已保存'
        });

    } catch (error) {
        res.status(500).json({ error: '设置保存失败' });
    }
});

module.exports = router;
