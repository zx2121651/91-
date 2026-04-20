import re

file_path = "Aurelian-Backend/src/routes/admin.js"
with open(file_path, "r") as f:
    content = f.read()

# Add invite code management and dashboard stats routes
invite_dashboard_routes = """
// ==========================================
// 5. 邀请码分发与平台大盘数据 (Referrals & Dashboard)
// ==========================================

const createInviteSchema = Joi.object({
    codePrefix: Joi.string().max(10).uppercase().optional(), // 允许定制如 "VIP", "BLACK" 等前缀
    usageLimit: Joi.number().integer().min(1).max(10000).default(1),
    expiresInDays: Joi.number().integer().min(1).optional()
});

/**
 * 管家团队手动生成超级邀请码
 */
router.post('/invite-codes', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { error, value } = createInviteSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const { codePrefix, usageLimit, expiresInDays } = value;

        // 组装独一无二的高定短码
        const prefix = codePrefix ? `${codePrefix}-` : 'AURELIAN-';
        const randomSuffix = Math.random().toString(36).substring(2, 8).toUpperCase();
        const code = `${prefix}${randomSuffix}`;

        let expiresAt = null;
        if (expiresInDays) {
            expiresAt = new Date(Date.now() + expiresInDays * 24 * 60 * 60 * 1000);
        }

        const newInviteCode = new InviteCode({
            code: code,
            creatorId: req.user.id, // 这里代表是某位管家生成的
            usageLimit: usageLimit,
            usageCount: 0,
            expiresAt: expiresAt,
            status: 'ACTIVE'
        });

        await newInviteCode.save();

        res.status(201).json({
            success: true,
            message: '特殊的邀请通道已建立。',
            data: {
                code: newInviteCode.code,
                usageLimit: newInviteCode.usageLimit,
                expiresAt: newInviteCode.expiresAt
            }
        });

    } catch (error) {
        console.error('[ADMIN] 生成邀请码失败:', error);
        res.status(500).json({ error: '生成邀请码失败，请联系技术人员。' });
    }
});

/**
 * 监控平台邀请码的裂变与使用情况
 */
router.get('/invite-codes', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { page = 1, limit = 20, status } = req.query;
        const skip = (parseInt(page) - 1) * parseInt(limit);

        const query = {};
        if (status) query.status = status;

        const codes = await InviteCode.find(query)
            .sort({ createdAt: -1 })
            .skip(skip)
            .limit(parseInt(limit))
            .populate('creatorId', 'name role') // 查看是哪个管家或高级会员生成的
            .lean();

        const total = await InviteCode.countDocuments(query);

        res.status(200).json({
            data: codes,
            meta: {
                total,
                page: parseInt(page),
                pages: Math.ceil(total / limit)
            }
        });

    } catch (error) {
        console.error('[ADMIN] 获取邀请码列表失败:', error);
        res.status(500).json({ error: '读取引荐数据失败。' });
    }
});

/**
 * 平台核心大盘数据 (Dashboard Stats)
 * 汇总各种待处理的积压任务，以及关键资产增长情况
 */
router.get('/dashboard/stats', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const Match = require('../models/Match');

        // 使用 Promise.all 并发获取多张核心表的聚合数据
        const [
            totalUsers,
            activeUsers,
            pendingVettings,
            totalVideos,
            pendingVideos,
            totalMatches
        ] = await Promise.all([
            User.countDocuments({}), // 注册总人数
            User.countDocuments({ status: 'ACTIVE' }), // 尊贵的已认证会员数
            VerificationRequest.countDocuments({ status: 'PENDING' }), // 待审核的认证申请 (待办)
            Video.countDocuments({ status: 'APPROVED' }), // 流通中的作品总量
            Video.countDocuments({ status: 'PENDING_REVIEW' }), // 待人工安全审核的视频 (待办)
            Match.countDocuments({ status: 'ACTIVE' }) // 缔结成功的有效关系对数
        ]);

        res.status(200).json({
            success: true,
            data: {
                users: {
                    total: totalUsers,
                    active: activeUsers,
                    pendingRatio: totalUsers > 0 ? ((totalUsers - activeUsers) / totalUsers * 100).toFixed(1) + '%' : '0%'
                },
                pendingTasks: {
                    vettings: pendingVettings,
                    videosToModerate: pendingVideos
                },
                content: {
                    approvedVideos: totalVideos,
                    activeMatches: totalMatches
                }
            },
            message: '大盘概览拉取成功，这是目前高定社区的生命力跳动。'
        });

    } catch (error) {
        console.error('[ADMIN] 获取大盘数据失败:', error);
        res.status(500).json({ error: '核心系统无法汇总数据，请检查 MongoDB 负载。' });
    }
});

module.exports = router;"""

if "/dashboard/stats" not in content:
    # Remove the placeholder comments at the end and insert new routes
    content = content.replace(
        "// 其他预留的后台功能...\n// router.get('/dashboard', verifyToken, requireAdminRole, async (req, res) => { ... });\n// router.post('/videos/:id/moderate', verifyToken, requireAdminRole, async (req, res) => { ... });\n\nmodule.exports = router;",
        invite_dashboard_routes
    )

with open(file_path, "w") as f:
    f.write(content)
