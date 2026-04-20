import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix signature of MainFeedScreen
content = re.sub(
    r'fun MainFeedScreen\([^)]*\)',
    'fun MainFeedScreen(onNavigateToEvents: () -> Unit = {}, onNavigateToProfile: (String) -> Unit = {}, onNavigateToMasquerade: () -> Unit = {}, onNavigateToPublish: () -> Unit = {})',
    content
)

missing_imports = """
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.filled.Favorite
"""

if "import androidx.compose.material.icons.rounded.AddCircle" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Star", missing_imports + "\nimport androidx.compose.material.icons.filled.Star")


with open(file_path, "w") as f:
    f.write(content)


app_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(app_path, "r") as f:
    app_content = f.read()

app_content = re.sub(
    r'composable\(Screen\.Discover\.route\) \{ MainFeedScreen\([^)]*\) \}',
    'composable(Screen.Discover.route) { MainFeedScreen(onNavigateToMasquerade = { navController.navigate("masquerade") }, onNavigateToPublish = { navController.navigate("publish_video") }) }',
    app_content
)
with open(app_path, "w") as f:
    f.write(app_content)
