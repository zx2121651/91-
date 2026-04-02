const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');
const { verifyToken } = require('../middleware/auth.middleware');

// Public route: Anyone can request login, but gets a PENDING token
router.post('/login', authController.login);

// Protected route: Needs a basic token to submit an invite code
router.post('/verify-invite', verifyToken, authController.verifyInvite);

module.exports = router;
