import re

file_path = "Aurelian-Backend/src/routes/admin.js"
with open(file_path, "r") as f:
    content = f.read()

events_routes = """
// ==========================================
// 3. 沙龙活动与高端运营 (Events Management)
// ==========================================

const createEventSchema = Joi.object({
    title: Joi.string().max(100).required(),
    description: Joi.string().required(),
    type: Joi.string().valid('SALON', 'PARTY', 'TASTING', 'EXHIBITION').default('SALON'),
    city: Joi.string().required(),
    location: Joi.string().required(),
    date: Joi.date().iso().required(),
    capacity: Joi.number().integer().min(1).required(),
    attireProtocol: Joi.string().default('Black Tie / Evening Gown')
});

/**
 * 发布新的高定沙龙或派对
 */
router.post('/events', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { error, value } = createEventSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const newEvent = new Event({
            ...value,
            status: 'UPCOMING'
        });

        await newEvent.save();

        res.status(201).json({
            success: true,
            message: '盛宴已成功筹备并发布。',
            data: newEvent
        });

    } catch (error) {
        console.error('[ADMIN] 发布活动失败:', error);
        res.status(500).json({ error: '管家筹备活动时遇到阻碍。' });
    }
});

/**
 * 获取某场活动的嘉宾名单 (RSVP 列表)
 */
router.get('/events/:id/rsvps', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { id } = req.params;
        const status = req.query.status; // 可选过滤 PENDING 或 WAITLISTED

        const query = { eventId: id };
        if (status) query.status = status;

        const rsvps = await require('../models/EventRSVP').find(query)
            .populate('userId', 'name phone membershipTier isVerified trustScore followersCount')
            .sort({ createdAt: 1 })
            .lean();

        res.status(200).json({ data: rsvps });

    } catch (error) {
        res.status(500).json({ error: '无法获取席位名单。' });
    }
});

const reviewRsvpSchema = Joi.object({
    action: Joi.string().valid('CONFIRM', 'REJECT').required(),
    reason: Joi.string().max(200).allow('').optional()
});

/**
 * 人工确认或拒绝嘉宾的出席申请 (例如从 PENDING/WAITLISTED 变 CONFIRMED)
 */
router.post('/events/rsvps/:rsvpId/review', verifyToken, requireAdminRole, async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const { rsvpId } = req.params;
        const { error, value } = reviewRsvpSchema.validate(req.body);
        if (error) {
            await session.abortTransaction();
            return res.status(400).json({ error: error.details[0].message });
        }

        const { action, reason } = value;

        const rsvp = await require('../models/EventRSVP').findById(rsvpId).session(session);
        if (!rsvp) {
            await session.abortTransaction();
            return res.status(404).json({ error: '未找到该 RSVP 记录。' });
        }

        if (rsvp.status === 'CONFIRMED' && action === 'CONFIRM') {
            await session.abortTransaction();
            return res.status(400).json({ error: '该席位已被确认过。' });
        }

        // 如果是把候补/待定转为确认，需要考虑容量 (在并发不高的情况下可以如此检查)
        if (action === 'CONFIRM') {
            const event = await Event.findById(rsvp.eventId).session(session);
            // 只有当之前没算进 enrolledCount 的时候才扣减
            if (rsvp.status !== 'PENDING' && rsvp.status !== 'WAITLISTED') {
                 // 其他奇怪状态暂不处理
            } else {
                 if (event.enrolledCount + rsvp.partySize > event.capacity) {
                     await session.abortTransaction();
                     return res.status(400).json({ error: '该沙龙的贵宾席位已满，无法通过更多申请。' });
                 }
                 // WAITLISTED 转为 CONFIRMED，需要增加 enrolledCount
                 event.enrolledCount += rsvp.partySize;
                 await event.save({ session });
            }
            rsvp.status = 'CONFIRMED';
        } else if (action === 'REJECT') {
            // 如果原本是 PENDING（已经占用了 enrolledCount），驳回时要释放名额
            if (rsvp.status === 'PENDING') {
                 const event = await Event.findById(rsvp.eventId).session(session);
                 event.enrolledCount = Math.max(0, event.enrolledCount - rsvp.partySize);
                 await event.save({ session });
            }
            rsvp.status = 'CANCELLED';
            rsvp.reason = reason || '由于场地受限，本次无法为您保留席位。';
        }

        await rsvp.save({ session });
        await session.commitTransaction();

        res.status(200).json({ success: true, message: `已成功${action === 'CONFIRM' ? '确认' : '拒绝'}该嘉宾的出席。` });

    } catch (error) {
        await session.abortTransaction();
        console.error('[ADMIN] 处理 RSVP 失败:', error);
        res.status(500).json({ error: '排座系统发生异常。' });
    } finally {
        session.endSession();
    }
});

module.exports = router;"""

if "/events/:id/rsvps" not in content:
    content = content.replace("module.exports = router;", events_routes)

with open(file_path, "w") as f:
    f.write(content)
