import re

file_path = "Aurelian-Backend/src/models/Message.js"
with open(file_path, "r") as f:
    content = f.read()

# Add readAt and expiresAt
if "readAt:" not in content:
    content = content.replace(
        "ephemeralDurationSeconds: { type: Number, default: 0 },",
        """ephemeralDurationSeconds: { type: Number, default: 0 },

  // 阅后即焚引擎的核心时间戳
  readAt: { type: Date, default: null },
  expiresAt: { type: Date, default: null }, // 如果是阅后即焚，这等于 readAt + duration"""
    )

# Add TTL Index
ttl_index = """
// 核心原子特性：阅后即焚数据库层面的彻底销毁 (MongoDB TTL Index)
// MongoDB 会自动定期扫描并删除 expiresAt 小于当前时间的文档
MessageSchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 });
"""

if "expireAfterSeconds" not in content:
    content = content.replace("module.exports = mongoose.model('Message', MessageSchema);", ttl_index + "\nmodule.exports = mongoose.model('Message', MessageSchema);")

with open(file_path, "w") as f:
    f.write(content)
