import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/ProfileScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Make the signature flexible to add more routes if needed
content = re.sub(
    r'fun ProfileScreen\(\s*userId: String,\s*onNavigateBack: \(\) -> Unit\s*\)',
    """fun ProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
)""",
    content
)

# Update the action buttons area based on isMe
old_actions = """        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* TODO: Navigate to Edit Profile */ },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(
                    text = if (isMe) "EDIT PROFILE" else "CONNECT",
                    color = DeepBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            if (!isMe) {
                OutlinedButton(
                    onClick = { /* TODO: Direct Message */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Silver),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Message",
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MESSAGE",
                        color = Gold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { /* TODO: Settings */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Silver),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        text = "SETTINGS",
                        color = Silver,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }"""

new_actions = """        // 针对 "我的" 或 "他人主页" 的多态操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isMe) {
                Button(
                    onClick = { /* TODO: 编辑资料弹层 */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        text = "编辑资料",
                        color = DeepBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedButton(
                    onClick = { onNavigateToSettings() },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Silver),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        text = "偏好设置",
                        color = Silver,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Button(
                    onClick = { /* TODO: 发起高定关注 / 关注成功提示 */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        text = "关注",
                        color = DeepBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedButton(
                    onClick = { onNavigateToChat(mockName) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Silver),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Message",
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "私聊",
                        color = Gold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }"""

if "EDIT PROFILE" in content:
    content = content.replace(old_actions, new_actions)

with open(file_path, "w") as f:
    f.write(content)
