const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Chat Engine (Placeholder for WebSocket upgrading)
router.get('/conversations', verifyToken, requireActiveStatus, (req, res) => {
    res.status(200).json({
        data: [
            { convId: "conv_123", lastMessage: "See you at the gallery.", unreadCount: 1 }
        ]
    });
});

router.get('/conversations/:id/messages', verifyToken, requireActiveStatus, (req, res) => {
    const { id } = req.params;
    const limit = parseInt(req.query.limit) || 20;

    res.status(200).json({
        data: [
            { id: "msg_1", senderId: "usr_match_1", content: "Are you attending the gala tomorrow?", timestamp: Date.now() - 3600000 },
            { id: "msg_2", senderId: req.user.id, content: "Yes, I'll be in the VIP lounge.", timestamp: Date.now() - 1800000 }
        ]
    });
});

router.post('/send', verifyToken, requireActiveStatus, (req, res) => {
    const { convId, content } = req.body;
    if (!content) return res.status(400).json({ error: "Empty message" });

    console.log(`[CHAT] User ${req.user.id} sending to ${convId}: "${content.substring(0, 20)}..."`);
    res.status(200).json({
        data: {
            msgId: `msg_${Date.now()}`,
            timestamp: Date.now()
        }
    });
});

module.exports = router;
