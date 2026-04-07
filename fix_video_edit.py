import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

missing_import = "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset"

if "tabIndicatorOffset" not in content[:1000]:
    content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\n" + missing_import)

with open(file_path, "w") as f:
    f.write(content)
