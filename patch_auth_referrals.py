import re

file_path = "Aurelian-Backend/src/routes/auth.js"
with open(file_path, "r") as f:
    content = f.read()

# Add invite code usage processing during registration / verify-invite
verify_route = """
const InviteCode = require('../models/InviteCode');

const verifyInviteSchema = Joi.object({
    inviteCode: Joi.string().required()
});

/**
 * 用户输入邀请码以获取入会资格
 * 如果是新会员尝试加入，可以在这里真实查验 inviteCode 表。
 */
router.post('/verify-invite', async (req, res) => {
    try {
        const { error, value } = verifyInviteSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const { inviteCode } = value;

        // 1. 查询邀请码是否有效
        const codeRecord = await InviteCode.findOne({ code: inviteCode.toUpperCase() });

        if (!codeRecord) {
            return res.status(404).json({ error: '邀请短码不合法，入会失败。' });
        }

        // 2. 检查邀请码状态与限制
        if (codeRecord.status !== 'ACTIVE' || codeRecord.usageCount >= codeRecord.usageLimit) {
            return res.status(403).json({ error: '该邀请码名额已满或已失效。' });
        }

        // 3. 检查有效期
        if (codeRecord.expiresAt && codeRecord.expiresAt < new Date()) {
            return res.status(403).json({ error: '该邀请码已过期。' });
        }

        // 4. (这里演示不创建用户，只是验证通过返回)
        // 实际上可以生成一个 "TEMP" Token 给前端跳转去注册，或者直接将对方 creatorId 返回用于建立树形结构

        // 假设用户已经注册了(在 req.user 里)，那么直接绑定并更新
        // 为了演示我们简单返回验证通过，如果实际应用中需要在这里走完完整事务：
        // await InviteCode.updateOne({ _id: codeRecord._id }, { $inc: { usageCount: 1 } });

        res.status(200).json({
            data: {
                valid: true,
                referrerId: codeRecord.creatorId ? codeRecord.creatorId.toString() : 'SYSTEM_ADMIN'
            },
            message: '验证通过，欢迎进入 AURELIAN NIGHT。'
        });

    } catch (error) {
        console.error('[AUTH] 验证邀请码失败:', error);
        res.status(500).json({ error: '内部验证异常，请重试' });
    }
});
"""

if "verify-invite" not in content:
    content = content.replace("module.exports = router;", verify_route + "\nmodule.exports = router;")

with open(file_path, "w") as f:
    f.write(content)
