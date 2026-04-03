const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Interaction Core Engine
router.post('/like', verifyToken, requireActiveStatus, (req, res) => {
    const { targetUserId } = req.body;
    // 模拟 30% 概率触发相互匹配成功
    const isMatch = Math.random() > 0.7;
    const matchId = isMatch ? `match_${Date.now()}` : null;

    console.log(`[ALGO] User ${req.user.id} liked ${targetUserId}. Match=${isMatch}`);

    res.status(200).json({
        data: { matched: isMatch, matchId }
    });
});

router.post('/pass', verifyToken, requireActiveStatus, (req, res) => {
    res.status(200).json({ success: true });
});

router.get('/admirers', verifyToken, requireActiveStatus, (req, res) => {
    // 模拟返回仰慕者列表
    res.status(200).json({
        data: [
            { userId: "usr_admirer_1", isBlurred: true },
            { userId: "usr_admirer_2", isBlurred: false }
        ]
    });
});

module.exports = router;
