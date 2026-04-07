package com.aurelian.app

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditScreen(
    videoUri: String,
    onBack: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("滤镜") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val filters = listOf("原画", "胶片(Film)", "黑白(B&W)", "电影感", "漏光(Leak)")
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("高级编辑", color = Silver, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            // 视频预览区域 (模拟)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                // TODO: 真实项目中这里应替换为 ExoPlayer 实例并应用所选滤镜
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "预览视频", tint = Silver, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("应用滤镜: $selectedFilter", color = Gold, fontSize = 14.sp)
                }
            }

            // 文案输入区
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("为这段瞬间命名...", color = Silver.copy(alpha = 0.5f)) },
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

            // 编辑功能 Tab 区 (滤镜 / 配乐 / 裁剪)
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
                        Text("高格调配乐库即将上线（古典 / 爵士 / 氛围电子）", color = Silver.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                    "裁剪" -> {
                        Text("拖动以裁剪视频长度", color = Silver.copy(alpha = 0.5f), fontSize = 14.sp)
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
                        coroutineScope.launch {
                            try {
                                val response = NetworkClient.apiService.publishVideo(
                                    PublishVideoRequest(title, bio, "media_mock_camera_${System.currentTimeMillis()}")
                                )
                                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                                onNavigateToFeed()
                            } catch (e: Exception) {
                                Toast.makeText(context, "发布失败: ${e.message}", Toast.LENGTH_SHORT).show()
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
