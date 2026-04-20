const express = require('express');
const router = express.Router();
const Joi = require('joi');
const { verifyToken } = require('../middleware/auth.middleware');

const VerificationRequest = require('../models/VerificationRequest');
const User = require('../models/User');

const submitAssetsSchema = Joi.object({
    // 支持批量上传身份/资产证明 URL
    documentUrls: Joi.array().items(Joi.string().uri()).min(1).required().messages({
        'array.min': '您必须提供至少一份认证材料。',
        'any.required': '证明材料不可为空。'
    }),
    type: Joi.string().valid('ASSETS', 'IDENTITY').default('IDENTITY'),
    notes: Joi.string().max(500).allow('').optional()
});

/**
 * 提交资产或身份认证资料
 * 我们只允许具有基础 Token 的用户访问，甚至不需要 ACTIVE 状态，因为这是他们变成 ACTIVE 的必经之路。
 */
router.post('/submit-assets', verifyToken, async (req, res) => {
    try {
        const { error, value } = submitAssetsSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const { documentUrls, type, notes } = value;
        const userId = req.user.id;

        // 1. 检查是否已经存在相同类型且正在审核中的请求
        const existingRequest = await VerificationRequest.findOne({
            userId: userId,
            type: type,
            status: 'PENDING'
        });

        if (existingRequest) {
            return res.status(400).json({ error: '您已经有一份相关的认证正在处理中，请勿重复提交。管家将尽快回复您。' });
        }

        // 2. 如果已经通过了，也可以拒绝重新提交
        const approvedRequest = await VerificationRequest.findOne({
            userId: userId,
            type: type,
            status: 'APPROVED'
        });

        if (approvedRequest) {
            return res.status(400).json({ error: '您的资料已被认可，无需再次认证。' });
        }

        // 3. 记录新的审核请求
        const newRequest = new VerificationRequest({
            userId: userId,
            type: type,
            documentUrls: documentUrls,
            notes: notes,
            status: 'PENDING'
        });

        await newRequest.save();

        // 可选：在这里将用户的状态更新为 'PENDING_REVIEW'
        await User.findByIdAndUpdate(userId, { $set: { status: 'PENDING' } });

        res.status(201).json({
            success: true,
            status: 'PENDING',
            message: '您的高定审核材料已安全加密并移交管家部。通常审核会在 24 小时内完成。'
        });

    } catch (error) {
        console.error('[VETTING] 资料提交失败:', error);
        res.status(500).json({ error: '通道加载异常，请联系您的专属顾问' });
    }
});

/**
 * 查询用户的当前审核状态 (可选：给前端展示进度)
 */
router.get('/status', verifyToken, async (req, res) => {
    try {
        const requests = await VerificationRequest.find({ userId: req.user.id })
            .select('type status createdAt rejectReason')
            .sort({ createdAt: -1 })
            .lean();

        res.status(200).json({
            data: requests
        });

    } catch (error) {
        console.error('[VETTING] 查询审核状态失败:', error);
        res.status(500).json({ error: '无法获取审核进度' });
    }
});

module.exports = router;
