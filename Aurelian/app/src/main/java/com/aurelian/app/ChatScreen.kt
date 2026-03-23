package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(userName) {
        // Here we use userName as convId. Ideally we pass convId via Navigation.
        viewModel.loadMessages(userName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        TopAppBar(
            title = { Text(userName, color = Gold, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gold)
                }
            },
            actions = {
                // Request Invitation Button (Placeholder for elite feature)
                IconButton(onClick = onNavigateToInvite) {
                    Icon(Icons.Default.Email, contentDescription = "高定私人邀约", tint = Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF131313))
        )

        when (val state = uiState) {
            is ChatUiState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            }
            is ChatUiState.Error -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "加载失败: ${state.message}", color = Color.Red)
                }
            }
            is ChatUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    reverseLayout = false // In real app, might want to reverse for chat, but let's stick to standard top-down for now
                ) {
                    items(state.messages) { message ->
                        MessageBubble(
                            message = message.content,
                            isMe = message.sender.id == "me",
                            timestamp = message.timestamp,
                            isInvitation = false // In a real scenario, this flag would come from a richer Message model or subtype
                        )
                    }
                }
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B1B1B))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("输入消息...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Silver,
                    unfocusedTextColor = Silver,
                    cursorColor = Gold
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Gold, RoundedCornerShape(24.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Black)
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: String,
    isMe: Boolean,
    timestamp: String,
    isInvitation: Boolean = false,
    inviteType: String = "",
    inviteTime: String = "",
    inviteLocation: String = "",
    inviteMessage: String = ""
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bgColor = if (isMe) Color(0xFF3A2B15) else Color(0xFF262626) // Deep gold tint for 'me'
    val textColor = if (isMe) Gold else Silver

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = alignment
    ) {
        if (isInvitation) {
            InvitationCard(inviteType, inviteTime, inviteLocation, inviteMessage)
        } else {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .background(bgColor)
                    .padding(16.dp)
            ) {
                Text(text = message, color = textColor, fontSize = 16.sp, lineHeight = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = timestamp, color = Color.DarkGray, fontSize = 12.sp)
    }
}

@Composable
fun InvitationCard(type: String, time: String, location: String, message: String) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B1B1B))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Email, contentDescription = "Invitation", tint = Gold, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("专属高定私人邀约", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(type, color = Silver, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(time, color = Silver, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "Location", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(location, color = Silver, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { /*TODO*/ }) {
                    Text("婉拒", color = Color.Gray)
                }
                Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black)) {
                    Text("接受邀约", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
