import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add @OptIn(ExperimentalMaterial3Api::class) to MainFeedScreen
if "@OptIn(ExperimentalMaterial3Api::class)" not in content:
    content = content.replace(
        "@OptIn(ExperimentalFoundationApi::class)\n@Composable\nfun MainFeedScreen(",
        "@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)\n@Composable\nfun MainFeedScreen("
    )

with open(file_path, "w") as f:
    f.write(content)
