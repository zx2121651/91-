const mongoose = require('mongoose');

const EventSchema = new mongoose.Schema({
  title: { type: String, required: true },
  description: { type: String, required: true },

  // 活动类型：'SALON' (沙龙), 'PARTY' (派对), 'TASTING' (品鉴会), 'EXHIBITION' (看展)
  type: { type: String, enum: ['SALON', 'PARTY', 'TASTING', 'EXHIBITION'], default: 'SALON' },

  // 举办城市和详细地址
  city: { type: String, required: true },
  location: { type: String, required: true },

  // 时间与容量
  date: { type: Date, required: true },
  capacity: { type: Number, required: true },
  enrolledCount: { type: Number, default: 0 },

  // 高定要求
  attireProtocol: { type: String, default: 'Black Tie / Evening Gown' },
  coverUrl: { type: String, default: '' },

  // 状态：'UPCOMING' (即将开始), 'ONGOING' (进行中), 'COMPLETED' (已结束), 'CANCELLED' (已取消)
  status: { type: String, enum: ['UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'], default: 'UPCOMING' }
}, {
  timestamps: true
});

EventSchema.index({ city: 1, status: 1, date: 1 });

module.exports = mongoose.model('Event', EventSchema);
