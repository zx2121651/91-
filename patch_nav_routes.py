import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update the profile route to accept optional/mandatory userId
old_profile = """            composable(Screen.Profile.route) {
                ProfileScreen(
                    userId = "me",
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

new_profile = """            composable(Screen.Profile.route) {
                ProfileScreen(
                    userId = "me",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { userName ->
                        navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}")
                    },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable(
                route = "profile/{userId}",
                arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val userId = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("userId") ?: "", "UTF-8")
                ProfileScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { userName ->
                        navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}")
                    }
                )
            }"""

if "profile/{userId}" not in content:
    content = content.replace(old_profile, new_profile)

# Update MainFeedScreen route
old_main_feed = """composable(Screen.Discover.route) { MainFeedScreen(onNavigateToMasquerade = { navController.navigate("masquerade") }, onNavigateToPublish = { isDraft ->
                    if (isDraft) {
                        navController.navigate("video_edit_draft")
                    } else {
                        navController.navigate("camera")
                    }
                }) }"""

new_main_feed = """composable(Screen.Discover.route) {
                MainFeedScreen(
                    onNavigateToProfile = { userId -> navController.navigate("profile/${java.net.URLEncoder.encode(userId, "UTF-8")}") },
                    onNavigateToMasquerade = { navController.navigate("masquerade") },
                    onNavigateToPublish = { isDraft ->
                        if (isDraft) {
                            navController.navigate("video_edit_draft")
                        } else {
                            navController.navigate("camera")
                        }
                    }
                )
            }"""

content = content.replace(old_main_feed, new_main_feed)


# Check MatchesScreen for Profile Navigation
old_matches = """composable(Screen.Matches.route) { MatchesScreen(onNavigateToProfile = { navController.navigate("profile") }) }"""
new_matches = """composable(Screen.Matches.route) { MatchesScreen(onNavigateToProfile = { userId -> navController.navigate("profile/${java.net.URLEncoder.encode(userId, "UTF-8")}") }) }"""

content = content.replace(old_matches, new_matches)

with open(file_path, "w") as f:
    f.write(content)
