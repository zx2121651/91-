const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Inject the double-layer security:
// 1. Must have a valid JWT.
// 2. Must be an 'ACTIVE' (invited/verified) user.
router.get('/videos', verifyToken, requireActiveStatus, (req, res) => {

    // Log the user ID to prove we extracted it from the JWT
    console.log(`[SECURE FEED] Delivering high-end content to active user: ${req.user.id}`);

    // High-quality mock data replacing the old unauthenticated dummy data
    const mockFeed = [
        {
            userId: "usr_elite_01",
            name: "Alexandre de Rothschild",
            bio: "Monaco | Private Equity | Collector",
            location: "Monaco",
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            coverUrl: "https://example.com/cover1.jpg",
            isLiked: false
        },
        {
            userId: "usr_elite_02",
            name: "Eleanor V.",
            bio: "Art curating across Paris and Milan.",
            location: "Paris, France",
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            coverUrl: "https://example.com/cover2.jpg",
            isLiked: true
        }
    ];

    res.status(200).json({
        data: mockFeed,
        nextCursor: "bGFzdF92aWRlb19pZF8wMDI="
    });
});

module.exports = router;
