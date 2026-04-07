with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if line.strip().startswith("composable(Screen.Events.route) {"):
        skip = True
        new_lines.append("            composable(Screen.Events.route) {\n")
        new_lines.append("                EventsScreen(\n")
        new_lines.append("                    onNavigateBack = { navController.popBackStack() }\n")
        new_lines.append("                )\n")
        new_lines.append("            }\n")
        continue

    if skip:
        if line.strip() == "}":
            # This logic might fail if nesting is complex, let's just do a clean string replacement
            pass

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    full_content = f.read()

# We will just replace the exact text
old_text = """            composable(Screen.Events.route) {
                EventsScreen(onNavigateToEventDetails = { eventId ->
                    if (eventId == "evt_2") {
                        navController.navigate("tea_ceremony")
                    } else {
                        navController.navigate("eventDetails/${java.net.URLEncoder.encode(eventId, "UTF-8")}")
                    }
                })
            }"""

new_text = """            composable(Screen.Events.route) {
                EventsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

fixed = full_content.replace(old_text, new_text)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(fixed)
