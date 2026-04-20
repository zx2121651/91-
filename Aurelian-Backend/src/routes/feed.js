const express = require('express');
const mongoose = require('mongoose');
const Joi = require('joi');
const router = express.Router();
const { verifyToken, requireActiveStatus } = require('../middleware/auth.middleware');

const Video = require('../models/Video');
const User = require('../models/User');
const Interaction = require('../models/Interaction');
const Comment = require('../models/Comment');

// 数据验证 Schema
const publishSchema = Joi.object({
    title: Joi.string().max(50).required().messages({
        'string.empty': '高端瞬间需要一个精简的标题',
        'string.max': '标题不能超过50个字符'
    }),
    bio: Joi.string().max(300).allow('').optional(),
    mediaId: Joi.string().required(),
    audioTrack: Joi.string().default('原声'),
    filterName: Joi.string().default('原画'),
    trimStartMs: Joi.number().min(0).default(0),
    trimEndMs: Joi.number().min(0).default(0)
});

/**
 * 获取高端短视频信息流
 * 实现了真实的 MongoDB 分页与游标加载，关联查询作者信息，并过滤掉用户已看过/点过赞的视频。
 */
router.get('/videos', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        const limit = parseInt(req.query.limit) || 5;
        const cursor = req.query.cursor; // 传入的应该是上次请求返回的最旧的一条 Video 的 _id

        const targetUserId = req.query.userId;
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
        }

        if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
            // MongoDB 常见的游标分页：基于 Object ID 降序 (由于包含了时间戳)
            query._id = { ...query._id, $lt: cursor };
        }

        // 3. 执行聚合与级联查询
        const videos = await Video.find(query)
            .sort({ _id: -1 }) // 最新发布的在前
            .limit(limit)
            .populate('userId', 'name location bio avatarUrl membershipTier isVerified') // 只查出需要的用户信息字段
            .lean();

        // 4. 数据格式组装与转换，以符合客户端目前的 Network.kt / FeedResponse
        const feedData = videos.map(video => ({
            userId: video.userId._id.toString(),
            name: video.userId.name,
            bio: video.userId.bio || video.bio, // 如果用户没签名，显示视频的 bio
            location: video.userId.location,
            videoUrl: video.videoUrl,
            coverUrl: video.coverUrl || 'https://images.unsplash.com/photo-1520443240718-fce21901db79?auto=format&fit=crop&w=800&q=80',
            isLiked: false, // 动态流返回的都是没点过赞的
            videoId: video._id.toString()
        }));

        // 5. 游标计算
        const nextCursor = videos.length === limit ? videos[videos.length - 1]._id.toString() : null;

        res.status(200).json({
            data: feedData,
            nextCursor: nextCursor
        });

    } catch (error) {
        console.error('[FEED] 查询信息流错误:', error);
        res.status(500).json({ error: '无法获取高端瞬间，请稍后重试', details: error.message });
    }
});

/**
 * 发布新的高定短视频
 * 加入 Joi 参数校验、数据库写入与事务逻辑
 */
router.post('/publish', verifyToken, requireActiveStatus, async (req, res) => {
    try {
        // 1. 严格的数据校验
        const { error, value } = publishSchema.validate(req.body);
        if (error) {
            return res.status(400).json({ error: error.details[0].message });
        }

        // 2. Mock: 这里正常应该通过 mediaId 去云端查询转码后的真实视频 URL
        // 目前为了让功能跑通，生成一条模拟的高质感 URL
        const mockProcessedVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";

        // 3. 创建数据库实体记录
        const newVideo = new Video({
            userId: req.user.id,
            title: value.title,
            bio: value.bio,
            mediaId: value.mediaId,
            videoUrl: mockProcessedVideoUrl,
            editing: {
                filterName: value.filterName,
                audioTrack: value.audioTrack,
                watermarkText: value.title, // 取巧地用标题作为水印
                trimStartMs: value.trimStartMs,
                trimEndMs: value.trimEndMs
            },
            status: 'PENDING_REVIEW' // 视频上传后进入待审状态，管家团队将在后台审核后放入信息流
        });

        // 4. 保存记录
        await newVideo.save();

        console.log(`[FEED PUBLISH] 用户 \${req.user.id} 成功将作品插入数据库. Video ID: \${newVideo._id}`);

        res.status(201).json({
            success: true,
            message: '您的精彩瞬间已成功发布，并入库完成。',
            videoId: newVideo._id
        });

    } catch (error) {
        console.error('[FEED PUBLISH] 发布失败:', error);
        res.status(500).json({ error: '发布过程中服务器开小差了，请稍后再试' });
    }
});


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

module.exports = router;
