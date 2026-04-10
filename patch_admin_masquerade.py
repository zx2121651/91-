import re

file_path = "Aurelian-Backend/src/routes/admin.js"
with open(file_path, "r") as f:
    content = f.read()

masquerade_routes = """
// ==========================================
// 4. 盲盒与假面舞会运营 (Masquerade Management)
// ==========================================

const createSessionSchema = Joi.object({
    sessionDate: Joi.date().iso().required(),
    question: Joi.string().required(),
    startTime: Joi.date().iso().required(),
    endTime: Joi.date().iso().required()
});

/**
 * 筹办今晚或未来的假面舞会 (Masquerade Session)
 */
router.post('/masquerade/sessions', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { error, value } = createSessionSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const newSession = new MasqueradeSession({
            ...value,
            status: 'SCHEDULED' // 根据 startTime，系统自动或后台手动变更为 ACTIVE
        });

        // 实际场景下，可以使用定时任务 (node-cron) 来自动切换 SCHEDULED 到 ACTIVE
        // 这里提供基础的入库支持
        await newSession.save();

        res.status(201).json({
            success: true,
            message: '今晚的灵魂拷问已设下。',
            data: newSession
        });

    } catch (error) {
        if (error.code === 11000) {
            return res.status(400).json({ error: '该日期已存在规划好的舞会，请勿重复添加。' });
        }
        res.status(500).json({ error: '开启舞会失败。' });
    }
});

/**
 * 获取一场假面舞会的所有回答 (用于离线灵魂匹配)
 */
router.get('/masquerade/sessions/:id/answers', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { id } = req.params;
        const matchStatus = req.query.matchStatus; // PENDING, MATCHED, UNMATCHED

        const query = { sessionId: id };
        if (matchStatus) query.matchStatus = matchStatus;

        const answers = await require('../models/MasqueradeAnswer').find(query)
            .populate('userId', 'name location minAgePreference')
            .lean();

        res.status(200).json({
            data: answers,
            meta: {
                total: answers.length,
                pendingCount: answers.filter(a => a.matchStatus === 'PENDING').length
            }
        });

    } catch (error) {
        res.status(500).json({ error: '无法获取假面舞会的回声。' });
    }
});

module.exports = router;"""

if "/masquerade/sessions" not in content:
    content = content.replace("module.exports = router;", masquerade_routes)

with open(file_path, "w") as f:
    f.write(content)
