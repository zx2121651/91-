const express = require('express');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

// Elite Events Directory
router.get('/', verifyToken, requireActiveStatus, (req, res) => {
    const { type } = req.query; // 'YACHT', 'ART', 'FASHION'

    const mockEvents = [
        { eventId: "evt_mbfw", title: "Milan Fashion Week Afterparty", date: "2024-09-21T22:00:00Z" },
        { eventId: "evt_abm", title: "Art Basel Miami: Private Viewing", date: "2024-12-05T19:00:00Z" }
    ];

    // Filter logic if needed...
    res.status(200).json({ data: mockEvents });
});

router.get('/:id', verifyToken, requireActiveStatus, (req, res) => {
    const { id } = req.params;

    res.status(200).json({
        data: {
            title: "Milan Fashion Week Afterparty",
            date: "2024-09-21T22:00:00Z",
            location: "Secret Villa, Milan",
            description: "An exclusive gathering of designers, models, and top-tier patrons.",
            attireProtocol: "Black Tie / High Fashion",
            coverUrl: "https://example.com/event-cover.jpg"
        }
    });
});

router.post('/:id/rsvp', verifyToken, requireActiveStatus, (req, res) => {
    const { id } = req.params;
    const { partySize } = req.body;

    if (!partySize || partySize > 2) {
        return res.status(400).json({ error: "Party size cannot exceed 2 for this exclusive event." });
    }

    console.log(`[EVENTS] User ${req.user.id} RSVP'd for ${partySize} to event ${id}`);

    res.status(200).json({
        data: { status: "WAITLISTED" } // High-end events are usually waitlisted pending approval
    });
});

module.exports = router;
