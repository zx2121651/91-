const mongoose = require('mongoose');

const InviteCodeSchema = new mongoose.Schema({
  // 唯一的邀请短码 (通常为大写字母+数字，例如 "AURELIAN-8X9D")
  code: { type: String, required: true, unique: true, uppercase: true, trim: true },

  // 谁生成的这个邀请码 (可以是系统 ADMIN 生成，也可以是高级会员裂变生成)
  creatorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', default: null },

  // 邀请码的状态：'ACTIVE' (有效), 'USED' (已用完), 'REVOKED' (已撤销)
  status: { type: String, enum: ['ACTIVE', 'USED', 'REVOKED', 'EXPIRED'], default: 'ACTIVE' },

  // 限制使用次数 (剩余次数)
  usageLimit: { type: Number, default: 1, min: 0 },
  usageCount: { type: Number, default: 0, min: 0 },

  // 谁使用了这个邀请码 (记录所有通过这个码注册的用户)
  usedBy: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],

  // 邀请码有效期 (如果为 null 则永久有效)
  expiresAt: { type: Date, default: null }
}, {
  timestamps: true
});

// 查询用户专属活跃邀请码
InviteCodeSchema.index({ creatorId: 1, status: 1 });
InviteCodeSchema.index({ code: 1, status: 1 });

module.exports = mongoose.model('InviteCode', InviteCodeSchema);
