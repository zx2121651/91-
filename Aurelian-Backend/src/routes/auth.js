const express = require('express');
const jwt = require('jsonwebtoken');
const router = express.Router();

const JWT_SECRET = process.env.JWT_SECRET;

// Login Mock
router.post('/login', (req, res) => {
    const { email } = req.body;
    if (email) {
        const token = jwt.sign({ id: 'usr_1', email }, JWT_SECRET, { expiresIn: '7d' });
        res.json({ data: { token, isNewUser: false } });
    } else {
        res.status(400).json({ error: 'Email is required.' });
    }
});

// Verify Invite Code Mock
router.post('/verify-invite', (req, res) => {
    const { inviteCode } = req.body;
    if (inviteCode === 'AURE-X79M-VQ2P') {
        res.json({ data: { valid: true, referrerId: 'usr_998' } });
    } else {
        res.status(401).json({ error: 'Invalid invitation code.' });
    }
});

module.exports = router;
