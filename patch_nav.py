import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# Replace publish_video with camera, and add video_edit
# Firstly, modify MainFeedScreen's onNavigateToPublish
content = content.replace('navController.navigate("publish_video")', 'navController.navigate("camera")')

# Then replace the composable block
old_block = """            composable("publish_video") {
                PublishVideoScreen(onBack = { navController.popBackStack() })
            }"""

new_block = """            composable("camera") {
                CameraScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { videoUri ->
                        navController.navigate("video_edit/${java.net.URLEncoder.encode(videoUri, "UTF-8")}")
                    }
                )
            }
            composable(
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

content = content.replace(old_block, new_block)

with open(file_path, "w") as f:
    f.write(content)
