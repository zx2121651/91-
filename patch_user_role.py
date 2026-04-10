import re

file_path = "Aurelian-Backend/src/models/User.js"
with open(file_path, "r") as f:
    content = f.read()

# Add role field to UserSchema
if "role: {" not in content:
    content = content.replace(
        "// 资产/高定认证状态：'PENDING', 'ACTIVE', 'REJECTED'",
        "// 系统角色：'USER' (普通高定会员), 'ADMIN' (后台管家/管理员)\n  role: { type: String, enum: ['USER', 'ADMIN'], default: 'USER' },\n\n  // 资产/高定认证状态：'PENDING', 'ACTIVE', 'REJECTED'"
    )

with open(file_path, "w") as f:
    f.write(content)
