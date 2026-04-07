import re

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# Instead of passing down events through everything, let's just make a simple TopBar in MainFeedScreen to get there
# Actually, the bottom navigation or a top bar action is ideal for a high-end app to discover events.
# We'll patch MainFeedScreen to add a sleek top corner icon.

search_main_feed = r"""        composable\(Screen\.MainFeed\.route\) \{
            MainFeedScreen\(
                onNavigateToMasquerade = \{ navController\.navigate\(Screen\.Masquerade\.route\) \},
                onNavigateToProfile = \{ userId ->
                    navController\.navigate\("profile/\$userId"\)
                \}
            \)
        \}"""

replace_main_feed = """        composable(Screen.MainFeed.route) {
            MainFeedScreen(
                onNavigateToEvents = { navController.navigate("events") },
                onNavigateToMasquerade = { navController.navigate(Screen.Masquerade.route) },
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }"""
content = re.sub(search_main_feed, replace_main_feed, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)


with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'r') as f:
    feed_content = f.read()

# Update signature
feed_content = feed_content.replace(
    "fun MainFeedScreen(\n    onNavigateToMasquerade: () -> Unit = {},",
    "fun MainFeedScreen(\n    onNavigateToEvents: () -> Unit = {},\n    onNavigateToMasquerade: () -> Unit = {},"
)

# Add a floating icon at Top-Right for Discover/Events
search_box_start = r"    Box\(modifier = Modifier\.fillMaxSize\(\)\) \{"
replace_box_start = """    Box(modifier = Modifier.fillMaxSize()) {
        // ... rest of FeedItems ..."""

search_pager = r"""                VerticalPager\(
                    state = pagerState,
                    modifier = Modifier
                        \.fillMaxSize\(\)
                        \.background\(DeepBlack\)
                \) \{ page ->
                    FeedItem\(user = users\[page\], isSelected = page == pagerState\.currentPage, onNavigateToProfile = onNavigateToProfile, onNavigateToMasquerade = onNavigateToMasquerade, onLike = \{ viewModel\.likeUser\(users\[page\]\) \}\)
                \}"""

replace_pager = """                Box(modifier = Modifier.fillMaxSize()) {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeepBlack)
                    ) { page ->
                        FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToProfile = onNavigateToProfile, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) })
                    }

                    // Top Right Action: Exclusive Events Discovery
                    IconButton(
                        onClick = onNavigateToEvents,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp, end = 16.dp) // Below status bar
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.DateRange,
                            contentDescription = "Exclusive Events",
                            tint = Gold.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }"""
feed_content = re.sub(search_pager, replace_pager, feed_content)

# add DateRange icon import
if "import androidx.compose.material.icons.filled.DateRange" not in feed_content:
    feed_content = feed_content.replace("import androidx.compose.material.icons.filled.Share", "import androidx.compose.material.icons.filled.Share\nimport androidx.compose.material.icons.filled.DateRange")

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'w') as f:
    f.write(feed_content)
