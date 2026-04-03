const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Store active SSE connections (userId -> res object)
// In production, use Redis Pub/Sub if multiple Node instances are running
const activeVettingStreams = new Map();

// 1. Submit Assets (Triggers UNDER_REVIEW state)
router.post('/submit-assets', verifyToken, (req, res) => {
    const { documentUrls } = req.body;
    if (!documentUrls || documentUrls.length === 0) {
        return res.status(400).json({ error: 'Asset documentation required for review.' });
    }

    // Move user to UNDER_REVIEW in real DB
    res.status(200).json({ status: "UNDER_REVIEW" });
});

// 2. Server-Sent Events (SSE) Stream for real-time status updates
// This allows the client's "Breathing Skeleton" to instantly unlock
// the moment an admin or AI approves their application, without ugly polling.
router.get('/stream', verifyToken, (req, res) => {
    const userId = req.user.id;

    // Set headers for SSE
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');

    // Send an initial heartbeat to confirm connection
    res.write(`data: ${JSON.stringify({ event: 'CONNECTED', message: 'Awaiting Concierge Review...' })}\n\n`);

    // Store the response object so we can push data to it later
    activeVettingStreams.set(userId, res);
    console.log(`[VETTING STREAM] User ${userId} is now waiting at the Velvet Rope.`);

    // Handle client disconnect
    req.on('close', () => {
        console.log(`[VETTING STREAM] User ${userId} disconnected from the Velvet Rope.`);
        activeVettingStreams.delete(userId);
    });

    // --- MOCKING THE APPROVAL PROCESS ---
    // For demo purposes: If they wait for 10 seconds, we magically approve them
    setTimeout(() => {
        const stream = activeVettingStreams.get(userId);
        if (stream) {
            console.log(`[VETTING STREAM] Approving user ${userId} automatically after 10s...`);

            // In a real app, this event tells the client to fetch a new JWT Token and transition to the Feed
            stream.write(`data: ${JSON.stringify({
                event: 'STATUS_UPDATED',
                newStatus: 'ACTIVE',
                message: 'Welcome to Aurelian Night.'
            })}\n\n`);

            // Optional: Close the stream after approval
            // stream.end();
        }
    }, 10000);
});

// 3. Admin Webhook to trigger approval manually
// E.g., An admin clicks "Approve" on a dashboard
router.post('/admin/approve/:userId', (req, res) => {
    // SECURITY: This route MUST be protected by an ADMIN role middleware in production
    const { userId } = req.params;

    const stream = activeVettingStreams.get(userId);
    if (stream) {
        stream.write(`data: ${JSON.stringify({
            event: 'STATUS_UPDATED',
            newStatus: 'ACTIVE',
            message: 'Your application has been manually approved by the Concierge.'
        })}\n\n`);
        console.log(`[VETTING STREAM] Admin pushed APPROVAL to user ${userId}`);
        res.status(200).json({ success: true, message: 'Approval pushed to client instantly.' });
    } else {
        res.status(404).json({ error: 'User is not currently connected to the vetting stream.' });
    }
});

module.exports = router;
