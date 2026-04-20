import re

file_path = "Aurelian-Backend/src/middleware/auth.middleware.js"
with open(file_path, "r") as f:
    content = f.read()

# Add requireAdminRole middleware
new_middleware = """
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
};"""

if "requireAdminRole" not in content:
    content = content.replace("module.exports = {\n    verifyToken,\n    requireActiveStatus,\n    JWT_SECRET\n};", new_middleware)

with open(file_path, "w") as f:
    f.write(content)
