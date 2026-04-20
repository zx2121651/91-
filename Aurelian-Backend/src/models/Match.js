const mongoose = require('mongoose');

const MatchSchema = new mongoose.Schema({
  // 匹配双方 (确保按字母序或对象 ID 升序排列存放，防止重复 (A->B) 和 (B->A) 生成两遍记录)
  users: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],

  // 匹配来源：'FEED_LIKE' (信息流点赞), 'MASQUERADE' (盲盒活动), 'HOOKUP' (速约)
  source: { type: String, enum: ['FEED_LIKE', 'MASQUERADE', 'HOOKUP'], default: 'FEED_LIKE' },

  // 匹配状态：'ACTIVE' (有效), 'UNMATCHED' (已解除匹配)
  status: { type: String, enum: ['ACTIVE', 'UNMATCHED'], default: 'ACTIVE' },

  // 如果解除了匹配，记录谁发起的
  unmatchedBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User', default: null }
}, {
  timestamps: true
});

// 建立索引，优化匹配列表查询
MatchSchema.index({ users: 1 });
MatchSchema.index({ "users.0": 1, "users.1": 1 }, { unique: true });
MatchSchema.index({ status: 1, updatedAt: -1 });

module.exports = mongoose.model('Match', MatchSchema);
