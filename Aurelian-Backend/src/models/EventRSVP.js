const mongoose = require('mongoose');

const EventRSVPSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  eventId: { type: mongoose.Schema.Types.ObjectId, ref: 'Event', required: true },

  // 报名状态：'PENDING' (审核中), 'CONFIRMED' (已确认), 'WAITLISTED' (候补), 'CANCELLED' (已取消)
  status: { type: String, enum: ['PENDING', 'CONFIRMED', 'WAITLISTED', 'CANCELLED'], default: 'PENDING' },

  // 同行人数 (比如可以带 1 位伴伴)
  partySize: { type: Number, default: 1, min: 1, max: 2 },

  // 用户的备注 (如饮食禁忌等)
  notes: { type: String, maxLength: 200, default: '' },

  // 如果被拒绝或取消的原因
  reason: { type: String, default: '' }
}, {
  timestamps: true
});

// 防止用户对同一个活动重复报名
EventRSVPSchema.index({ userId: 1, eventId: 1 }, { unique: true });
EventRSVPSchema.index({ eventId: 1, status: 1 });

module.exports = mongoose.model('EventRSVP', EventRSVPSchema);
