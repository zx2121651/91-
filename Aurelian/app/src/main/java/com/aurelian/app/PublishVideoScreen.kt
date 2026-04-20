package com.aurelian.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishVideoScreen(onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布动态", color = Silver, fontWeight = FontWeight.Medium) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 模拟视频预览区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "预览", tint = Silver, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击选择或录制短视频", color = Silver.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题", color = Silver.copy(alpha = 0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 描述输入
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("说点什么...", color = Silver.copy(alpha = 0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            // 发布按钮
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
                                PublishVideoRequest(title, bio, "media_mock_${System.currentTimeMillis()}")
                            )
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                            onBack()
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
                    Text("立即发布", color = DeepBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
