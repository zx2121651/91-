import re

file_path = "Aurelian-Backend/src/routes/feed.js"
with open(file_path, "r") as f:
    content = f.read()

# Update GET /videos to support ?userId=...
old_logic = """        // 1. 查询当前用户看过的视频列表 (用于排重)
        // 注意：在大规模生产中这会使用 Redis Bloom Filter。这里使用 MongoDB $nin 演示核心思想。
        const viewedInteractions = await Interaction.find({ userId: req.user.id })
            .select('videoId')
            .lean();
        const viewedVideoIds = viewedInteractions.map(i => i.videoId).filter(id => id != null);

        // 2. 构造查询条件
        // 必须是已审核通过的公开视频
        const query = { status: 'APPROVED', _id: { $nin: viewedVideoIds } };"""

new_logic = """        const targetUserId = req.query.userId;
        let query = { status: 'APPROVED' };

        // 1. 根据是否指定了 userId 判断是全局信息流还是个人主页流
        if (targetUserId) {
            // 如果是查看特定用户的主页视频，不需要排除看过的，直接查
            const filterId = targetUserId === 'me' ? req.user.id : targetUserId;
            query.userId = filterId;
            // 对于自己看自己的，甚至可以把未过审的 PENDING_REVIEW 也包含进来
            if (targetUserId === 'me') {
                query.status = { $in: ['APPROVED', 'PENDING_REVIEW', 'REJECTED'] };
            }
        } else {
            // 2. 只有全局发现流需要查已读过滤
            const viewedInteractions = await Interaction.find({ userId: req.user.id })
                .select('videoId')
                .lean();
            const viewedVideoIds = viewedInteractions.map(i => i.videoId).filter(id => id != null);
            query._id = { $nin: viewedVideoIds };
        }"""

if "targetUserId = req.query.userId" not in content:
    content = content.replace(old_logic, new_logic)

with open(file_path, "w") as f:
    f.write(content)
