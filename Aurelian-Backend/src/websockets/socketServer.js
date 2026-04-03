const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../middleware/auth.middleware');

// In-memory store mapping userId -> socketId
// For a multi-instance production environment, this MUST use Redis (e.g. socket.io-redis adapter)
const onlineUsers = new Map();

function initSocketServer(httpServer) {
    const io = new Server(httpServer, {
        cors: {
            origin: '*', // Allow clients (Android/iOS) to connect
            methods: ['GET', 'POST']
        }
    });

    console.log('[WEBSOCKET] Socket.io server initialized.');

    // 1. WebSocket 鉴权握手 (Zero-Trust Handshake)
    // 只有持有合法的 JWT 且状态为 ACTIVE 的用户才能建立长连接。
    io.use((socket, next) => {
        const token = socket.handshake.auth.token || socket.handshake.headers['authorization'];

        if (!token) {
            console.warn(`[WEBSOCKET] Rejected connection from ${socket.id}: No token provided.`);
            return next(new Error('Authentication error: No token provided'));
        }

        try {
            // "Bearer xxx" -> "xxx"
            const cleanToken = token.startsWith('Bearer ') ? token.split(' ')[1] : token;
            const decoded = jwt.verify(cleanToken, JWT_SECRET);

            if (decoded.status !== 'ACTIVE') {
                console.warn(`[WEBSOCKET] Rejected connection from ${decoded.id}: User status is ${decoded.status}.`);
                return next(new Error('Authentication error: User must be ACTIVE to access real-time features.'));
            }

            // Attach user info to socket instance for later use
            socket.user = decoded;
            next();
        } catch (err) {
            console.warn(`[WEBSOCKET] Rejected connection from ${socket.id}: Invalid token.`);
            return next(new Error('Authentication error: Invalid or expired token'));
        }
    });

    // 2. 核心实时业务事件
    io.on('connection', (socket) => {
        const userId = socket.user.id;

        // Register user online
        onlineUsers.set(userId, socket.id);
        console.log(`[WEBSOCKET] Elite user connected: ${userId} on socket ${socket.id}. Online: ${onlineUsers.size}`);

        // Broadcast to friends/followers (simplified)
        // socket.broadcast.emit('user_online', { userId });

        // -- Event: Private Message --
        socket.on('send_message', (payload) => {
            const { targetUserId, message, convId } = payload;

            console.log(`[CHAT] ${userId} -> ${targetUserId}: "${message.substring(0, 15)}..."`);

            const targetSocketId = onlineUsers.get(targetUserId);

            // If the target is currently online, push instantly
            if (targetSocketId) {
                io.to(targetSocketId).emit('new_message', {
                    convId,
                    senderId: userId,
                    message,
                    timestamp: Date.now()
                });

                // Acknowledge delivery to sender
                socket.emit('message_delivered', { status: 'DELIVERED', targetUserId });
            } else {
                // If offline, logic here would typically save to DB and trigger an APNs/FCM Push Notification
                console.log(`[CHAT] ${targetUserId} is offline. Saving to queue and dispatching Push Notification.`);
                socket.emit('message_delivered', { status: 'QUEUED', targetUserId });
            }
        });

        // -- Event: Typing Indicator --
        // Enhances the luxury real-time feel of the conversation
        socket.on('typing', (payload) => {
            const { targetUserId, isTyping } = payload;
            const targetSocketId = onlineUsers.get(targetUserId);

            if (targetSocketId) {
                io.to(targetSocketId).emit('typing_status', {
                    userId,
                    isTyping
                });
            }
        });

        // -- Event: Disconnect --
        socket.on('disconnect', () => {
            onlineUsers.delete(userId);
            console.log(`[WEBSOCKET] Elite user disconnected: ${userId}. Online: ${onlineUsers.size}`);
            // socket.broadcast.emit('user_offline', { userId });
        });
    });

    return io;
}

module.exports = { initSocketServer };
