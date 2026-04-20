import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update MainFeedScreen invocation
content = re.sub(
    r'onNavigateToPublish\s*=\s*\{\s*navController\.navigate\("camera"\)\s*\}',
    """onNavigateToPublish = { isDraft ->
                    if (isDraft) {
                        navController.navigate("video_edit_draft")
                    } else {
                        navController.navigate("camera")
                    }
                }""",
    content
)

# Update CameraScreen route to pass isDraft=false
content = content.replace(
    """onNavigateToEdit = { videoUri ->
                        navController.navigate("video_edit/${java.net.URLEncoder.encode(videoUri, "UTF-8")}")
                    }""",
    """onNavigateToEdit = { videoUri ->
                        navController.navigate("video_edit/${java.net.URLEncoder.encode(videoUri, "UTF-8")}/false")
                    }"""
)

# Update VideoEditScreen route to accept isDraft
old_route = """            composable(
                route = "video_edit/{videoUri}",
                arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val videoUri = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("videoUri") ?: "", "UTF-8")
                VideoEditScreen(
                    videoUri = videoUri,
                    onBack = { navController.popBackStack() },
                    onNavigateToFeed = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    }
                )
            }"""

new_route = """            composable(
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
            }"""

content = content.replace(old_route, new_route)

with open(file_path, "w") as f:
    f.write(content)
