const mongoose = require('mongoose');

const MasqueradeAnswerSchema = new mongoose.Schema({
  // 谁在这个特定会话回答了问题
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  sessionId: { type: mongoose.Schema.Types.ObjectId, ref: 'MasqueradeSession', required: true },

  // 回答内容
  answerText: { type: String, required: true, maxLength: 500, trim: true },

  // 后台搓合状态：'PENDING' (未撮合), 'MATCHED' (已匹配出有缘人), 'UNMATCHED' (落单)
  matchStatus: { type: String, enum: ['PENDING', 'MATCHED', 'UNMATCHED'], default: 'PENDING' },

  // 如果匹配成功，这是与他/她相匹配的另一个人提交的回答ID
  matchedWithId: { type: mongoose.Schema.Types.ObjectId, ref: 'MasqueradeAnswer', default: null }
}, {
  timestamps: true
});

// 每个会话只能提交一次答案
MasqueradeAnswerSchema.index({ userId: 1, sessionId: 1 }, { unique: true });
MasqueradeAnswerSchema.index({ sessionId: 1, matchStatus: 1 });

module.exports = mongoose.model('MasqueradeAnswer', MasqueradeAnswerSchema);
