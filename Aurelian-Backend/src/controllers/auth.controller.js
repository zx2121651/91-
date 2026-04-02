const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../middleware/auth.middleware');

// Mock Database
const usersDB = {
    'user@example.com': { id: 'usr_1001', status: 'PENDING' }, // Needs invite code
    'elite@aurelian.com': { id: 'usr_1002', status: 'ACTIVE' }  // Already verified
};

// 1. 登录/注册逻辑
const login = (req, res) => {
    const { email, code } = req.body;

    // Validate request (Simplified for MVP)
    if (!email) {
        return res.status(400).json({ error: 'Email is required' });
    }

    // Lookup or "Register"
    let user = usersDB[email];
    let isNewUser = false;

    if (!user) {
        // High-end app: default to PENDING. Must be verified later.
        user = { id: `usr_${Date.now()}`, status: 'PENDING' };
        usersDB[email] = user;
        isNewUser = true;
    }

    // Issue JWT Token
    const token = jwt.sign(
        { id: user.id, status: user.status },
        JWT_SECRET,
        { expiresIn: '7d' }
    );

    res.status(200).json({
        data: {
            token,
            isNewUser,
            status: user.status
        }
    });
};

// 2. 验证黑金邀请码 (护城河核心逻辑)
const verifyInvite = (req, res) => {
    const { inviteCode } = req.body;
    const userId = req.user.id; // From verifyToken middleware

    // Hardcoded elite invite code for MVP testing
    if (inviteCode === 'GOLDEN777') {
        // Upgrade user status
        const newToken = jwt.sign(
            { id: userId, status: 'ACTIVE' },
            JWT_SECRET,
            { expiresIn: '7d' }
        );

        return res.status(200).json({
            data: {
                valid: true,
                referrerId: 'usr_elite_001',
                newToken: newToken // Client must replace old token with this upgraded one
            }
        });
    } else {
        return res.status(400).json({
            error: 'Invalid Code',
            message: 'This invitation code is invalid or has expired.'
        });
    }
};

module.exports = {
    login,
    verifyInvite
};
