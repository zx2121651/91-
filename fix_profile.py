import re

with open('Aurelian/app/src/main/java/com/aurelian/app/ProfileScreen.kt', 'r') as f:
    content = f.read()

# Fix imports since bash script echo often messes up imports that are needed
if "import androidx.compose.ui.text.style.TextAlign" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.text.style.TextAlign")

with open('Aurelian/app/src/main/java/com/aurelian/app/ProfileScreen.kt', 'w') as f:
    f.write(content)
