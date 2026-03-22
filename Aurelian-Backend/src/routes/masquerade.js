const express = require('express');
const router = express.Router();

// GET /api/v1/masquerade/status
router.get('/status', (req, res) => {
    // Simulate Friday Midnight event
    const now = new Date();
    const isFriday = now.getDay() === 5;

    res.json({
        data: {
            isOpen: isFriday, // Only open on Fridays
            endTime: Date.now() + 7200000, // 2 hours from now
            question: "品味契合：您最偏爱的单一麦芽威士忌是哪一款，以及为何？"
        }
    });
});

// POST /api/v1/masquerade/submit
router.post('/submit', (req, res) => {
    const { answer } = req.body;
    if (!answer || answer.length < 10) {
        return res.status(400).json({ error: '您的见解过于简短，请详细描述您的品味。' });
    }

    res.json({
        data: {
            status: "MATCHING_IN_PROGRESS",
            message: "您的见解已提交，正在为您在全球精英库中寻找共鸣的灵魂。"
        }
    });
});

module.exports = router;
