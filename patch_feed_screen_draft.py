import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add logic in MainFeedScreen to handle the publish button correctly
# Modify the signature of onNavigateToPublish to take a boolean (isDraft)
content = re.sub(
    r'onNavigateToPublish:\s*\(\)\s*->\s*Unit\s*=\s*\{\}',
    'onNavigateToPublish: (Boolean) -> Unit = {}',
    content
)

old_btn = """                        // 发布视频按钮
                        IconButton(onClick = onNavigateToPublish) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.AddCircle,
                                contentDescription = "发布动态",
                                tint = Silver.copy(alpha = 0.9f),
                                modifier = Modifier.size(32.dp)
                            )
                        }"""

new_btn = """                        // 草稿提示弹窗状态
                        var showDraftPrompt by remember { mutableStateOf(false) }

                        if (showDraftPrompt) {
                            AlertDialog(
                                onDismissRequest = { showDraftPrompt = false },
                                title = { Text("未完成的动态", color = Gold, fontWeight = FontWeight.Bold) },
                                text = { Text("您有一份未完成的高定剪辑草稿，是否继续编辑？", color = Silver) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showDraftPrompt = false
                                        onNavigateToPublish(true) // 恢复草稿
                                    }) {
                                        Text("继续编辑", color = Gold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showDraftPrompt = false
                                        val context = LocalContext.current
                                        DraftManager.clearDraft(context)
                                        onNavigateToPublish(false) // 开启新拍摄
                                    }) {
                                        Text("放弃并重拍", color = Color.Gray)
                                    }
                                },
                                containerColor = Color(0xFF1B1B1B)
                            )
                        }

                        // 发布视频按钮
                        val context = LocalContext.current
                        IconButton(onClick = {
                            if (DraftManager.hasDraft(context)) {
                                showDraftPrompt = true
                            } else {
                                onNavigateToPublish(false)
                            }
                        }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.AddCircle,
                                contentDescription = "发布动态",
                                tint = Silver.copy(alpha = 0.9f),
                                modifier = Modifier.size(32.dp)
                            )
                        }"""

content = content.replace(old_btn, new_btn)

with open(file_path, "w") as f:
    f.write(content)
