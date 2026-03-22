const express = require('express');
const router = express.Router();

// GET /api/v1/profile/me
router.get('/me', (req, res) => {
    res.json({
        data: {
            id: 'usr_1',
            name: '林静恩',
            bio: '古典乐与现代主义建筑的鉴赏者。',
            location: '中国, 北京',
            membership: 'ROYAL',
            isVerified: true,
            stealthMode: false
        }
    });
});

// POST /api/v1/vetting/submit-assets
router.post('/submit-assets', (req, res) => {
    res.json({
        data: {
            status: 'PENDING_REVIEW',
            message: '您的资产认证材料已提交，常务委员会将在48小时内完成审核。'
        }
    });
});

module.exports = router;
