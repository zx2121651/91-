const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Elite Invites (Off-platform or specialized meetups)
router.post('/send', verifyToken, requireActiveStatus, (req, res) => {
    const { targetUserId, type, location, time, message } = req.body;

    console.log(`[INVITE] ${req.user.id} inviting ${targetUserId} to a ${type} at ${location}`);

    res.status(200).json({
        inviteId: `inv_${Date.now()}`,
        status: "DELIVERED"
    });
});

router.post('/:id/respond', verifyToken, requireActiveStatus, (req, res) => {
    const { id } = req.params;
    const { action } = req.body; // e.g., 'ACCEPT', 'DECLINE'

    res.status(200).json({
        data: {
            inviteId: id,
            status: action === 'ACCEPT' ? 'CONFIRMED' : 'REJECTED'
        }
    });
});

module.exports = router;
