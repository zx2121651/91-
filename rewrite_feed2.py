import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add imports
missing_imports = """
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
"""
if "import androidx.compose.material.icons.rounded.AddCircle" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Star", missing_imports + "\nimport androidx.compose.material.icons.filled.Star")

# Fix signature
# Be very careful to replace exactly what is there
sig_pattern = r'fun MainFeedScreen\(onNavigateToMasquerade:\s*\(\)\s*->\s*Unit\s*=\s*\{\}\)\s*\{'
if re.search(sig_pattern, content):
    content = re.sub(sig_pattern, 'fun MainFeedScreen(onNavigateToMasquerade: () -> Unit = {}, onNavigateToPublish: () -> Unit = {}) {', content)
else:
    # Try another pattern
    sig_pattern2 = r'fun MainFeedScreen\(onNavigateToMasquerade:\s*\(\)\s*->\s*Unit\)\s*\{'
    if re.search(sig_pattern2, content):
        content = re.sub(sig_pattern2, 'fun MainFeedScreen(onNavigateToMasquerade: () -> Unit, onNavigateToPublish: () -> Unit = {}) {', content)

# It could also be that viewModel is injected:
# Let's search for "fun MainFeedScreen" to see how it's defined
with open("sig_check.txt", "w") as out:
    for line in content.splitlines():
        if "fun MainFeedScreen" in line:
            out.write(line + "\n")
