const mongoose = require('mongoose');

const HookupRequestSchema = new mongoose.Schema({
  // 发起人与受邀人
  senderId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  targetUserId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  // 约会意图：'DRINK' (小酌), 'DINNER' (晚宴), 'PARTY' (派对), 'CASUAL' (随性)
  meetingType: { type: String, enum: ['DRINK', 'DINNER', 'PARTY', 'CASUAL'], required: true },

  // 留言与安全模式
  note: { type: String, maxLength: 200, default: '' },
  safeMode: { type: Boolean, default: true },

  // 状态流转：'PENDING' (待确认), 'ACCEPTED' (已接受), 'REJECTED' (已拒绝), 'EXPIRED' (已过期)
  status: { type: String, enum: ['PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED'], default: 'PENDING' },

  // 邀约有效期 (比如两小时后自动过期)
  expiresAt: { type: Date, required: true },

  // 如果已接受，则跳转或生成对应的会话 ID
  conversationId: { type: mongoose.Schema.Types.ObjectId, ref: 'Conversation', default: null }
}, {
  timestamps: true // createdAt, updatedAt
});

// 防止对同一个人同时发送多条未决请求
HookupRequestSchema.index({ senderId: 1, targetUserId: 1, status: 1 });
HookupRequestSchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 }); // MongoDB TTL 自动清理过期数据

module.exports = mongoose.model('HookupRequest', HookupRequestSchema);
