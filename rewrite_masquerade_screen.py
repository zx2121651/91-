import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MasqueradeScreen.kt"

new_screen = """package com.aurelian.app

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasqueradeScreen(
    onBack: () -> Unit,
    viewModel: MasqueradeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    var answerText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStatus()
    }

    // 缓慢放大的神秘背景动画
    val infiniteTransition = rememberInfiniteTransition(label = "bgScale")
    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgScaleAnim"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF07070F))) {

        // 核心神秘感装饰：放大居中的超大星标或图片背景
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Gold.copy(alpha = 0.05f),
                modifier = Modifier.size(400.dp).scale(bgScale)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // TopBar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = Silver)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "AURELIAN MASQUERADE",
                    color = Gold,
                    fontSize = 14.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp)) // 对齐占位
            }

            Spacer(modifier = Modifier.height(40.dp))

            when (val state = uiState) {
                is MasqueradeUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gold)
                    }
                }
                is MasqueradeUiState.Error -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
                is MasqueradeUiState.Open -> {
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 倒计时沙漏
                        MasqueradeCountdown(endTimeMs = state.endTime)

                        Spacer(modifier = Modifier.height(60.dp))

                        // 灵魂拷问渲染
                        Text(
                            text = "今夜拷问：",
                            color = Silver.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "“\${state.question}”",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(60.dp))

                        // 答卷输入框
                        OutlinedTextField(
                            value = answerText,
                            onValueChange = { if (it.length <= 150) answerText = it },
                            placeholder = { Text("戴上面具，写下内心深处的答案...", color = Color.Gray, fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Gold
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Text(
                            text = "\${answerText.length} / 150",
                            color = Silver.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // 递交答卷按钮
                        Button(
                            onClick = {
                                if (answerText.isBlank()) {
                                    Toast.makeText(context, "答卷不能为空", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.submitAnswer(
                                    answer = answerText,
                                    onSuccess = {
                                        Toast.makeText(context, "面具已为您戴好", Toast.LENGTH_LONG).show()
                                        // 延迟后退回主页，防止重复答题
                                        onBack()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            shape = RoundedCornerShape(27.dp),
                            enabled = !submitState
                        ) {
                            if (submitState) {
                                CircularProgressIndicator(color = DeepBlack, modifier = Modifier.size(24.dp))
                            } else {
                                Text("递交答卷", color = DeepBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                is MasqueradeUiState.ClosedOrSubmitted -> {
                    // 已结束或已提交的状态
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "面具",
                            tint = Gold,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = state.question,
                            color = Silver,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(60.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
                        ) {
                            Text("返回主页", color = Silver)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MasqueradeCountdown(endTimeMs: Long) {
    var remainingTime by remember { mutableStateOf(endTimeMs - System.currentTimeMillis()) }

    LaunchedEffect(endTimeMs) {
        while (remainingTime > 0) {
            delay(1000)
            remainingTime = endTimeMs - System.currentTimeMillis()
        }
    }

    val totalSeconds = (remainingTime / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val timeString = String.format("%02d : %02d : %02d", hours, minutes, seconds)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("距离舞池落幕", color = Silver.copy(alpha = 0.5f), fontSize = 12.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = timeString,
            color = Gold,
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp
        )
    }
}
"""

with open(file_path, "w") as f:
    f.write(new_screen)
