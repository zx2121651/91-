const mongoose = require('mongoose');

const CommentSchema = new mongoose.Schema({
  // 评论归属的视频与作者
  videoId: { type: mongoose.Schema.Types.ObjectId, ref: 'Video', required: true },
  authorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  // 评论内容
  content: { type: String, required: true, maxLength: 500, trim: true },

  // 嵌套评论与盖楼回复
  parentCommentId: { type: mongoose.Schema.Types.ObjectId, ref: 'Comment', default: null },
  replyToUserId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', default: null }, // 被回复的用户

  // 点赞等辅助属性
  likesCount: { type: Number, default: 0 },

  // 审核风控状态 (如涉黄涉暴可被隐藏)
  status: { type: String, enum: ['VISIBLE', 'HIDDEN', 'DELETED'], default: 'VISIBLE' },

}, {
  timestamps: true
});

// 信息流评论按时间升序展示 (最旧的在前面) 或热度展示
CommentSchema.index({ videoId: 1, createdAt: 1 });
CommentSchema.index({ videoId: 1, likesCount: -1 });

module.exports = mongoose.model('Comment', CommentSchema);
