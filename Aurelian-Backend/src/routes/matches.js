const express = require('express');
const router = express.Router();

// GET /api/v1/matches
router.get('/', (req, res) => {
    res.json({
        data: [
            {
                matchId: "mtc_101",
                isNew: true,
                user: {
                    userId: "usr_5",
                    name: "沈修明, 32",
                    location: "香港",
                    coverUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80"
                }
            },
            {
                matchId: "mtc_102",
                isNew: false,
                user: {
                    userId: "usr_6",
                    name: "Marcus, 35",
                    location: "London",
                    coverUrl: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80"
                }
            }
        ]
    });
});

module.exports = router;
