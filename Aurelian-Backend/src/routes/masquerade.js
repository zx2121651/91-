const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Midnight Masquerade (Anonymous, high-intellect blind matching)
router.get('/status', verifyToken, requireActiveStatus, (req, res) => {
    // Only open during specific hours, mock open for now
    const now = new Date();
    const isOpen = now.getHours() >= 22 || now.getHours() <= 4;

    res.status(200).json({
        data: {
            isOpen: true, // Mocking to true so client can test
            endTime: Date.now() + 3600000,
            question: "What is the most profound lesson you've learned from a failure?"
        }
    });
});

router.post('/submit', verifyToken, requireActiveStatus, (req, res) => {
    const { answer } = req.body;
    if (!answer || answer.length < 20) {
        return res.status(400).json({ error: "Answers must be substantive." });
    }

    console.log(`[MASQUERADE] User ${req.user.id} submitted profound thought: "${answer.substring(0, 30)}..."`);

    res.status(200).json({
        data: { status: "AWAITING_MATCH" }
    });
});

module.exports = router;
