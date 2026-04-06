import re

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# Add missing imports if needed
if "import androidx.navigation.navArgument" not in content:
    content = content.replace("import androidx.navigation.compose.composable", "import androidx.navigation.compose.composable\nimport androidx.navigation.navArgument\nimport androidx.navigation.NavType")

# Find the MainFeed route definition. It might look slightly different than expected.
search_main_feed = r"""        composable\(Screen\.MainFeed\.route\) \{
            MainFeedScreen\(\)
        \}"""

replace_main_feed = """        composable(Screen.MainFeed.route) {
            MainFeedScreen(
                onNavigateToMasquerade = { navController.navigate(Screen.Masquerade.route) },
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"
            ProfileScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }"""
content = re.sub(search_main_feed, replace_main_feed, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)
