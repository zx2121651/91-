package com.aurelian.app

import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun VideoEditScreen(
    videoUri: String,
    isDraft: Boolean = false,
    onBack: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("滤镜") }

    val filters = listOf("原画", "胶片(Film)", "黑白(B&W)", "电影感", "漏光(Leak)")
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    val audioTracks = listOf("原声", "古典弦乐", "慵懒爵士", "深夜黑胶", "氛围电子")
    var selectedAudio by remember { mutableStateOf(audioTracks[0]) }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var videoDurationMs by remember { mutableStateOf(15000L) }
    var sliderRange by remember { mutableStateOf(0f..1f) }

    var showDraftDialog by remember { mutableStateOf(false) }

    // 初始化草稿恢复逻辑
    LaunchedEffect(isDraft) {
        if (isDraft) {
            DraftManager.getDraft(context)?.let { draft ->
                title = draft.title
                bio = draft.bio
                selectedFilter = draft.selectedFilter
                selectedAudio = draft.selectedAudio
                sliderRange = draft.sliderStart..draft.sliderEnd
            }
        }
    }

    // 拦截返回事件
    val handleBackPress = {
        // 如果内容有变动或者非空，则提示保存草稿
        if (title.isNotBlank() || selectedFilter != "原画" || selectedAudio != "原声" || sliderRange.start > 0f || sliderRange.endInclusive < 1f) {
            showDraftDialog = true
        } else {
            exoPlayer?.stop()
            onBack()
        }
    }

    BackHandler {
        handleBackPress()
    }

    // 退出提示保存草稿的弹窗
    if (showDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            title = { Text("保存草稿", color = Gold, fontWeight = FontWeight.Bold) },
            text = { Text("您还有未完成的剪辑，是否保存到草稿箱以便下次继续？", color = Silver) },
            confirmButton = {
                TextButton(onClick = {
                    val draft = DraftData(
                        videoUri = videoUri,
                        title = title,
                        bio = bio,
                        selectedFilter = selectedFilter,
                        selectedAudio = selectedAudio,
                        sliderStart = sliderRange.start,
                        sliderEnd = sliderRange.endInclusive,
                        timestamp = System.currentTimeMillis()
                    )
                    DraftManager.saveDraft(context, draft)
                    showDraftDialog = false
                    exoPlayer?.stop()
                    onBack()
                }) {
                    Text("保存", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    DraftManager.clearDraft(context)
                    showDraftDialog = false
                    exoPlayer?.stop()
                    onBack()
                }) {
                    Text("不保存", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1B1B1B)
        )
    }

    DisposableEffect(videoUri) {
        val player = ExoPlayer.Builder(context).build().apply {
            val uris = videoUri.split(",")
            val mediaItems = uris.map { MediaItem.fromUri(Uri.parse(it)) }
            setMediaItems(mediaItems)
            repeatMode = Player.REPEAT_MODE_ALL
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val dur = duration
                        if (dur > 0) {
                            videoDurationMs = dur
                        }
                    }
                }
            })
            prepare()
            play()
        }
        exoPlayer = player

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    // 当滑块改变时，调整播放器的播放区间

    // 实时更新播放器的滤镜效果
    LaunchedEffect(selectedFilter) {
        exoPlayer?.let { player ->
            val effects = VideoEditorCore.createVideoEffects(selectedFilter, null) // 预览时不加文字水印，仅滤镜
            player.setVideoEffects(effects)
        }
    }

    LaunchedEffect(sliderRange) {
        exoPlayer?.let { player ->
            val startMs = (sliderRange.start * videoDurationMs).toLong()
            player.seekTo(startMs)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("高级编辑", color = Silver, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = handleBackPress) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Silver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 视频预览区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                exoPlayer?.let { player ->
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = player
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 右上角状态标签
                if (selectedFilter != "原画") {
                    Text(
                        "已应用: \$selectedFilter",
                        color = Gold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(DeepBlack.copy(alpha=0.8f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 文案输入区
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("添加专属文字水印...", color = Silver.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("讲述背后的故事...", color = Silver.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 编辑功能 Tab 区
            TabRow(
                selectedTabIndex = listOf("滤镜", "配乐", "裁剪").indexOf(selectedTab),
                containerColor = DeepBlack,
                contentColor = Gold,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty()) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[listOf("滤镜", "配乐", "裁剪").indexOf(selectedTab)]),
                            color = Gold
                        )
                    }
                }
            ) {
                listOf("滤镜", "配乐", "裁剪").forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab, color = if (selectedTab == tab) Gold else Silver) }
                    )
                }
            }

            // 编辑面板内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(DeepBlack)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTab) {
                    "滤镜" -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filters) { filterName ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedFilter == filterName) Color(0xFF333333) else Color(0xFF1E1E1E))
                                        .border(
                                            2.dp,
                                            if (selectedFilter == filterName) Gold else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedFilter = filterName },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(filterName, color = Silver, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    "配乐" -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(audioTracks) { trackName ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedAudio == trackName) Color(0xFF333333) else Color(0xFF1E1E1E))
                                        .border(
                                            2.dp,
                                            if (selectedAudio == trackName) Gold else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedAudio = trackName },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(trackName, color = Silver, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    "裁剪" -> {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val startSec = (sliderRange.start * videoDurationMs / 1000f).roundToInt()
                            val endSec = (sliderRange.endInclusive * videoDurationMs / 1000f).roundToInt()
                            Text(
                                "已选择: " + startSec + " 秒 - " + endSec + " 秒",
                                color = Gold,
                                fontSize = 14.sp
                            )
                            RangeSlider(
                                value = sliderRange,
                                onValueChange = { sliderRange = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Gold,
                                    activeTrackColor = Gold,
                                    inactiveTrackColor = Color.DarkGray
                                )
                            )
                        }
                    }
                }
            }

            // 底部发布按钮
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "请填写标题", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isPublishing = true
                        exoPlayer?.pause()

                        coroutineScope.launch {
                            try {
                                // 判断是否需要底层裁剪处理
                                val needsTrim = sliderRange.start > 0.01f || sliderRange.endInclusive < 0.99f

                                if (needsTrim) {
                                    Toast.makeText(context, "正在进行专业影片剪辑...", Toast.LENGTH_SHORT).show()
                                    val startMs = (sliderRange.start * videoDurationMs).toLong()
                                    val endMs = (sliderRange.endInclusive * videoDurationMs).toLong()
                                    val outputFile = File(context.cacheDir, "edited_video_\${System.currentTimeMillis()}.mp4")

                                    // 开启后台真实裁剪
                                    withContext(Dispatchers.IO) {
                                        VideoEditorCore.processVideo(
                                            context = context,
                                            inputUri = Uri.parse(videoUri),
                                            startMs = startMs,
                                            endMs = endMs,
                                            filterName = selectedFilter,
                                            watermarkText = title,
                                            audioTrack = selectedAudio,
                                            outputFile = outputFile
                                        )
                                    }
                                }

                                // 发起 Mock 的网络请求发布
                                val response = NetworkClient.apiService.publishVideo(
                                    PublishVideoRequest(title, bio, "media_\${System.currentTimeMillis()}", selectedAudio)
                                )
                                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                                DraftManager.clearDraft(context)
                                onNavigateToFeed()
                            } catch (e: Exception) {
                                Toast.makeText(context, "剪辑或发布失败: \${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isPublishing = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isPublishing
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(color = DeepBlack, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = DeepBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("完成并发布", color = DeepBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
