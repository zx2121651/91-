const express = require('express');
const router = express.Router();

// GET /api/v1/conversations
router.get('/conversations', (req, res) => {
    res.json({
        data: [
            {
                convId: "cnv_001",
                type: "CONCIERGE",
                user: { name: "礼宾管家 朱利安", verified: true, avatarUrl: "..." },
                lastMessage: "晚上好，需要为您预订明天的画廊吗？",
                unreadCount: 0
            },
            {
                convId: "cnv_002",
                type: "MEMBER",
                user: { name: "伊莎贝拉, 28", verified: true, avatarUrl: "..." },
                lastMessage: "非常期待明天的画廊私人预览。",
                unreadCount: 1
            }
        ]
    });
});

// POST /api/v1/invitations/send
router.post('/invitations/send', (req, res) => {
    const { targetUserId, type, location, time, message } = req.body;
    if (!targetUserId || !type || !location || !time) {
        return res.status(400).json({ error: 'Missing required invitation fields.' });
    }

    // Simulate sending a high-end invitation card
    res.json({
        data: {
            inviteId: `inv_${Date.now()}`,
            status: "SENT",
            message: "高定请柬已通过专线发送。"
        }
    });
});

// POST /api/v1/invitations/:id/respond
router.post('/invitations/:id/respond', (req, res) => {
    const { id } = req.params;
    const { action } = req.body; // ACCEPT or DECLINE

    if (!['ACCEPT', 'DECLINE'].includes(action)) {
        return res.status(400).json({ error: 'Invalid action. Must be ACCEPT or DECLINE.' });
    }

    res.json({
        data: {
            inviteId: id,
            status: action === 'ACCEPT' ? 'ACCEPTED' : 'DECLINED'
        }
    });
});

module.exports = router;
