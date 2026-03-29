import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'r') as f:
    content = f.read()

# Make sure we import UnstableApi because MainFeedViewModel functions are now marked with it
if "import androidx.media3.common.util.UnstableApi" not in content:
    content = content.replace("import androidx.media3.ui.PlayerView", "import androidx.media3.common.util.UnstableApi\nimport androidx.media3.ui.PlayerView")

search_pattern = r"""                VerticalPager\(
                    state = pagerState,
                    modifier = Modifier
                        \.fillMaxSize\(\)
                        \.background\(DeepBlack\)
                \) \{ page ->
                    FeedItem\(user = users\[page\], isSelected = page == pagerState\.currentPage, onNavigateToMasquerade = onNavigateToMasquerade, onLike = \{ viewModel\.likeUser\(users\[page\]\) \}\)
                \}"""

replace_pattern = """                // Smart preloading and cancelling based on scroll state
                LaunchedEffect(pagerState.currentPage) {
                    val currentIdx = pagerState.currentPage

                    // Preload next and next+1
                    if (currentIdx + 1 < users.size) viewModel.preloadVideo(users[currentIdx + 1].videoUrl)
                    if (currentIdx + 2 < users.size) viewModel.preloadVideo(users[currentIdx + 2].videoUrl)

                    // Cancel preloading for far away items to save bandwidth
                    if (currentIdx - 2 >= 0) viewModel.cancelPreload(users[currentIdx - 2].videoUrl)
                    if (currentIdx + 3 < users.size) viewModel.cancelPreload(users[currentIdx + 3].videoUrl)
                }

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepBlack)
                ) { page ->
                    FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) })
                }"""

content = re.sub(search_pattern, replace_pattern, content)

# Also need to suppress UnstableApi for the FeedItem calls if needed, or just suppress at top
if "@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)" not in content:
    content = content.replace("@OptIn(ExperimentalFoundationApi::class)", "@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)\n@OptIn(ExperimentalFoundationApi::class)")

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'w') as f:
    f.write(content)
