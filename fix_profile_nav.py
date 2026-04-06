import re

# 1. Fix MainFeedScreen missing import
with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.foundation.clickable" not in content:
    content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable")

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'w') as f:
    f.write(content)

# 2. Fix AurelianApp missing params for old ProfileScreen route
with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    app_content = f.read()

search_old_profile = r"""        composable\(Screen\.Profile\.route\) \{
            ProfileScreen\(
                onNavigateToSettings = \{ navController\.navigate\("settings"\) \},
                onNavigateToReferral = \{ navController\.navigate\("referral"\) \},
                onNavigateToSubscription = \{ navController\.navigate\("subscription"\) \}
            \)
        \}"""

replace_old_profile = """        composable(Screen.Profile.route) {
            ProfileScreen(
                userId = "me", // Default for bottom tab
                onNavigateBack = { navController.popBackStack() }
            )
        }"""
app_content = re.sub(search_old_profile, replace_old_profile, app_content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(app_content)
