const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// High-end matching
router.get('/', verifyToken, requireActiveStatus, (req, res) => {
    const page = parseInt(req.query.page) || 1;
    res.status(200).json({
        data: [
            {
                matchId: "match_xyz_1",
                user: {
                    id: "usr_match_1",
                    name: "Charlotte D.",
                    location: "London, UK",
                    bio: "Curator at large.",
                    videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                }
            }
        ]
    });
});

module.exports = router;
