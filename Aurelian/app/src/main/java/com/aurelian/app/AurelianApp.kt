package com.aurelian.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Discover : Screen("discover", "发现", Icons.Default.Home)
    object Matches : Screen("matches", "心动", Icons.Default.Favorite)
    object Messages : Screen("messages", "私信", Icons.Default.Email)
    object Events : Screen("events", "沙龙", Icons.Default.Person)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
}

val items = listOf(
    Screen.Discover,
    Screen.Matches,
    Screen.Messages,
    Screen.Events,
    Screen.Profile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AurelianApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route

            if (currentRoute in items.map { it.route }) {
                NavigationBar(
                    containerColor = DeepBlack,
                    contentColor = Silver
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Gold,
                                unselectedIconColor = Silver,
                                selectedTextColor = Gold,
                                unselectedTextColor = Silver,
                                indicatorColor = DeepBlack
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = "login",
            Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.Discover.route) {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }

            composable(Screen.Discover.route) { MainFeedScreen() }
            composable(Screen.Matches.route) { MatchesScreen() }
            composable(Screen.Messages.route) {
                MessagesScreen(onNavigateToChat = { userName ->
                    navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}")
                })
            }
            composable(Screen.Events.route) {
                EventsScreen(onNavigateToEventDetails = { eventName ->
                    navController.navigate("eventDetails/${java.net.URLEncoder.encode(eventName, "UTF-8")}")
                })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToReferral = { navController.navigate("referral") },
                    onNavigateToSubscription = { navController.navigate("subscription") }
                )
            }

            composable("chat/{userName}") { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "礼宾部"
                ChatScreen(userName = userName, onBack = { navController.popBackStack() })
            }
            composable("eventDetails/{eventName}") { backStackEntry ->
                val eventName = backStackEntry.arguments?.getString("eventName")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "活动"
                EventDetailsScreen(eventName = eventName, onBack = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable("referral") {
                ReferralScreen(onBack = { navController.popBackStack() })
            }
            composable("subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
