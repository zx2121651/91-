require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const http = require('http'); // Node HTTP module needed for Socket.io integration

// Route imports
const authRoutes = require('./src/routes/auth');
const feedRoutes = require('./src/routes/feed');
const profileRoutes = require('./src/routes/profile');
const vettingRoutes = require('./src/routes/vetting');
const matchesRoutes = require('./src/routes/matches');
const eventsRoutes = require('./src/routes/events');
const interactionsRoutes = require('./src/routes/interactions');
const messagesRoutes = require('./src/routes/messages');
const invitationsRoutes = require('./src/routes/invitations');
const masqueradeRoutes = require('./src/routes/masquerade');
const referralsRoutes = require('./src/routes/referrals');
const mediaRoutes = require('./src/routes/media');
const hookupsRoutes = require('./src/routes/hookups');
const adminRoutes = require('./src/routes/admin');

// Socket Server initialization
const { initSocketServer } = require('./src/websockets/socketServer');

const app = express();
const PORT = process.env.PORT || 3000;

// Create HTTP server wrapping Express
const server = http.createServer(app);

// Initialize Socket.io on top of the HTTP server
const io = initSocketServer(server);

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

// Attach the socket.io instance to req so routes can use it if needed
// (e.g., triggering a push notification from an HTTP API)
app.use((req, res, next) => {
    req.io = io;
    next();
});

// REST Routes
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/feed', feedRoutes);
app.use('/api/v1/profile', profileRoutes);
app.use('/api/v1/vetting', vettingRoutes);
app.use('/api/v1/matches', matchesRoutes);
app.use('/api/v1/events', eventsRoutes);
app.use('/api/v1/interactions', interactionsRoutes);
app.use('/api/v1/messages', messagesRoutes);
app.use('/api/v1/invitations', invitationsRoutes);
app.use('/api/v1/masquerade', masqueradeRoutes);
app.use('/api/v1/referrals', referralsRoutes);
app.use('/api/v1/media', mediaRoutes);
app.use('/api/v1/hookups', hookupsRoutes);
app.use('/api/v1/admin', adminRoutes);

// Health Check
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'UP', message: 'Aurelian Night API & WebSocket are running.' });
});

// Error Handling Middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Internal Server Error', details: err.message });
});

// Make sure to call server.listen instead of app.listen!
server.listen(PORT, () => {
    console.log(`Server (HTTP + WS) is running on port ${PORT}`);
});
