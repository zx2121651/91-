const express = require('express');
const router = express.Router();
const Joi = require('joi');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
// JWT Secret 正常写在 .env 里，这里先给个默认值确保能跑通
const JWT_SECRET = process.env.JWT_SECRET || 'aurelian_super_secret_high_end_key';

const loginSchema = Joi.object({
    email: Joi.string().email().required().messages({
        'string.email': '请使用有效的高端邮箱地址',
        'string.empty': '邮箱不能为空'
    }),
    code: Joi.string().required()
});

router.post('/login', async (req, res) => {
    try {
        const { error, value } = loginSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const { email, code } = value;

        // 这里通常调用发短信/邮件的服务进行 code 校验
        if (code !== '123456' && code !== '000000') {
            return res.status(401).json({ error: '验证码无效' });
        }

        // 在数据库中查找或新建用户
        let user = await User.findOne({ email });
        let isNewUser = false;

        if (!user) {
            isNewUser = true;
            // 自动创建
            user = new User({
                name: `Aurelian Member \${Math.floor(Math.random() * 9000) + 1000}`,
                email,
                status: 'PENDING', // 刚注册是待审核状态
                membershipTier: 'STANDARD'
            });
            await user.save();
        }

        // 签发 JWT
        const token = jwt.sign(
            { id: user._id.toString(), status: user.status, email: user.email },
            JWT_SECRET,
            { expiresIn: '7d' } // Token 有效期 7 天
        );

        res.status(200).json({
            data: {
                token,
                isNewUser,
                userId: user._id.toString(),
                status: user.status
            },
            message: isNewUser ? '欢迎踏入顶级社交圈，请继续完善您的资料' : '欢迎回来，尊贵的会员'
        });

    } catch (error) {
        console.error('[AUTH] 登录失败:', error);
        res.status(500).json({ error: '认证服务器异常，请联系您的专属顾问' });
    }
});

// 其他鉴权接口如 verify-invite 可以继续扩展
module.exports = router;
