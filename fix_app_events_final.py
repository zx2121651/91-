import re

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

search = r"""            composable\(Screen\.Events\.route\) \{
                EventsScreen\(onNavigateToEventDetails = \{ eventId ->
                    if \(eventId == "evt_2"\) \{
                        navController\.navigate\("tea_ceremony"\)
                    \} else \{
                        navController\.navigate\("eventDetails/$\{java\.net\.URLEncoder\.encode\(eventId, "UTF-8"\)\}"\)
                    \}
                \}\)
            \}"""

replace = """            composable(Screen.Events.route) {
                EventsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

content = re.sub(search, replace, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)
