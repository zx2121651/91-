import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

missing_imports = """
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountCircle
"""
if "import androidx.compose.ui.draw.clip" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", missing_imports + "\nimport androidx.compose.material.icons.filled.Add")

with open(file_path, "w") as f:
    f.write(content)
