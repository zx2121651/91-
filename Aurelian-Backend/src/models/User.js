const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
  // 基础身份
  name: { type: String, required: true, trim: true },
  phone: { type: String, unique: true, sparse: true },
  email: { type: String, unique: true, sparse: true },

  // 个人资料
  bio: { type: String, default: '', maxLength: 300 },
  location: { type: String, default: 'Unknown' },
  avatarUrl: { type: String, default: '' },

  // 资产/高定认证状态：'PENDING', 'ACTIVE', 'REJECTED'
  status: { type: String, enum: ['PENDING', 'ACTIVE', 'REJECTED', 'BANNED'], default: 'PENDING' },
  membershipTier: { type: String, enum: ['STANDARD', 'GOLD', 'BLACK'], default: 'STANDARD' },
  isVerified: { type: Boolean, default: false },

  // 隐私与偏好
  stealthMode: { type: Boolean, default: false },
  minAgePreference: { type: Number, default: 18 },

  // 统计与信用分
  followersCount: { type: Number, default: 0 },
  followingCount: { type: Number, default: 0 },
  trustScore: { type: Number, default: 100 }, // 风控系统使用

}, {
  timestamps: true // 自动管理 createdAt, updatedAt
});

// 索引优化查询
UserSchema.index({ status: 1, isVerified: 1 });
UserSchema.index({ location: 1 });

module.exports = mongoose.model('User', UserSchema);
