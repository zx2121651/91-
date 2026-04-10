import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Make sure to update the ProfileScreen calls
old_profile_call = """            composable(Screen.Profile.route) {
                ProfileScreen(
                    userId = "me",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { userName ->
                        navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}")
                    },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }"""

new_profile_call = """            composable(Screen.Profile.route) {
                ProfileScreen(
                    userId = "me",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { userName ->
                        navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}")
                    },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToUserFeed = { uid, index -> navController.navigate("user_feed/${java.net.URLEncoder.encode(uid, "UTF-8")}/$index") }
                )
            }"""
if "user_feed" not in content[:3000]: # Just an arbitrary check, search is better
    if new_profile_call not in content:
        content = content.replace(old_profile_call, new_profile_call)

old_other_profile = """            composable(
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

new_other_profile = """            composable(
                route = "profile/{userId}",
                arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val userId = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("userId") ?: "", "UTF-8")
                ProfileScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { userName ->
                        navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}")
                    },
                    onNavigateToUserFeed = { uid, index -> navController.navigate("user_feed/${java.net.URLEncoder.encode(uid, "UTF-8")}/$index") }
                )
            }"""

if new_other_profile not in content:
    content = content.replace(old_other_profile, new_other_profile)


# Add the new route user_feed
user_feed_route = """
            composable(
                route = "user_feed/{userId}/{index}",
                arguments = listOf(
                    androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("index") { type = androidx.navigation.NavType.IntType }
                )
            ) { backStackEntry ->
                val userId = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("userId") ?: "me", "UTF-8")
                val initialIndex = backStackEntry.arguments?.getInt("index") ?: 0
                UserFeedScreen(
                    userId = userId,
                    initialIndex = initialIndex,
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

if "user_feed/{userId}/{index}" not in content:
    # Just insert it before the closing brace of NavHost
    # E.g. right before composable("settings")
    content = content.replace('composable("settings") {', user_feed_route + '\n            composable("settings") {')

with open(file_path, "w") as f:
    f.write(content)
