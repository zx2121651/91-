const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Elite Referral System (The Velvet Rope Generator)
router.get('/status', verifyToken, requireActiveStatus, (req, res) => {
    // Top users get fewer invites to maintain scarcity
    const remaining = req.user.role === 'ADMIN' ? 100 : 3;

    res.status(200).json({
        data: {
            inviteCode: "AUR_ELITE_007",
            remaining: remaining
        }
    });
});

module.exports = router;
