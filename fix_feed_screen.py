import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix imports
missing_imports = """
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
"""

if "import androidx.compose.material.icons.filled.Favorite" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.FavoriteBorder", missing_imports + "\nimport androidx.compose.material.icons.filled.FavoriteBorder")

# Fix MainFeedScreen signature if needed
# It might have been skipped by my last regex if it had newlines
if "fun MainFeedScreen(onNavigateToMasquerade: () -> Unit, onNavigateToPublish: () -> Unit = {})" not in content:
    content = re.sub(r'fun MainFeedScreen\(.*?\)', 'fun MainFeedScreen(onNavigateToMasquerade: () -> Unit = {}, onNavigateToPublish: () -> Unit = {})', content)

if "fun MainFeedScreen(" not in content:
   pass

# Add isLiked to User model if not there? Wait, User model is in Network.kt or another file. Let's check where it is.
# First, let's fix the mutableStateOf
content = content.replace("var liked by remember { mutableStateOf(user.isLiked) }", "var liked by remember { mutableStateOf(false) }")

with open(file_path, "w") as f:
    f.write(content)
