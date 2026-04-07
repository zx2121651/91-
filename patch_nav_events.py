import re

# Update AurelianApp.kt
with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# Add composable parameter import if missing
if "import androidx.navigation.compose.composable" not in content:
    content = content.replace("import androidx.navigation.compose.NavHost", "import androidx.navigation.compose.NavHost\nimport androidx.navigation.compose.composable")

# Find the profile route to add events right after it
search_profile = r"""        composable\(
            route = "profile/\{userId\}",
            arguments = listOf\(navArgument\("userId"\) \{ type = NavType\.StringType \}\)
        \) \{ backStackEntry ->
            val userId = backStackEntry\.arguments\?\.getString\("userId"\) \?: "me"
            ProfileScreen\(
                userId = userId,
                onNavigateBack = \{ navController\.popBackStack\(\) \}
            \)
        \}"""

replace_profile = """        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"
            ProfileScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("events") {
            EventsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }"""
content = re.sub(search_profile, replace_profile, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)
