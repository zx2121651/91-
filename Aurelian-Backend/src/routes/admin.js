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

module.exports = router;
