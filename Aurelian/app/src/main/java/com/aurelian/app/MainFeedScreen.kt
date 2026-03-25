package com.aurelian.app

import androidx.compose.material.icons.filled.Star

import android.net.Uri
import android.widget.Toast
import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.material3.Button

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun MainFeedScreen(
    onNavigateToMasquerade: () -> Unit = {},
    viewModel: MainFeedViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    var matchedUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        viewModel.matchEvent.collect { user ->
            matchedUser = user
        }
    }


    when (val state = uiState) {
        is FeedUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        }
        is FeedUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "加载失败", color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.message, color = Silver)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchVideos() }) {
                        Text("重试")
                    }
                }
            }
        }
        is FeedUiState.Success -> {
            val users = state.users
            if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                    Text("暂无视频", color = Silver)
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { users.size })

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepBlack)
                ) { page ->
                    FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) })
                }

                matchedUser?.let { user ->
                    AlertDialog(
                        onDismissRequest = { matchedUser = null },
                        title = { Text(text = "恭喜匹配！", color = Gold, fontWeight = FontWeight.Bold) },
                        text = { Text("您与 ${user.name} 相互心动了。缘分在午夜绽放，立刻去打个招呼吧！", color = Silver) },
                        confirmButton = {
                            TextButton(onClick = { matchedUser = null }) {
                                Text("立即聊天", color = Gold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { matchedUser = null }) {
                                Text("继续浏览", color = Color.Gray)
                            }
                        },
                        containerColor = Color(0xFF1B1B1B)
                    )
                }
            }
        }
    }
}

@Composable
fun FeedItem(user: User, isSelected: Boolean, onNavigateToMasquerade: () -> Unit, onLike: () -> Unit) {
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

        // Minimalist Gradient overlay for readability at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.35f) // Gradient only covers the bottom 35%
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xE6131313)), // DeepBlack with 90% opacity
                        startY = 0f
                    )
                )
        )

        // Extremely clean User Info Overlay (Bottom Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 90.dp) // Leave MORE space for Right Actions to avoid overlap
        ) {
            Text(
                text = user.name,
                color = Silver,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.location.uppercase(),
                color = Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = user.bio,
                color = Silver.copy(alpha = 0.8f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2, // Restrict bio to 2 lines max
                overflow = TextOverflow.Ellipsis
            )
        }

        // Minimalist Actions Overlay (Right Side)
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
        }
    }
}
