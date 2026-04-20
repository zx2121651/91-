const fs = require('fs');

file_path = "Aurelian-Backend/src/routes/feed.js";
with open(file_path, "r") as f:
    content = f.read()

# Add Comment model require
if "const Comment = require('../models/Comment');" not in content:
    content = content.replace("const Interaction = require('../models/Interaction');", "const Interaction = require('../models/Interaction');\nconst Comment = require('../models/Comment');")

# Add the GET /videos/:id/comments route
get_comments = """
/**
 * 获取某个高定瞬间的所有私密讨论
 * 按时间倒序，支持分页
 */
router.get('/videos/:id/comments', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { id } = req.params;
        const limit = parseInt(req.query.limit) || 20;
        const cursor = req.query.cursor;

        const query = { videoId: id, status: 'VISIBLE' };
        if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
            query._id = { $lt: cursor };
        }

        const comments = await Comment.find(query)
            .sort({ _id: -1 })
            .limit(limit)
            .populate('authorId', 'name avatarUrl')
            .lean();

        const data = comments.map(c => ({
            commentId: c._id.toString(),
            content: c.content,
            authorName: c.authorId.name,
            authorAvatar: c.authorId.avatarUrl,
            likesCount: c.likesCount,
            createdAt: c.createdAt
        }));

        res.status(200).json({
            data,
            nextCursor: comments.length === limit ? comments[comments.length - 1]._id.toString() : null
        });
    } catch (error) {
        console.error('[COMMENTS] 获取评论失败:', error);
        res.status(500).json({ error: '无法获取私密讨论' });
    }
});

/**
 * 在高端瞬间留下私密讨论
 */
router.post('/videos/:id/comments', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const { id } = req.params;
        const { content } = req.body;

        if (!content || content.trim().length === 0) {
            return res.status(400).json({ error: '您的留言不能为空。' });
        }

        const newComment = new Comment({
            videoId: id,
            authorId: req.user.id,
            content: content.trim()
        });

        await newComment.save();
        await Video.findByIdAndUpdate(id, { $inc: { commentsCount: 1 } });

        res.status(201).json({
            success: true,
            message: '您的品位已记录在案。',
            commentId: newComment._id
        });
    } catch (error) {
        console.error('[COMMENTS] 发表讨论失败:', error);
        res.status(500).json({ error: '发表失败，请稍后重试。' });
    }
});

module.exports = router;"""

if "/videos/:id/comments" not in content:
    content = content.replace("module.exports = router;", get_comments)

with open(file_path, "w") as f:
    f.write(content)
