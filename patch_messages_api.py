import re

file_path = "Aurelian-Backend/src/routes/messages.js"
with open(file_path, "r") as f:
    content = f.read()

# Update sendMessageSchema
old_schema = """const sendMessageSchema = Joi.object({
    convId: Joi.string().required(),
    content: Joi.string().max(2000).required().messages({
        'string.empty': '私密消息内容不能空白。'
    }),
    type: Joi.string().valid('TEXT', 'IMAGE', 'VIDEO').default('TEXT'),
    isEphemeral: Joi.boolean().default(false)
});"""

new_schema = """const sendMessageSchema = Joi.object({
    convId: Joi.string().required(),
    content: Joi.string().max(2000).required().messages({
        'string.empty': '私密消息内容不能空白。'
    }),
    type: Joi.string().valid('TEXT', 'IMAGE', 'VIDEO').default('TEXT'),
    isEphemeral: Joi.boolean().default(false),
    ephemeralDurationSeconds: Joi.number().min(1).max(60).default(5) // 默认为 5 秒阅后即焚
});"""

if "ephemeralDurationSeconds" not in content:
    content = content.replace(old_schema, new_schema)


# Update new message creation
old_msg = """        const newMessage = new Message({
            conversationId: convId,
            senderId: senderId,
            type: type,
            content: content,
            isEphemeral: isEphemeral
        });"""

new_msg = """        const { ephemeralDurationSeconds } = value;
        const newMessage = new Message({
            conversationId: convId,
            senderId: senderId,
            type: type,
            content: content,
            isEphemeral: isEphemeral,
            ephemeralDurationSeconds: isEphemeral ? ephemeralDurationSeconds : 0
        });"""

if "ephemeralDurationSeconds:" not in content:
    content = content.replace(old_msg, new_msg)


# Update GET /conversations/:id/messages response to include readAt and expiresAt
# Filter out messages where expiresAt is less than now (Double check since TTL thread runs every 60s)
old_query = """        const query = { conversationId: convId, status: { $ne: 'RECALLED' } };
        if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
            query._id = { $lt: cursor };
        }

        const messages = await Message.find(query)
            .sort({ _id: -1 }) // 降序：最新在前面，前端反转显示
            .limit(limit)
            .populate('senderId', 'name avatarUrl')
            .lean();"""

new_query = """        const now = new Date();
        const query = {
            conversationId: convId,
            status: { $ne: 'RECALLED' },
            // 兜底防御：由于 MongoDB TTL 引擎通常有 60s 左右的扫描延迟，
            // 必须在应用层强行过滤出 expiresAt 已经超时的残余记录。
            $or: [
                { expiresAt: null },
                { expiresAt: { $gt: now } }
            ]
        };
        if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
            query._id = { ...query._id, $lt: cursor };
        }

        const messages = await Message.find(query)
            .sort({ _id: -1 }) // 降序：最新在前面，前端反转显示
            .limit(limit)
            .populate('senderId', 'name avatarUrl')
            .lean();"""

if "$or: [" not in content:
    content = content.replace(old_query, new_query)


old_format = """        const formattedMessages = messages.map(msg => ({
            msgId: msg._id.toString(),
            senderId: msg.senderId._id.toString(),
            senderName: msg.senderId.name,
            content: msg.isEphemeral && msg.status === 'READ' ? '[阅后即焚内容已销毁]' : msg.content,
            type: msg.type,
            timestamp: msg.createdAt,
            status: msg.status,
            isMe: msg.senderId._id.toString() === userId
        }));"""

new_format = """        const formattedMessages = messages.map(msg => ({
            msgId: msg._id.toString(),
            senderId: msg.senderId._id.toString(),
            senderName: msg.senderId.name,
            content: msg.content,
            type: msg.type,
            timestamp: msg.createdAt,
            status: msg.status,
            isMe: msg.senderId._id.toString() === userId,
            isEphemeral: msg.isEphemeral,
            ephemeralDurationSeconds: msg.ephemeralDurationSeconds,
            readAt: msg.readAt,
            expiresAt: msg.expiresAt
        }));"""

if "msg.ephemeralDurationSeconds" not in content:
    content = content.replace(old_format, new_format)

# Add POST /messages/:id/read route
read_route = """
/**
 * 原子化更新：将消息标记为已读，并为阅后即焚启动物理死亡倒计时 (Burn-out Sequence)
 */
router.post('/:msgId/read', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { msgId } = req.params;
        const userId = req.user.id;

        const message = await Message.findById(msgId);
        if (!message) {
            return res.status(404).json({ error: '信件不存在或已被彻底销毁。' });
        }

        // 不能把自己的消息标记为已读（或者不需要倒计时，取决于产品设计，这里只允许接收方触发）
        if (message.senderId.toString() === userId) {
            return res.status(200).json({ success: true, message: '无需对自己点火。' });
        }

        // 如果是已被读过的，则直接返回
        if (message.status === 'READ' || message.readAt) {
            return res.status(200).json({
                success: true,
                data: { expiresAt: message.expiresAt }
            });
        }

        const now = new Date();
        const updateDoc = {
            status: 'READ',
            readAt: now
        };

        if (message.isEphemeral) {
            // 精确计算物理死亡时间戳：当前时间 + 焚毁倒数时长
            const durationMs = message.ephemeralDurationSeconds * 1000;
            updateDoc.expiresAt = new Date(now.getTime() + durationMs);
            console.log(`[BURN-OUT ENGINE] 消息 \${msgId} 触发已读。将于 \${updateDoc.expiresAt.toISOString()} 销毁。`);
        }

        const updatedMsg = await Message.findByIdAndUpdate(
            msgId,
            { $set: updateDoc },
            { new: true }
        );

        res.status(200).json({
            success: true,
            data: {
                readAt: updatedMsg.readAt,
                expiresAt: updatedMsg.expiresAt
            }
        });

    } catch (error) {
        console.error('[MESSAGES] 触发已读失败:', error);
        res.status(500).json({ error: '点火装置异常' });
    }
});
"""

if "/:msgId/read" not in content:
    content = content.replace("module.exports = router;", read_route + "\nmodule.exports = router;")


with open(file_path, "w") as f:
    f.write(content)
