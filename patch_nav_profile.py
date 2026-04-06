import re

# Update AurelianApp.kt
with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# Add composable parameter import if missing
if "import androidx.navigation.navArgument" not in content:
    content = content.replace("import androidx.navigation.compose.composable", "import androidx.navigation.compose.composable\nimport androidx.navigation.navArgument\nimport androidx.navigation.NavType")

# Update MainFeed block to pass navigation callback down to FeedScreen
search_main_feed = r"""        composable\(Screen\.MainFeed\.route\) \{
            MainFeedScreen\(
                onNavigateToMasquerade = \{ navController\.navigate\(Screen\.Masquerade\.route\) \}
            \)
        \}"""

replace_main_feed = """        composable(Screen.MainFeed.route) {
            MainFeedScreen(
                onNavigateToMasquerade = { navController.navigate(Screen.Masquerade.route) },
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "me"
            ProfileScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }"""
content = re.sub(search_main_feed, replace_main_feed, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)

# Update MainFeedScreen.kt to support onNavigateToProfile
with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'r') as f:
    feed_content = f.read()

feed_content = feed_content.replace(
    "fun MainFeedScreen(\n    onNavigateToMasquerade: () -> Unit = {},",
    "fun MainFeedScreen(\n    onNavigateToMasquerade: () -> Unit = {},\n    onNavigateToProfile: (String) -> Unit = {},"
)

# Now find the FeedItem call inside VerticalPager and add onNavigateToProfile
feed_content = feed_content.replace(
    "FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) })",
    "FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToProfile = onNavigateToProfile, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) })"
)

# Update FeedItem signature
feed_content = feed_content.replace(
    "fun FeedItem(user: User, isSelected: Boolean, onNavigateToMasquerade: () -> Unit, onLike: () -> Unit)",
    "fun FeedItem(user: User, isSelected: Boolean, onNavigateToProfile: (String) -> Unit, onNavigateToMasquerade: () -> Unit, onLike: () -> Unit)"
)

# Make the User Info Overlay clickable to navigate to profile
search_overlay = r"""        // Extremely clean User Info Overlay \(Bottom Left\)
        Column\(
            modifier = Modifier
                \.align\(Alignment\.BottomStart\)
                \.padding\(start = 16\.dp, end = 80\.dp, bottom = 90\.dp\) // Leave MORE space for Right Actions to avoid overlap
        \) \{"""

replace_overlay = """        // Extremely clean User Info Overlay (Bottom Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 90.dp) // Leave MORE space for Right Actions to avoid overlap
                .clickable { onNavigateToProfile(user.id) } // Clicking user info navigates to profile
        ) {"""
feed_content = re.sub(search_overlay, replace_overlay, feed_content)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'w') as f:
    f.write(feed_content)
