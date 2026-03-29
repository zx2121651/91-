const express = require('express');
const router = express.Router();

const hookupCards = [
    {
        userId: 'usr_901',
        name: 'Nina, 24',
        age: 24,
        city: 'Shanghai',
        bio: '夜跑、爵士吧、说走就走的周末。',
        intent: 'Tonight',
        tags: ['同城', '夜生活', '不尬聊'],
        avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=500&q=80'
    },
    {
        userId: 'usr_902',
        name: 'K, 27',
        age: 27,
        city: 'Beijing',
        bio: '偏爱有边界感、直接、真诚的连接。',
        intent: 'Weekend',
        tags: ['周末见面', '先聊天', '重视安全'],
        avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=500&q=80'
    },
    {
        userId: 'usr_903',
        name: 'Mia, 26',
        age: 26,
        city: 'Shanghai',
        bio: '周五晚餐 + 微醺聊天，重视礼貌和边界。',
        intent: 'Weekend',
        tags: ['餐酒', '轻社交', '先语音'],
        avatarUrl: 'https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=500&q=80'
    },
    {
        userId: 'usr_904',
        name: 'Leo, 29',
        age: 29,
        city: 'Shenzhen',
        bio: '运动后吃夜宵，喜欢真实不套路。',
        intent: 'Tonight',
        tags: ['运动', '夜宵', '直接'],
        avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=500&q=80'
    }
];

const requestStore = new Map();

router.get('/cards', (req, res) => {
    const city = (req.query.city || '').trim();
    const intent = (req.query.intent || '').trim();
    const limit = Math.min(parseInt(req.query.limit || '10', 10), 20);
    const cursor = parseInt(req.query.cursor || '0', 10);

    let filtered = hookupCards;
    if (city) {
        filtered = filtered.filter((card) => card.city.toLowerCase() === city.toLowerCase());
    }
    if (intent) {
        filtered = filtered.filter((card) => card.intent.toLowerCase() === intent.toLowerCase());
    }

    const start = Number.isNaN(cursor) ? 0 : cursor;
    const end = start + (Number.isNaN(limit) ? 10 : limit);
    const page = filtered.slice(start, end);
    const nextCursor = end < filtered.length ? String(end) : null;

    res.json({
        data: page,
        nextCursor,
        meta: {
            total: filtered.length,
            city: city || null,
            intent: intent || null
        }
    });
});

router.post('/request', (req, res) => {
    const { targetUserId, note, safeMode, meetingType } = req.body;
    if (!targetUserId) {
        return res.status(400).json({ error: 'targetUserId is required' });
    }

    const cleanedNote = (note || '').trim();
    if (cleanedNote.length > 120) {
        return res.status(400).json({ error: 'note must be <= 120 chars' });
    }

    const requestId = `hk_${Date.now()}`;
    const payload = {
        requestId,
        targetUserId,
        note: cleanedNote,
        safeMode: safeMode !== false,
        meetingType: meetingType || 'DRINK',
        status: 'SENT',
        createdAt: Date.now()
    };

    requestStore.set(requestId, payload);

    res.json({ data: payload });
});

router.get('/request/:id', (req, res) => {
    const data = requestStore.get(req.params.id);
    if (!data) {
        return res.status(404).json({ error: 'request not found' });
    }

    const elapsed = Date.now() - data.createdAt;
    const status = elapsed > 10000 ? 'RESPONDED' : data.status;
    res.json({ data: { ...data, status } });
});

module.exports = router;
