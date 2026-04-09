const mongoose = require('mongoose');

const MasqueradeSessionSchema = new mongoose.Schema({
  // 假面舞会每天/定期生成一个会话 (Session)
  sessionDate: { type: Date, required: true, unique: true },

  // 今晚的主题或灵魂拷问 (Question of the night)
  question: { type: String, required: true },

  // 开场与结束时间
  startTime: { type: Date, required: true },
  endTime: { type: Date, required: true },

  // 状态：'SCHEDULED' (计划中), 'ACTIVE' (进行中), 'CLOSED' (已结束)
  status: { type: String, enum: ['SCHEDULED', 'ACTIVE', 'CLOSED'], default: 'SCHEDULED' }
}, {
  timestamps: true
});

// 查询当前活跃会话
MasqueradeSessionSchema.index({ status: 1, startTime: 1, endTime: 1 });

module.exports = mongoose.model('MasqueradeSession', MasqueradeSessionSchema);
