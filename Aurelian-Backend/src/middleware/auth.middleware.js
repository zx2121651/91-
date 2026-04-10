const jwt = require('jsonwebtoken');

// Fallback secret for local development. IN PRODUCTION THIS MUST BE IN .env
const JWT_SECRET = process.env.JWT_SECRET || 'aurelian_night_super_secret_key_2024';

/**
 * 核心鉴权中间件 (Zero-Trust Security)
 * 验证请求头中的 Bearer Token，防止任何未经授权的 API 访问。
 */
const verifyToken = (req, res, next) => {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({
            error: 'Unauthorized',
            message: 'Access denied. No token provided.'
        });
    }

    const token = authHeader.split(' ')[1];

    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        // 将解码后的用户信息挂载到 req 对象上，供后续路由使用
        // 包含: id, status, role
        req.user = decoded;
        next();
    } catch (err) {
        return res.status(403).json({
            error: 'Forbidden',
            message: 'Invalid or expired token.'
        });
    }
};

/**
 * 高端门槛拦截中间件 (The Velvet Rope)
 * 仅允许状态为 'ACTIVE' (已通过资产验证/输入高级邀请码) 的用户访问核心资源(如 Feed 流、私信)。
 * 如果状态为 'PENDING' 或 'WAITLISTED'，强制拒绝访问，阻断劣质用户的渗透。
 */
const requireActiveStatus = (req, res, next) => {
    if (!req.user) {
        return res.status(401).json({ error: 'Unauthorized', message: 'User not authenticated.' });
    }

    if (req.user.status !== 'ACTIVE') {
        return res.status(403).json({
            error: 'Forbidden',
            code: 'STATUS_PENDING',
            message: 'Your account is currently under review or requires an exclusive invite code to access this chamber.'
        });
    }

    next();
};


/**
 * 管家/后台系统管理员权限校验
 * 拦截所有发往 /api/v1/admin/* 的请求，确保调用者是最高权限者。
 */
const requireAdminRole = (req, res, next) => {
    if (!req.user) {
        return res.status(401).json({ error: 'Unauthorized', message: 'User not authenticated.' });
    }

    if (req.user.role !== 'ADMIN') {
        return res.status(403).json({
            error: 'Forbidden',
            code: 'ROLE_DENIED',
            message: '越权访问：此区域仅向后台系统管家开放。'
        });
    }

    next();
};

module.exports = {
    verifyToken,
    requireActiveStatus,
    requireAdminRole,
    JWT_SECRET
};
