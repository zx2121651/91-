const mongoose = require('mongoose');

const VerificationRequestSchema = new mongoose.Schema({
  // 谁发起的审核申请
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  // 审核类型：'ASSETS' (资产审核 - 豪车、房产、存款), 'IDENTITY' (身份认证 - 护照、名片、学历)
  type: { type: String, enum: ['ASSETS', 'IDENTITY'], required: true },

  // 提交的图片凭证 (安全存储库的链接数组)
  documentUrls: [{ type: String, required: true }],

  // 补充说明
  notes: { type: String, maxLength: 500, default: '' },

  // 审核状态：'PENDING' (待人工审核), 'APPROVED' (已通过), 'REJECTED' (已驳回)
  status: { type: String, enum: ['PENDING', 'APPROVED', 'REJECTED'], default: 'PENDING' },

  // 驳回原因 (可选)
  rejectReason: { type: String, default: null },

  // 审核人 (内部 Admin ID)
  reviewedBy: { type: mongoose.Schema.Types.ObjectId, ref: 'Admin', default: null },

  // 审核完成时间
  reviewedAt: { type: Date, default: null }
}, {
  timestamps: true
});

// 查询某个用户的当前申请状态
VerificationRequestSchema.index({ userId: 1, type: 1, status: 1 });

module.exports = mongoose.model('VerificationRequest', VerificationRequestSchema);
