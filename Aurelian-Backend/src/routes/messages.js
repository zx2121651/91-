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


// GET /api/v1/messages/conversations/:id/messages
router.get('/conversations/:id/messages', (req, res) => {
    const { id } = req.params;
    // Mock existing messages
    res.json({
        data: [
            {
                id: "msg_1",
                sender: { id: "other", name: "对方", bio: "", location: "" },
                content: "非常期待明天的画廊私人预览。",
                timestamp: "21:14"
            },
            {
                id: "msg_2",
                sender: { id: "me", name: "我", bio: "", location: "" },
                content: "我也是。听说这次展出的几幅后现代作品很值得期待。",
                timestamp: "21:16"
            }
        ]
    });
});

// POST /api/v1/messages/send
router.post('/send', (req, res) => {
    const { convId, content } = req.body;
    if (!convId || !content) {
        return res.status(400).json({ error: 'Missing convId or content' });
    }

    // Mock successful send
    res.json({
        data: {
            msgId: `msg_${Date.now()}`,
            timestamp: Date.now()
        }
    });
});

module.exports = router;
