import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update HookupsScreen composable
old_hookups = """            composable(Screen.Hookups.route) { HookupsScreen() }"""
new_hookups = """            composable(Screen.Hookups.route) {
                HookupsScreen(
                    onNavigateToRequests = { navController.navigate("hookup_requests") }
                )
            }

            composable("hookup_requests") {
                HookupRequestsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = { userId -> navController.navigate("profile/${java.net.URLEncoder.encode(userId, "UTF-8")}") },
                    onNavigateToChat = { userName -> navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}") }
                )
            }"""

if "hookup_requests" not in content:
    content = content.replace(old_hookups, new_hookups)

with open(file_path, "w") as f:
    f.write(content)
