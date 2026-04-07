import re

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# Fix the routing parameters for EventsScreen (we removed eventDetails for MVP simplicity)
search_events = r"""        composable\("events"\) \{
            EventsScreen\(
                onNavigateToEventDetails = \{ eventId ->
                    // Handle encoding issues
                    if \(eventId\.isBlank\(\)\) \{
                        navController\.navigate\("eventDetails/placeholder"\)
                    \} else \{
                        navController\.navigate\("eventDetails/$\{java\.net\.URLEncoder\.encode\(eventId, "UTF-8"\)\}"\)
                    \}
                \}
            \)
        \}"""

replace_events = """        composable("events") {
            EventsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }"""
content = re.sub(search_events, replace_events, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)
