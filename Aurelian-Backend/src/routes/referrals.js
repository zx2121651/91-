const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const InviteCode = require('../models/InviteCode');

/**
 * 获取当前用户的可用专属邀请码及邀请统计
 * 根据业务，通常一个新会员入会后会获得 1-2 个初始短码
 */
router.get('/status', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const userId = req.user.id;

        // 1. 查询用户是否已有生成的 InviteCode 记录且有效
        const inviteCodeRecord = await InviteCode.findOne({
            creatorId: userId,
            status: 'ACTIVE'
        }).lean();

        if (!inviteCodeRecord) {
            // 如果没有分配过邀请码，可能需要由系统在此处动态生成一个
            // 假设黑卡会员自动获得无限制使用次数的码，普通会员只能生成一次。
            const user = await require('../models/User').findById(userId).select('membershipTier name');
            const limit = user.membershipTier === 'BLACK' ? 100 : 2; // 黑卡送 100 次，普通 2 次
            const randomSuffix = Math.random().toString(36).substring(2, 6).toUpperCase();
            const generatedCode = `AURELIAN-\${user.name.substring(0, 3).toUpperCase()}-\${randomSuffix}`;

            const newCode = new InviteCode({
                code: generatedCode,
                creatorId: userId,
                status: 'ACTIVE',
                usageLimit: limit,
                usageCount: 0
            });
            await newCode.save();

            return res.status(200).json({
                data: {
                    inviteCode: newCode.code,
                    remaining: limit,
                    usedByCount: 0
                },
                message: '您的专属邀请短码已生成。'
            });
        }

        // 2. 如果已经存在，计算剩余名额
        const remaining = inviteCodeRecord.usageLimit - inviteCodeRecord.usageCount;

        // 可选：如果用完了把状态设为 USED
        if (remaining <= 0 && inviteCodeRecord.status !== 'USED') {
             await InviteCode.updateOne({ _id: inviteCodeRecord._id }, { $set: { status: 'USED' } });
        }

        res.status(200).json({
            data: {
                inviteCode: inviteCodeRecord.code,
                remaining: remaining > 0 ? remaining : 0,
                usedByCount: inviteCodeRecord.usageCount
            },
            message: '感谢您邀请同好加入高定社交圈。'
        });

    } catch (error) {
        console.error('[REFERRALS] 获取邀请状态失败:', error);
        res.status(500).json({ error: '管家正在查询邀请记录，请稍候。' });
    }
});

module.exports = router;
