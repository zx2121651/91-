const mongoose = require('mongoose');

const VideoSchema = new mongoose.Schema({
  // 关联发布者
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  // 核心资源
  videoUrl: { type: String, required: true }, // 转码后的播放链接 (HLS/DASH 或 MP4)
  coverUrl: { type: String, default: '' },    // 高清封面图

  // 原数据与媒体属性
  mediaId: { type: String, required: true, unique: true }, // S3/OSS 上传标识
  durationMs: { type: Number, default: 0 },
  fileSize: { type: Number, default: 0 },

  // 剪辑参数 (保存用户的高级编辑配置)
  editing: {
    filterName: { type: String, default: '原画' },
    audioTrack: { type: String, default: '原声' },
    watermarkText: { type: String, default: '' },
    trimStartMs: { type: Number, default: 0 },
    trimEndMs: { type: Number, default: 0 }
  },

  // 内容文案
  title: { type: String, required: true, maxLength: 50 },
  bio: { type: String, default: '', maxLength: 300 }, // 视频的描述/心情

  // 安全与审核状态：'PROCESSING' (转码中), 'PENDING_REVIEW' (待审), 'APPROVED', 'REJECTED'
  status: { type: String, enum: ['PROCESSING', 'PENDING_REVIEW', 'APPROVED', 'REJECTED'], default: 'PROCESSING' },

  // 统计数据
  likesCount: { type: Number, default: 0 },
  commentsCount: { type: Number, default: 0 },
  viewsCount: { type: Number, default: 0 },
  sharesCount: { type: Number, default: 0 },

}, {
  timestamps: true
});

// 信息流推荐查询核心索引
VideoSchema.index({ status: 1, createdAt: -1 });
VideoSchema.index({ userId: 1, status: 1 });
VideoSchema.index({ likesCount: -1 });

module.exports = mongoose.model('Video', VideoSchema);
