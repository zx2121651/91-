const express = require('express');
const router = express.Router();

// GET /api/v1/events
router.get('/', (req, res) => {
    res.json({
        data: [
            {
                eventId: "evt_1",
                title: "金秋假面舞会",
                date: "10月31日",
                time: "21:00",
                location: "贝尔格莱维亚庄园",
                isFeatured: true,
                coverUrl: "https://images.unsplash.com/photo-1519671482749-fd09871171dd?auto=format&fit=crop&w=1200&q=80"
            },
            {
                eventId: "evt_2",
                title: "东方雅集 · 宋代点茶",
                date: "11月5日",
                time: "14:00",
                location: "上海外滩私人茶室",
                isFeatured: false,
                coverUrl: "https://images.unsplash.com/photo-1544256718-3b623862210c?auto=format&fit=crop&w=800&q=80"
            },
            {
                eventId: "evt_3",
                title: "当代艺术鉴赏夜",
                date: "11月12日",
                time: "18:00",
                location: "新邦德街",
                isFeatured: false,
                coverUrl: "https://images.unsplash.com/photo-1544158498-8422fb813db1?auto=format&fit=crop&w=800&q=80"
            }
        ]
    });
});


// GET /api/v1/events/:id
router.get('/:id', (req, res) => {
    const { id } = req.params;
    res.json({
        data: {
            title: "金秋假面舞会",
            date: "10月31日, 21:00 - 02:00",
            location: "贝尔格莱维亚私人庄园 (具体地址于活动前24小时发送)",
            description: "一场致敬威尼斯狂欢节的沉浸式晚宴与舞会。本次活动仅限 50 位尊贵会员参与。请佩戴面具出席，隐藏世俗身份，释放内心真我。",
            attireProtocol: "Black Tie (男士燕尾服，女士晚礼服)，并必须佩戴眼罩或面具。",
            coverUrl: "https://images.unsplash.com/photo-1519671482749-fd09871171dd?auto=format&fit=crop&w=1200&q=80"
        }
    });
});

// POST /api/v1/events/:id/rsvp
router.post('/:id/rsvp', (req, res) => {
    const { id } = req.params;
    const { partySize } = req.body;

    // Simulate RSVP success
    res.json({
        data: {
            status: "REQUESTED"
        }
    });
});

module.exports = router;
