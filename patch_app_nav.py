import re

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# Replace the startDestination of the top level NavHost
search_nav = r"NavHost\(navController = navController, startDestination = Screen\.MainFeed\.route\) \{"
replace_nav = """    // Simplified App State Management for Demo purposes
    // Ideally this would be powered by a datastore/viewmodel
    var currentToken by remember { mutableStateOf<String?>(null) }
    var currentUserStatus by remember { mutableStateOf<String?>(null) }

    val initialRoute = when {
        currentToken == null -> "login"
        currentUserStatus == "PENDING" -> "invite"
        else -> Screen.MainFeed.route
    }

    NavHost(navController = navController, startDestination = initialRoute) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { token, status ->
                    currentToken = token
                    currentUserStatus = status
                    if (status == "ACTIVE") {
                        navController.navigate(Screen.MainFeed.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("invite") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("invite") {
            InviteCodeScreen(
                pendingToken = currentToken ?: "",
                onVerifySuccess = { newToken ->
                    currentToken = newToken
                    currentUserStatus = "ACTIVE"
                    navController.navigate(Screen.MainFeed.route) {
                        popUpTo("invite") { inclusive = true }
                    }
                }
            )
        }
"""
content = re.sub(search_nav, replace_nav, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)
