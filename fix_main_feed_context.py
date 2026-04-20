import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Make sure we declare `val context = LocalContext.current` early inside the MainFeedScreen
if "val context = LocalContext.current" not in content[:3000]:
    content = content.replace("var matchedUser by remember { mutableStateOf<User?>(null) }", "var matchedUser by remember { mutableStateOf<User?>(null) }\n    val context = LocalContext.current")

with open(file_path, "w") as f:
    f.write(content)
