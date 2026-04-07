import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Make sure onNavigateToPublish is actually mapped if it was named differently in AurelianApp.kt
content = re.sub(
    r'composable\(Screen\.Discover\.route\) \{ MainFeedScreen\(.*?\) \}',
    'composable(Screen.Discover.route) { MainFeedScreen(onNavigateToMasquerade = { navController.navigate("masquerade") }, onNavigateToPublish = { navController.navigate("publish_video") }) }',
    content
)

with open(file_path, "w") as f:
    f.write(content)
