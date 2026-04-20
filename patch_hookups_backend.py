import re

file_path = "Aurelian-Backend/src/routes/hookups.js"
with open(file_path, "r") as f:
    content = f.read()

new_routes = """
/**
 * 获取当前用户收到或发起的邀约通知列表
 */
router.get('/requests', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const type = req.query.type || 'RECEIVED'; // 'RECEIVED' 或 'SENT'
        const limit = parseInt(req.query.limit) || 20;

        const query = {};
        if (type === 'RECEIVED') {
            query.targetUserId = req.user.id;
        } else {
            query.senderId = req.user.id;
        }

        const requests = await HookupRequest.find(query)
            .sort({ createdAt: -1 })
            .limit(limit)
            // 根据类型 populate 对方的信息
            .populate(type === 'RECEIVED' ? 'senderId' : 'targetUserId', 'name avatarUrl bio location isVerified')
            .lean();

        // 格式化输出
        const data = requests.map(reqData => {
            const otherUser = type === 'RECEIVED' ? reqData.senderId : reqData.targetUserId;
            return {
                requestId: reqData._id.toString(),
                userId: otherUser._id.toString(),
                name: otherUser.name,
                avatarUrl: otherUser.avatarUrl,
                bio: otherUser.bio,
                location: otherUser.location,
                isVerified: otherUser.isVerified,
                meetingType: reqData.meetingType,
                note: reqData.note,
                safeMode: reqData.safeMode,
                status: reqData.status,
                createdAt: reqData.createdAt.getTime(),
                expiresAt: reqData.expiresAt.getTime()
            };
        });

        res.status(200).json({ data });
    } catch (error) {
        console.error('[HOOKUPS] 获取邀约列表失败:', error);
        res.status(500).json({ error: '无法获取邀约信箱内容' });
    }
});

const respondSchema = Joi.object({
    action: Joi.string().valid('ACCEPT', 'REJECT').required()
});

/**
 * 响应收到的邀约请求
 */
router.post('/requests/:id/respond', verifyToken, requireActiveStatus, async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const { id } = req.params;
        const { error, value } = respondSchema.validate(req.body);
        if (error) {
            await session.abortTransaction();
            return res.status(400).json({ error: error.details[0].message });
        }

        const { action } = value;
        const userId = req.user.id;

        // 1. 查找此请求，必须是发给当前用户的，且处于 PENDING 状态
        const hookupRequest = await HookupRequest.findOne({
            _id: id,
            targetUserId: userId,
            status: 'PENDING'
        }).session(session);

        if (!hookupRequest) {
            await session.abortTransaction();
            return res.status(404).json({ error: '该邀约不存在、已过期或已被处理。' });
        }

        if (action === 'REJECT') {
            hookupRequest.status = 'REJECTED';
            await hookupRequest.save({ session });
            await session.commitTransaction();
            return res.status(200).json({ success: true, message: '您已婉拒了该高定邀约。' });
        }

        // 2. 如果是 ACCEPT，需要创建一段私聊会话 Conversation
        hookupRequest.status = 'ACCEPTED';

        const Conversation = require('../models/Conversation');
        let conversation = await Conversation.findOne({
            participants: { $all: [userId, hookupRequest.senderId.toString()] }
        }).session(session);

        if (!conversation) {
            conversation = new Conversation({
                participants: [userId, hookupRequest.senderId.toString()],
                type: 'DIRECT',
                lastMessageText: '【邀约已接受】期待与您的相遇。'
            });
            await conversation.save({ session });
        }

        hookupRequest.conversationId = conversation._id;
        await hookupRequest.save({ session });

        await session.commitTransaction();

        res.status(200).json({
            success: true,
            message: '您已接受邀约，私密通道已为您开启。',
            data: {
                conversationId: conversation._id.toString()
            }
        });

    } catch (error) {
        await session.abortTransaction();
        console.error('[HOOKUPS] 响应邀约失败:', error);
        res.status(500).json({ error: '系统异常，无法回传您的心意。' });
    } finally {
        session.endSession();
    }
});

module.exports = router;"""

if "/requests/:id/respond" not in content:
    content = content.replace("module.exports = router;", new_routes)

with open(file_path, "w") as f:
    f.write(content)
