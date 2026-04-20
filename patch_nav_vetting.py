import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update SubscriptionScreen call in AurelianApp.kt
old_sub = """            composable("subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }"""

new_sub = """            composable("subscription") {
                SubscriptionScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToVetting = { type -> navController.navigate("vetting/$type") }
                )
            }"""

if "onNavigateToVetting" not in content:
    content = content.replace(old_sub, new_sub)

# Add VettingScreen route
vetting_route = """
            composable(
                route = "vetting/{type}",
                arguments = listOf(androidx.navigation.navArgument("type") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "IDENTITY"
                VettingScreen(
                    initialType = type,
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

if "vetting/{type}" not in content:
    content = content.replace('composable("subscription") {', vetting_route + '\n            composable("subscription") {')

with open(file_path, "w") as f:
    f.write(content)
