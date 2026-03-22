const express = require('express');
const router = express.Router();

const feedData = [
    {
        userId: "usr_2",
        name: "苏婉, 26",
        bio: "独立艺术策展人，游历全球的旅者。",
        location: "上海, 中国",
        videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        coverUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        isLiked: false
    }
];

router.get('/videos', (req, res) => {
    res.json({
        data: feedData,
        nextCursor: "bGFzdF92aWRlb19pZF8xMjM="
    });
});

module.exports = router;
