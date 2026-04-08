package com.aurelian.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Discover : Screen("discover", "发现", Icons.Default.Home)
    object Matches : Screen("matches", "心动", Icons.Default.Favorite)
    object Messages : Screen("messages", "私信", Icons.Default.Email)
    object Hookups : Screen("hookups", "速约", Icons.Default.Favorite)
    object Events : Screen("events", "沙龙", Icons.Default.Person)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
}

val items = listOf(
    Screen.Discover,
    Screen.Matches,
    Screen.Messages,
    Screen.Hookups,
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
                LoginScreen(onLoginSuccess = { token, status ->
                    navController.navigate(Screen.Discover.route) {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }

            composable(Screen.Discover.route) { MainFeedScreen(onNavigateToMasquerade = { navController.navigate("masquerade") }, onNavigateToPublish = { isDraft ->
                    if (isDraft) {
                        navController.navigate("video_edit_draft")
                    } else {
                        navController.navigate("camera")
                    }
                }) }
            composable(Screen.Matches.route) { MatchesScreen(onNavigateToProfile = { navController.navigate("profile") }) }
            composable(Screen.Messages.route) {
                MessagesScreen(onNavigateToChat = { userName ->
                    navController.navigate("chat/${java.net.URLEncoder.encode(userName, "UTF-8")}")
                })
            }
            composable(Screen.Hookups.route) { HookupsScreen() }
            composable(Screen.Events.route) {
                EventsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    userId = "me",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("chat/{userName}") { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "礼宾部"

                ChatScreen(userName = userName, onBack = { navController.popBackStack() }, onNavigateToInvite = { navController.navigate("sendInvite/$userName") })
            }
            composable("eventDetails/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: "活动"
                EventDetailsScreen(eventId = eventId, onBack = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable("masquerade") {
                MasqueradeScreen(onBack = { navController.popBackStack() })
            }
            composable("tea_ceremony") {
                TeaCeremonyScreen(onBack = { navController.popBackStack() })
            }
            composable("referral") {
                ReferralScreen(onBack = { navController.popBackStack() })
            }
            composable("sendInvite/{userName}") { backStackEntry ->

                SendInvitationScreen(onBack = { navController.popBackStack() }, onSend = { navController.popBackStack() })
            }
            composable("subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
            composable("camera") {
                CameraScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { videoUris ->
                        val joinedUris = videoUris.joinToString(",")
                        navController.navigate("video_edit/${java.net.URLEncoder.encode(joinedUris, "UTF-8")}/false")
                    }
                )
            }
            composable(
                route = "video_edit/{videoUri}/{isDraft}",
                arguments = listOf(
                    navArgument("videoUri") { type = NavType.StringType },
                    navArgument("isDraft") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val videoUri = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("videoUri") ?: "", "UTF-8")
                val isDraft = backStackEntry.arguments?.getBoolean("isDraft") ?: false
                VideoEditScreen(
                    videoUri = videoUri,
                    isDraft = isDraft,
                    onBack = { navController.popBackStack() },
                    onNavigateToFeed = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    }
                )
            }

            // 专门处理草稿恢复的跳转 (因为视频 URI 在草稿箱内获取)
            composable("video_edit_draft") {
                val context = androidx.compose.ui.platform.LocalContext.current
                val draft = DraftManager.getDraft(context)
                if (draft != null) {
                    VideoEditScreen(
                        videoUri = draft.videoUri,
                        isDraft = true,
                        onBack = { navController.popBackStack() },
                        onNavigateToFeed = {
                            navController.navigate(Screen.Discover.route) {
                                popUpTo(Screen.Discover.route) { inclusive = true }
                            }
                        }
                    )
                } else {
                    // 草稿读取失败回退
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
