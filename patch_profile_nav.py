import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/ProfileScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add onNavigateToUserFeed
old_sig = """fun ProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {"""

new_sig = """fun ProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToUserFeed: (String, Int) -> Unit = { _, _ -> }
) {"""

if "onNavigateToUserFeed" not in content:
    content = content.replace(old_sig, new_sig)

# Make mockVideos clickable
old_grid = """            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(mockVideos.size) { index ->
                    AsyncImage(
                        model = mockVideos[index],
                        contentDescription = "Post \$index",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(0.8f)
                            .border(0.5.dp, DeepBlack)
                    )
                }
            }"""

new_grid = """            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 真实场景下，这里会传入从后端拉取到的作品封面的 URL 数组
                items(mockVideos.size) { index ->
                    AsyncImage(
                        model = mockVideos[index],
                        contentDescription = "Post \$index",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(0.8f)
                            .border(0.5.dp, DeepBlack)
                            .clickable {
                                // 点击进入沉浸式个人作品墙，并直接定位到当前选中的视频
                                onNavigateToUserFeed(userId, index)
                            }
                    )
                }
            }"""

if "onNavigateToUserFeed(" not in content:
    content = content.replace(old_grid, new_grid)

with open(file_path, "w") as f:
    f.write(content)
