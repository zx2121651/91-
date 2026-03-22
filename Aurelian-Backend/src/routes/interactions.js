const express = require('express');
const router = express.Router();

// POST /api/v1/interactions/like
router.post('/like', (req, res) => {
    const { targetUserId } = req.body;
    if (!targetUserId) {
        return res.status(400).json({ error: 'Target user ID is required.' });
    }
    // Mock matching logic: 30% chance of a mutual match
    const isMatched = Math.random() < 0.3;
    res.json({
        data: {
            action: 'LIKE',
            targetUserId,
            matched: isMatched,
            matchId: isMatched ? `mtc_${Date.now()}` : null
        }
    });
});

// POST /api/v1/interactions/pass
router.post('/pass', (req, res) => {
    const { targetUserId } = req.body;
    if (!targetUserId) {
        return res.status(400).json({ error: 'Target user ID is required.' });
    }
    res.json({
        data: {
            action: 'PASS',
            targetUserId,
            success: true
        }
    });
});


// Get admirers (mock)
router.get('/admirers', (req, res) => {
    res.json({
        data: [
            { userId: "usr_3", isBlurred: true },
            { userId: "usr_4", isBlurred: true }
        ]
    });
});

module.exports = router;
