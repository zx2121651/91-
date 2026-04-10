package com.aurelian.app

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 简单的复用 ViewModel，支持加载特定 userId 的作品流
class UserFeedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState
    private var nextCursor: String? = null

    fun loadUserVideos(userId: String) {
        viewModelScope.launch {
            try {
                // 如果 userId 是 'me' 或者是其他用户的真实 ID
                val response = NetworkClient.apiService.getFeedVideos(userId = userId)
                nextCursor = response.nextCursor
                _uiState.value = FeedUiState.Success(response.data)
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error("获取作品流失败: \${e.message}")
            }
        }
    }

    fun likeUser(user: User) {
        viewModelScope.launch {
            try {
                NetworkClient.apiService.likeUser(LikeRequest(user.id))
            } catch (e: Exception) {
                // Ignore silent failure
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserFeedScreen(
    userId: String,
    initialIndex: Int,
    onNavigateBack: () -> Unit,
    viewModel: UserFeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (!hasInitialized) {
            viewModel.loadUserVideos(userId)
            hasInitialized = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        when (val state = uiState) {
            is FeedUiState.Loading -> {
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Silver)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold)
                        ) {
                            Text("返回主页", color = DeepBlack)
                        }
                    }
                }
            }
            is FeedUiState.Success -> {
                val users = state.users
                if (users.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("该会员暂无公开的剪影", color = Silver)
                    }
                } else {
                    val initialPage = if (initialIndex in users.indices) initialIndex else 0
                    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { users.size })

                    var showCommentsSheet by remember { mutableStateOf(false) }
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    val context = LocalContext.current

                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().background(DeepBlack)
                    ) { page ->
                        // 复用现有的 FeedItem 展示逻辑
                        FeedItem(
                            user = users[page],
                            isSelected = page == pagerState.currentPage,
                            onNavigateToProfile = {}, // 已经在个人主页的流里了，不需要再跳
                            onNavigateToMasquerade = {}, // 隐藏盲盒入口
                            onLike = { viewModel.likeUser(users[page]) },
                            onShowComments = { showCommentsSheet = true }
                        )
                    }

                    // 顶部返回按钮
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 48.dp, start = 16.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Silver.copy(alpha = 0.8f), modifier = Modifier.size(32.dp))
                    }

                    // 评论弹层
                    if (showCommentsSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showCommentsSheet = false },
                            sheetState = sheetState,
                            containerColor = DeepBlack,
                            scrimColor = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.7f)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "私密讨论区",
                                    color = Gold,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Mock 评论流
                                androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(5) { index ->
                                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.DarkGray))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("匿名高定会员 \${index + 1}", color = Silver.copy(alpha=0.7f), fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("这里的氛围太棒了，简直是数字时代的凡尔赛宫。", color = Color.White, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }

                                var commentText by remember { mutableStateOf("") }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.OutlinedTextField(
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        placeholder = { Text("在此留下您的品位...", color = Color.Gray, fontSize = 14.sp) },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Gold,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(25.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = {
                                            if (commentText.isNotBlank()) {
                                                commentText = ""
                                                Toast.makeText(context, "留言已发送", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(44.dp).background(Gold, CircleShape)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "发送", tint = DeepBlack)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
