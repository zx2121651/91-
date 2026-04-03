const express = require('express');
const router = express.Router();
const { verifyToken } = require('../middleware/auth.middleware');

// Vetting is a special case: pending users NEED to access this to submit assets
router.post('/submit-assets', verifyToken, (req, res) => {
    const { documentUrls } = req.body;
    if (!documentUrls || documentUrls.length === 0) {
        return res.status(400).json({ error: 'Asset documentation required for review.' });
    }

    // Move user to UNDER_REVIEW in real DB
    res.status(200).json({ status: "UNDER_REVIEW" });
});

module.exports = router;
