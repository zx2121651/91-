const express = require('express');
const router = express.Router();

// GET /api/v1/referrals/status
router.get('/status', (req, res) => {
    res.json({
        data: {
            inviteCode: "AURE-X79M-VQ2P",
            remaining: 3,
            message: "Aurelian 依靠会员的卓越品味而不断成长。"
        }
    });
});

module.exports = router;
