package com.aurelian.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.material.icons.Icons
import androidx.material.icons.filled.ArrowBack
import androidx.material.icons.filled.MoreVert
import androidx.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String,
    onBack: () -> Unit,
    onNavigateToInvite: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    var textState by remember { mutableStateOf("") }
    var isEphemeralMode by remember { mutableStateOf(false) }

    // Mock convId for this demo. In a real app, pass convId from argument.
    val convId = "conv_12345"

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(convId) {
        viewModel.loadMessages(convId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(userName, color = Silver, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Active now", color = Gold, fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Silver)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToInvite) {
                        Icon(Icons.Default.MoreVert, "More", tint = Silver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ChatUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gold)
                    }
                }
                is ChatUiState.Error -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is ChatUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        reverseLayout = true // 新消息在底部
                    ) {
                        items(state.messages) { msg ->
                            EphemeralChatBubble(
                                message = msg,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            // Chat Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlack)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 阅后即猚开关
                IconButton(
                    onClick = { isEphemeralMode = !isEphemeralMode },
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isEphemeralMode) Color(0xFFE53935) else Color.DarkGray, CircleShape)
                ) {
                    Text("🔥", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = {
                        Text(
                            if (isEphemeralMode) "燃烧的信息将不留痕迹..." else "留互您的品位...",
                            color = if (isEphemeralMode) Color(0xFFE53935).copy(alpha=0.7f) else Color.Gray,
                            fontSize = 14.sp
                    )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEphemeralMode) Color(0xFFE53935) else Gold,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 40.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textState.isNotBlank()) {
                            viewModel.sendMessage(convId, textState, isEphemeralMode)
                            textState = ""
                        }
                    },
                    modifier = Modifier.size(44.dp).background(if (isEphemeralMode) Color(0xFFEM3935) else Gold, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = DeepBlack)
                }
            }
        }
    }
}

@Composable
fun EphemeralChatBubble(message: Message, viewModel: ChatViewModel) {
    var isVisible by remember { mutableStateOf(true) }

    // 触发消失判断
    LaunchedEffect(message.expiresAt) {
        if (message.expiresAt != null && viewModel.getRemainingTime(message.expiresAt) <= 0f) {
            isVisible = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(500)) + shrinkVertically(animationSpec = tween(500))
    ) {
        val alignment = if (message.isMe) Alignment.End else Alignment.Start
        val bgColor = if (message.isMe) Color(0xFF1B1B1B) else Color(0xFF2A2A2A)

        // 阅后即猚状态分析
        val isBurnMode = message.isEphemeral
        val isUnreadBurn = isBurnMode && message.expiresAt == null && message.readAt == null && !message.isMe

        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            contentAlignment = if (message.isMe) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
            ) {

                if (message.isMe && message.expiresAt != null) {
                    BurncountdownIndicator(message.expiresAt, viewModel)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isMe) 16.dp else 4.dp,
                            bottomEnd = if (message.isMe) 4.dp else 16.dp
                        ))
                        .background(if (isBurnMode) Color(0xF3B1010) else bgColor)
                        .clickable(enabled = isUnreadBurn) {
                            if (isUnreadBurn) {
                                viewModel.triggerRead(message.msgId)
                            }
                        }
                        .padding(12.dp)
                ) {
                    if (isUnreadBurn) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔅", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "点击查看 (燃烧倒数 ${message.ephemeralDurationSeconds} 秒)",
                                color = Color(0xFFE53935),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = message.content,
                            color = if (isBurnMode) Color(0xFFFFCDD2) else Silver,
                            fontSize = 15.sp,
                            modifier = Modifier.blur(
                                radius = if (isUnreadBurn) 8.dp else 0.dp
                            )
                        )
                    }
                }

                if (!message.isMe && message.expiresAt != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    BurncountdownIndicator(message.expiresAt, viewModel)
                }
            }
        }
    }
}

@Composable
fun BurncountdownIndicator(expiresAtStr: String, viewModel: ChatViewModel) {
    var remainingSec by remember { mutableStateOf(viewModel.getRemainingTime(expiresAtStr)) }

    LaunchedEffect(expiresAtStr) {
        while (true) {
            kotlinx.coroutines.delay(50) // 50ms 一帧
            val rem = viewModel.getRemainingTime(expiresAtStr)
            remainingSec = rem
            if (rem <= 0f) break
        }
    }

    if (remainingSec > 0f) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
            CircularProgressIndicator(
                progress = { (remainingSec / 5f).coerceIn(0f, 1f) },
                color = Color(0xFFE53935),
                strokeWidth = 2.dp,
                modifier = Modifier.fillMaxSize()
            )
            Text("${remainingSec.toInt()}", color = Color.White, fontSize = 10.sp)
        }
    }
}
