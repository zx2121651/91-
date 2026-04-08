import re

# Fix AurelianApp.kt
file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

if "import androidx.compose.runtime.LaunchedEffect" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect")

with open(file_path, "w") as f:
    f.write(content)

# Fix MainFeedScreen.kt -> LocalContext.current cannot be called inside onClick block
file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Replace `val context = LocalContext.current` inside the dismissButton click listener and the outer button
bad_logic = """                                dismissButton = {
                                    TextButton(onClick = {
                                        showDraftPrompt = false
                                        val context = LocalContext.current
                                        DraftManager.clearDraft(context)
                                        onNavigateToPublish(false) // 开启新拍摄
                                    }) {
                                        Text("放弃并重拍", color = Color.Gray)
                                    }
                                }"""

good_logic = """                                dismissButton = {
                                    TextButton(onClick = {
                                        showDraftPrompt = false
                                        DraftManager.clearDraft(context)
                                        onNavigateToPublish(false) // 开启新拍摄
                                    }) {
                                        Text("放弃并重拍", color = Color.Gray)
                                    }
                                }"""
content = content.replace(bad_logic, good_logic)

bad_btn = """                        // 发布视频按钮
                        val context = LocalContext.current
                        IconButton(onClick = {
                            if (DraftManager.hasDraft(context)) {
                                showDraftPrompt = true
                            } else {
                                onNavigateToPublish(false)
                            }
                        }) {"""

good_btn = """                        // 发布视频按钮
                        IconButton(onClick = {
                            if (DraftManager.hasDraft(context)) {
                                showDraftPrompt = true
                            } else {
                                onNavigateToPublish(false)
                            }
                        }) {"""
content = content.replace(bad_btn, good_btn)

with open(file_path, "w") as f:
    f.write(content)
