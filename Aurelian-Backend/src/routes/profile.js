const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Profile & Vetting

router.get('/me', verifyToken, (req, res) => {
    // 允许 PENDING 和 ACTIVE 状态查看自己的基础主页
    res.status(200).json({
        data: {
            id: req.user.id,
            name: req.user.status === 'ACTIVE' ? "Alexandre R." : "New Member",
            membership: req.user.status === 'ACTIVE' ? "Elite Black Card" : "Under Review",
            isVerified: req.user.status === 'ACTIVE'
        }
    });
});

router.post('/update', verifyToken, requireActiveStatus, (req, res) => {
    res.status(200).json({ success: true });
});

router.post('/preferences', verifyToken, requireActiveStatus, (req, res) => {
    res.status(200).json({ success: true });
});

module.exports = router;
