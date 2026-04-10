import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/HookupsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add onNavigateToRequests parameter to HookupsScreen
if "onNavigateToRequests" not in content:
    content = content.replace(
        "fun HookupsScreen() {",
        "fun HookupsScreen(onNavigateToRequests: () -> Unit = {}) {"
    )

# Add TopAppBar notification icon
old_top_bar = """        topBar = {
            TopAppBar(
                title = { Text("高定速约", color = Silver, fontWeight = FontWeight.Medium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },"""

new_top_bar = """        topBar = {
            TopAppBar(
                title = { Text("高定速约", color = Silver, fontWeight = FontWeight.Medium) },
                actions = {
                    IconButton(onClick = onNavigateToRequests) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = "速约通知",
                            tint = Gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },"""

if "MailOutline" not in content and "actions" not in old_top_bar:
    content = content.replace(old_top_bar, new_top_bar)

with open(file_path, "w") as f:
    f.write(content)
