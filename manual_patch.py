with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

import re

search = r"""            composable\(Screen\.Profile\.route\) \{
                ProfileScreen\(
                    onNavigateToSettings = \{ navController\.navigate\("settings"\) \},
                    onNavigateToReferral = \{ navController\.navigate\("referral"\) \},
                    onNavigateToSubscription = \{ navController\.navigate\("subscription"\) \}
                \)
            \}"""

replace = """            composable(Screen.Profile.route) {
                ProfileScreen(
                    userId = "me",
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

content = re.sub(search, replace, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)
