const mongoose = require('mongoose');

const InteractionSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  targetUserId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  videoId: { type: mongoose.Schema.Types.ObjectId, ref: 'Video' }, // 发生交互的视频内容

  // 交互类型：'LIKE' (点赞), 'PASS' (跳过), 'REPORT' (举报)
  type: { type: String, enum: ['LIKE', 'PASS', 'REPORT'], required: true },

  // 用于防抖去重
  createdAt: { type: Date, default: Date.now }
});

// 防止用户对同一个目标/视频重复发送相同类型的交互
InteractionSchema.index({ userId: 1, targetUserId: 1, type: 1 }, { unique: true });
InteractionSchema.index({ userId: 1, videoId: 1, type: 1 }, { unique: true });

module.exports = mongoose.model('Interaction', InteractionSchema);
