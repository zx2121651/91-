import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Restore from HEAD first to get a clean state, then we do simple string replacements
import subprocess
subprocess.run(["git", "restore", file_path])

with open(file_path, "r") as f:
    content = f.read()

# Add imports
missing_imports = """
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
"""
if "import androidx.compose.material.icons.rounded.AddCircle" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Star", missing_imports + "\nimport androidx.compose.material.icons.filled.Star")

# Replace signature
# Looking at original file (via git diff or something similar):
# The original might be `fun MainFeedScreen(onNavigateToMasquerade: () -> Unit) {`
# Or `fun MainFeedScreen(onNavigateToEvents: () -> Unit = {}, onNavigateToProfile: (String) -> Unit = {}, onNavigateToMasquerade: () -> Unit = {}, viewModel: MainFeedViewModel = viewModel()) {`
# Let's use a very targeted replace for the signature block
content = re.sub(
    r'fun MainFeedScreen\([^)]*\)\s*\{',
    'fun MainFeedScreen(\n    onNavigateToEvents: () -> Unit = {},\n    onNavigateToMasquerade: () -> Unit = {},\n    onNavigateToProfile: (String) -> Unit = {},\n    onNavigateToPublish: () -> Unit = {},\n    viewModel: MainFeedViewModel = viewModel()\n) {',
    content
)


old_events_icon = """                    // Top Right Action: Exclusive Events Discovery
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
                    }"""

new_events_icon = """                    // 顶部右侧按钮容器
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 发布视频按钮
                        IconButton(onClick = onNavigateToPublish) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.AddCircle,
                                contentDescription = "发布动态",
                                tint = Silver.copy(alpha = 0.9f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }"""

if "发布动态" not in content:
    content = content.replace(old_events_icon, new_events_icon)


old_actions = """        // Minimalist Actions Overlay (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val iconTint = Gold.copy(alpha = 0.85f)
            val iconSize = 26.dp

            IconButton(
                onClick = onNavigateToMasquerade,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = "午夜盲盒", tint = iconTint, modifier = Modifier.size(iconSize))
            }
            IconButton(
                onClick = onLike,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "喜欢", tint = iconTint, modifier = Modifier.size(iconSize))
            }
            IconButton(
                onClick = { Toast.makeText(context, "私密社交，禁止公开评论", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.MailOutline, contentDescription = "评论", tint = iconTint, modifier = Modifier.size(iconSize))
            }
            IconButton(
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "我正在 Aurelian Night 关注一位品位非凡的会员。快来开启您的私密高定之旅。")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "分享会员主页")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "分享", tint = iconTint, modifier = Modifier.size(iconSize))
            }
        }"""

new_actions = """        // 极简高端的操作栏 (右侧)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val iconTint = Color.White.copy(alpha = 0.95f)
            val iconSize = 32.dp

            // 1. 用户头像 / 关注
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onNavigateToProfile(user.id) },
                contentAlignment = Alignment.Center
            ) {
                // 圆形头像边框和占位
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(DeepBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "主页", tint = iconTint, modifier = Modifier.size(40.dp))
                }
            }

            // 2. 点赞（爱心）
            var liked by remember { mutableStateOf(user.isLiked) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        liked = !liked
                        onLike()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "心动",
                        tint = if (liked) Color(0xFFE53935) else iconTint,
                        modifier = Modifier.size(iconSize)
                    )
                }
                Text(text = if (liked) "1.2w" else "1.1w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // 3. 评论（私密）
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { Toast.makeText(context, "私密社交，禁止公开评论，请直接私信", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = "私信", tint = iconTint, modifier = Modifier.size(iconSize))
                }
                Text(text = "私聊", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // 4. 盲盒/探索
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onNavigateToMasquerade,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "午夜盲盒", tint = Gold, modifier = Modifier.size(iconSize))
                }
                Text(text = "探索", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // 5. 分享
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "我正在 Aurelian 发现一位品位非凡的会员。快来开启您的私密高定之旅。")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "分享会员主页")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "分享", tint = iconTint, modifier = Modifier.size(iconSize))
                }
                Text(text = "分享", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }"""

if "极简高端" not in content:
    content = content.replace(old_actions, new_actions)

with open(file_path, "w") as f:
    f.write(content)
