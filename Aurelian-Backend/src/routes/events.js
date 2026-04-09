const express = require('express');
const router = express.Router();
const Joi = require('joi');
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const Event = require('../models/Event');
const EventRSVP = require('../models/EventRSVP');

/**
 * 获取可报名的沙龙活动列表
 * 根据类型 (type) 或城市过滤，按日期升序排列 (越近的在前面)
 */
router.get('/', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const type = req.query.type; // 'SALON', 'PARTY', 等
        const city = req.query.city;

        const query = { status: { $in: ['UPCOMING', 'ONGOING'] } };
        if (type) query.type = type;
        if (city) query.city = new RegExp(city, 'i'); // 不区分大小写匹配

        // 仅查询尚未开始或正在进行中的活动
        // 确保 date > 现在，或者 status === 'ONGOING'
        const now = new Date();
        query.$or = [{ date: { $gte: now } }, { status: 'ONGOING' }];

        const events = await Event.find(query)
            .sort({ date: 1 })
            .limit(20)
            .lean();

        // 格式化输出
        const data = events.map(evt => ({
            eventId: evt._id.toString(),
            title: evt.title,
            date: evt.date.toISOString(), // 转换为 ISO 格式供前端使用
            location: evt.city + " · " + evt.location,
            coverUrl: evt.coverUrl || 'https://images.unsplash.com/photo-1519671482749-fd09be7ccebf?auto=format&fit=crop&w=800&q=80',
            type: evt.type,
            capacity: evt.capacity,
            enrolledCount: evt.enrolledCount
        }));

        res.status(200).json({ data });

    } catch (error) {
        console.error('[EVENTS] 获取沙龙列表失败:', error);
        res.status(500).json({ error: '无法获取活动清单，您的管家正在修复' });
    }
});

/**
 * 获取具体活动详情
 */
router.get('/:id', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const eventId = req.params.id;

        const event = await Event.findById(eventId).lean();
        if (!event) {
            return res.status(404).json({ error: '您访问的私密沙龙不存在。' });
        }

        // 查询当前用户是否已报名
        const rsvp = await EventRSVP.findOne({ userId: req.user.id, eventId: eventId }).lean();

        res.status(200).json({
            data: {
                eventId: event._id.toString(),
                title: event.title,
                date: event.date.toISOString(),
                location: event.city + " · " + event.location,
                description: event.description,
                attireProtocol: event.attireProtocol,
                coverUrl: event.coverUrl || 'https://images.unsplash.com/photo-1519671482749-fd09be7ccebf?auto=format&fit=crop&w=800&q=80',
                myRsvpStatus: rsvp ? rsvp.status : 'NONE', // 返回当前用户的报名状态
                capacity: event.capacity,
                enrolledCount: event.enrolledCount
            }
        });

    } catch (error) {
        console.error('[EVENTS] 获取活动详情失败:', error);
        res.status(500).json({ error: '查无此盛会' });
    }
});

// RSVP 参数校验
const rsvpSchema = Joi.object({
    partySize: Joi.number().integer().min(1).max(2).default(1),
    notes: Joi.string().max(200).allow('').optional()
});

/**
 * 报名/预约出席某个沙龙 (RSVP)
 * 此处应考虑高并发下的超卖问题，在正式生产环境可能需要分布式锁或更复杂的 MongoDB $inc 并发更新
 */
router.post('/:id/rsvp', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const eventId = req.params.id;
        const { error, value } = rsvpSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        const { partySize, notes } = value;

        // 1. 查找活动并检查名额与状态
        const event = await Event.findById(eventId);
        if (!event) {
            return res.status(404).json({ error: '沙龙已取消或不存在。' });
        }
        if (event.status !== 'UPCOMING') {
            return res.status(400).json({ error: '该活动当前不接受预约。' });
        }
        if (event.enrolledCount + partySize > event.capacity) {
            // 名额满了，可以考虑存为 'WAITLISTED'
            // return res.status(400).json({ error: '抱歉，本次沙龙席位已满，您可以联系管家进入候补名单。' });

            // 或者直接作为 WAITLISTED 保存
            const waitlistRsvp = new EventRSVP({
                userId: req.user.id,
                eventId: eventId,
                partySize: partySize,
                notes: notes,
                status: 'WAITLISTED'
            });
            await waitlistRsvp.save();
            return res.status(200).json({
                data: { status: 'WAITLISTED' },
                message: '尊贵的会员，该沙龙名额已满。您已自动加入专属候补名单。'
            });
        }

        // 2. 检查是否重复报名
        const existingRSVP = await EventRSVP.findOne({ userId: req.user.id, eventId: eventId });
        if (existingRSVP) {
            return res.status(400).json({ error: `您已经提交过预约，当前状态为: \${existingRSVP.status}` });
        }

        // 3. 执行报名并扣减名额 (使用 findOneAndUpdate 进行原子更新防止超卖)
        // 并发场景下，我们原子性地将 enrolledCount 增加 partySize，前提是增加后不能超过 capacity
        const updatedEvent = await Event.findOneAndUpdate(
            { _id: eventId, $expr: { $lte: [{ $add: ['$enrolledCount', partySize] }, '$capacity'] } },
            { $inc: { enrolledCount: partySize } },
            { new: true }
        );

        if (!updatedEvent) {
            // 如果并发导致名额超了，findOneAndUpdate 将返回 null
            return res.status(400).json({ error: '系统繁忙，由于其他会员抢先预定，名额已不足。' });
        }

        // 4. 创建成功报名记录
        const newRSVP = new EventRSVP({
            userId: req.user.id,
            eventId: eventId,
            partySize: partySize,
            notes: notes,
            status: 'PENDING' // 在高端圈层，通常还需要管家最终核实，所以可以先标为 PENDING，通过后变为 CONFIRMED
        });

        await newRSVP.save();

        res.status(201).json({
            data: { status: newRSVP.status },
            message: '您的赴宴请求已发送，管家将随后确认您的行程。'
        });

    } catch (error) {
        console.error('[EVENTS] RSVP 失败:', error);
        res.status(500).json({ error: '系统异常，管家正在处理' });
    }
});

module.exports = router;
