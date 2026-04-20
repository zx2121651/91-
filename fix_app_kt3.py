import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

bad_line = 'composable(Screen.Discover.route) { MainFeedScreen(onNavigateToMasquerade = { navController.navigate("masquerade") }, onNavigateToPublish = { navController.navigate("publish_video") }) }, onNavigateToPublish = { navController.navigate("publish_video") }) }'
good_line = 'composable(Screen.Discover.route) { MainFeedScreen(onNavigateToMasquerade = { navController.navigate("masquerade") }, onNavigateToPublish = { navController.navigate("publish_video") }) }'

content = content.replace(bad_line, good_line)

with open(file_path, "w") as f:
    f.write(content)
