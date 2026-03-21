package com.aurelian.app

import androidx.compose.material.icons.filled.Star

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainFeedScreen(onNavigateToMasquerade: () -> Unit = {}) {
    val users = listOf(
        User(1, "苏婉, 26", "独立艺术策展人，游历全球的旅者。", "上海, 中国", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
        User(2, "林静恩, 27", "古典乐与现代主义建筑的鉴赏者。", "北京, 中国", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
        User(3, "Sofia, 29", "品酒师，私人酒庄主理人。", "巴黎, 法国", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4")
    )

    val pagerState = rememberPagerState(pageCount = { users.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) { page ->
        FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToMasquerade = onNavigateToMasquerade)
    }
}

@Composable
fun FeedItem(user: User, isSelected: Boolean, onNavigateToMasquerade: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(user.videoUrl))
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
        }
    }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Video Player Background
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for bottom text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000)),
                        startY = 500f
                    )
                )
        )

        // User Info Overlay (Bottom Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 80.dp) // Leave space for BottomNav
        ) {
            Text(user.name, color = Silver, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(user.location, color = Gold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(user.bio, color = Silver, fontSize = 16.sp)
        }

        // Actions Overlay (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { /* TODO */ }) {
            IconButton(onClick = onNavigateToMasquerade) {
                Icon(Icons.Default.Star, contentDescription = "午夜盲盒", tint = Gold, modifier = Modifier.size(32.dp))
            }
                Icon(Icons.Default.FavoriteBorder, contentDescription = "喜欢", tint = Gold, modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MailOutline, contentDescription = "评论", tint = Gold, modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Share, contentDescription = "分享", tint = Gold, modifier = Modifier.size(32.dp))
            }
        }
    }
}
