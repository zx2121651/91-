package com.aurelian.app


import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material.icons.filled.Star

import android.net.Uri
import android.widget.Toast
import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.DateRange
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import coil.compose.AsyncImage

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainFeedScreen(
    onNavigateToEvents: () -> Unit = {},
    onNavigateToMasquerade: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToPublish: (Boolean) -> Unit = {},
    viewModel: MainFeedViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    var matchedUser by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.matchEvent.collect { user ->
            matchedUser = user
        }
    }


    when (val state = uiState) {
        is FeedUiState.Loading -> {
            // High-end Skeleton Loader (Breathing Animation)
            val infiniteTransition = rememberInfiniteTransition(label = "breathing")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepBlack),
                contentAlignment = Alignment.Center
            ) {
                // Instead of a cheap spinner, we show a glowing luxury motif or placeholder
                Text(
                    text = "AURELIAN NIGHT",
                    color = Gold.copy(alpha = alpha),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                )
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

                // Smart preloading and cancelling based on scroll state
                LaunchedEffect(pagerState.currentPage) {
                    val currentIdx = pagerState.currentPage

                    // Preload next and next+1
                    if (currentIdx + 1 < users.size) viewModel.preloadVideo(users[currentIdx + 1].videoUrl)
                    if (currentIdx + 2 < users.size) viewModel.preloadVideo(users[currentIdx + 2].videoUrl)

                    // Cancel preloading for far away items to save bandwidth
                    if (currentIdx - 2 >= 0) viewModel.cancelPreload(users[currentIdx - 2].videoUrl)
                    if (currentIdx + 3 < users.size) viewModel.cancelPreload(users[currentIdx + 3].videoUrl)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeepBlack)
                    ) { page ->
                        FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToProfile = onNavigateToProfile, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) })
                    }

                    // 顶部右侧按钮容器
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 草稿提示弹窗状态
                        var showDraftPrompt by remember { mutableStateOf(false) }

                        if (showDraftPrompt) {
                            AlertDialog(
                                onDismissRequest = { showDraftPrompt = false },
                                title = { Text("未完成的动态", color = Gold, fontWeight = FontWeight.Bold) },
                                text = { Text("您有一份未完成的高定剪辑草稿，是否继续编辑？", color = Silver) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showDraftPrompt = false
                                        onNavigateToPublish(true) // 恢复草稿
                                    }) {
                                        Text("继续编辑", color = Gold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showDraftPrompt = false
                                        DraftManager.clearDraft(context)
                                        onNavigateToPublish(false) // 开启新拍摄
                                    }) {
                                        Text("放弃并重拍", color = Color.Gray)
                                    }
                                },
                                containerColor = Color(0xFF1B1B1B)
                            )
                        }

                        // 发布视频按钮
                        IconButton(onClick = {
                            if (DraftManager.hasDraft(context)) {
                                showDraftPrompt = true
                            } else {
                                onNavigateToPublish(false)
                            }
                        }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.AddCircle,
                                contentDescription = "发布动态",
                                tint = Silver.copy(alpha = 0.9f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
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
fun FeedItem(user: User, isSelected: Boolean, onNavigateToProfile: (String) -> Unit, onNavigateToMasquerade: () -> Unit, onLike: () -> Unit) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Use DisposableEffect tied to user.id or URL to ensure it correctly manages the player instance
    DisposableEffect(user.id) {
        val player = ExoPlayerPool.acquirePlayer(context)

        val cacheDataSourceFactory = VideoCacheManager.getCacheDataSourceFactory()
        val mediaItem = MediaItem.fromUri(Uri.parse(user.videoUrl))
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player.apply {
            setMediaSource(mediaSource)
            repeatMode = Player.REPEAT_MODE_ALL

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isVideoReady = true
                    }
                }
            })
            prepare()
        }

        exoPlayer = player

        onDispose {
            // Return to pool instead of releasing completely
            ExoPlayerPool.releasePlayer(player)
            exoPlayer = null
            isVideoReady = false
        }
    }

    LaunchedEffect(isSelected, exoPlayer) {
        if (isSelected) {
            exoPlayer?.play()
        } else {
            exoPlayer?.pause()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Base Layer: Video Player
        exoPlayer?.let { player ->
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        val videoSurfaceView = this.videoSurfaceView
                        if (videoSurfaceView is android.view.SurfaceView) {
                            videoSurfaceView.setZOrderMediaOverlay(false)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.player = player
                }
            )
        }

        // Overlay Layer 1: Cover Image Placeholder
        AnimatedVisibility(
            visible = !isVideoReady,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(700))
        ) {
            AsyncImage(
                model = user.videoUrl,
                contentDescription = "视频封面" /* 视频加载前的封面图片占位 */,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(DeepBlack)
            )
        }

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
                .clickable { onNavigateToProfile(user.id) } // Clicking user info navigates to profile
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

        // 极简高端的操作栏 (右侧)
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
            var liked by remember { mutableStateOf(false) }
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
        }
    }
}
