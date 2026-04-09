const mongoose = require('mongoose');

const MessageSchema = new mongoose.Schema({
  // 所属会话 ID
  conversationId: { type: mongoose.Schema.Types.ObjectId, ref: 'Conversation', required: true },

  // 发送者
  senderId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  // 消息类型：'TEXT' (文字), 'IMAGE' (照片), 'VIDEO' (视频), 'AUDIO' (语音), 'LOCATION' (位置)
  type: { type: String, enum: ['TEXT', 'IMAGE', 'VIDEO', 'AUDIO', 'LOCATION'], default: 'TEXT' },

  // 消息内容 (可能是文本，也可能是带签名的媒体 URL)
  content: { type: String, required: true },

  // 阅后即焚标记 (Burn after reading)
  isEphemeral: { type: Boolean, default: false },
  ephemeralDurationSeconds: { type: Number, default: 0 },

  // 消息状态：'SENT' (已发送), 'DELIVERED' (已送达), 'READ' (已读), 'RECALLED' (已撤回)
  status: { type: String, enum: ['SENT', 'DELIVERED', 'READ', 'RECALLED'], default: 'SENT' },

  // 预留的富媒体尺寸、缩略图等辅助信息
  mediaMeta: {
    width: Number,
    height: Number,
    thumbnailUrl: String
  }
}, {
  timestamps: true // createdAt 是排序核心
});

// 单聊消息列表通常根据会话ID和创建时间游标查询
MessageSchema.index({ conversationId: 1, createdAt: -1 });

module.exports = mongoose.model('Message', MessageSchema);
