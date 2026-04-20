const mongoose = require('mongoose');

const ConversationSchema = new mongoose.Schema({
  // 会话参与者 (一般是两个人，且按照字典序存放)
  participants: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],

  // 会话类型：'DIRECT' (单聊), 'GROUP' (群聊/沙龙派对讨论)
  type: { type: String, enum: ['DIRECT', 'GROUP'], default: 'DIRECT' },

  // 最新一条消息的冗余缓存，优化会话列表展示速度
  lastMessageText: { type: String, default: null },
  lastMessageTimestamp: { type: Date, default: null },
  lastSenderId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', default: null },

  // 未读数 (Map: participantId -> unreadCount)
  // 为了查询方便，可以改用嵌套对象或子文档。Mongoose Map 也可用
  unreadCounts: { type: Map, of: Number, default: {} },

  // 是否是被屏蔽的会话
  isArchived: { type: Boolean, default: false }
}, {
  timestamps: true // updatedAt 用于会话列表排序
});

ConversationSchema.index({ participants: 1 });
ConversationSchema.index({ updatedAt: -1 });

module.exports = mongoose.model('Conversation', ConversationSchema);
