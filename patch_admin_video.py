import re

file_path = "Aurelian-Backend/src/routes/admin.js"
with open(file_path, "r") as f:
    content = f.read()

# Add the new admin video moderation routes
new_routes = """
// ==========================================
// 2. 内容安全与视频审核 (Content Moderation)
// ==========================================

/**
 * 获取待审或被举报的短视频列表
 * 管家团队需在此审核视频的着装、内容是否符合高定社区规范。
 */
router.get('/videos/pending', verifyToken, requireAdminRole, async (req, res) => {
    try {
        const { page = 1, limit = 10 } = req.query;
        const skip = (parseInt(page) - 1) * parseInt(limit);

        // 查找处于审核状态的视频
        const videos = await Video.find({ status: 'PENDING_REVIEW' })
            .sort({ createdAt: 1 }) // 按照提交流水线排序，先审早提交的
            .skip(skip)
            .limit(parseInt(limit))
            .populate('userId', 'name avatarUrl status membershipTier')
            .lean();

        const total = await Video.countDocuments({ status: 'PENDING_REVIEW' });

        res.status(200).json({
            data: videos,
            meta: {
                total,
                page: parseInt(page),
                pages: Math.ceil(total / limit)
            }
        });

    } catch (error) {
        console.error('[ADMIN] 获取待审视频失败:', error);
        res.status(500).json({ error: '读取审核池失败。' });
    }
});

const moderateVideoSchema = Joi.object({
    action: Joi.string().valid('APPROVE', 'REJECT', 'BAN_USER').required(),
    reason: Joi.string().max(300).allow('').optional()
});

/**
 * 执行视频内容安全裁决
 * 可以是通过、驳回下架、甚至直接封禁发布者。
 */
router.post('/videos/:id/moderate', verifyToken, requireAdminRole, async (req, res) => {
    const session = await mongoose.startSession();
    session.startTransaction();

    try {
        const { id } = req.params;
        const { error, value } = moderateVideoSchema.validate(req.body);
        if (error) {
            await session.abortTransaction();
            return res.status(400).json({ error: error.details[0].message });
        }

        const { action, reason } = value;

        // 1. 查找目标视频
        const video = await Video.findById(id).session(session);
        if (!video) {
            await session.abortTransaction();
            return res.status(404).json({ error: '该视频记录不存在。' });
        }

        // 2. 根据裁决更新视频与用户状态
        if (action === 'APPROVE') {
            video.status = 'APPROVED';
            // 审批通过的瞬间可以增加一条管家内部记录
        } else if (action === 'REJECT') {
            video.status = 'REJECTED';
            video.bio = `[此内容因违反高定社区规范已被折叠。原因: ${reason || '不符合圈层调性'}]`;
        } else if (action === 'BAN_USER') {
            video.status = 'REJECTED';

            // 连带封禁账号，清除害群之马
            await User.findByIdAndUpdate(
                video.userId,
                { $set: { status: 'BANNED' } },
                { session }
            );
        }

        await video.save({ session });
        await session.commitTransaction();

        res.status(200).json({
            success: true,
            message: `管家审核完成：视频已${action === 'APPROVE' ? '上架发布' : '下架处理'}。`,
            data: {
                videoId: video._id,
                newStatus: video.status
            }
        });

    } catch (error) {
        await session.abortTransaction();
        console.error('[ADMIN] 视频安全审核失败:', error);
        res.status(500).json({ error: '处理审核时系统遭遇异常。' });
    } finally {
        session.endSession();
    }
});

module.exports = router;"""

if "/videos/pending" not in content:
    content = content.replace("module.exports = router;", new_routes)

with open(file_path, "w") as f:
    f.write(content)
