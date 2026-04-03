require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');

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

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

// Routes
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

// Health Check
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'UP', message: 'Aurelian Night API is running.' });
});

// Error Handling Middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Internal Server Error', details: err.message });
});

app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
