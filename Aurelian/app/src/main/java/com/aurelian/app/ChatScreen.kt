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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(userName: String, onBack: () -> Unit, onNavigateToInvite: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("非常期待明天的画廊私人预览。", false, "21:14"),
            ChatMessage("我也是。听说这次展出的几幅后现代作品很值得期待。", true, "21:16"),
            ChatMessage("", true, "刚刚", isInvitation = true, inviteType = "米其林晚宴", inviteTime = "11月18日, 19:30", inviteLocation = "上海宝格丽酒店", inviteMessage = "希望能与你共进晚餐，探讨上周聊到的现代艺术。")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(userName, color = Gold, style = Typography.titleLarge, fontFamily = Typography.bodyLarge.fontFamily) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Gold)
                }
            },
            actions = {
                IconButton(onClick = onNavigateToInvite) {
                    Icon(Icons.Default.DateRange, contentDescription = "专属邀约", tint = Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF131313),
                titleContentColor = Gold
            )
        )

        HorizontalDivider(color = Color(0xFF303030), thickness = 1.dp)

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                if (msg.isInvitation) {
                    InvitationCardBubble(msg)
                } else {
                    ChatBubble(msg)
                }
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B1B1B))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("发送消息...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color(0xFF303030),
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
                        messages.add(ChatMessage(messageText, true, "刚刚"))
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Gold)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送", tint = Black)
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val time: String,
    val isInvitation: Boolean = false,
    val inviteType: String = "",
    val inviteTime: String = "",
    val inviteLocation: String = "",
    val inviteMessage: String = ""
)

@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (msg.isUser) 16.dp else 0.dp,
                        bottomEnd = if (msg.isUser) 0.dp else 16.dp
                    )
                )
                .background(
                    if (msg.isUser) Color(0xFFD4AF37) // Gold
                    else Color(0xFF1B1B1B)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = msg.text,
                    color = if (msg.isUser) Black else Silver,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.time,
                    color = if (msg.isUser) Color.DarkGray else Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun InvitationCardBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PRIVATE INVITATION", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(msg.inviteType, color = Silver, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = Typography.bodyLarge.fontFamily)
                Spacer(modifier = Modifier.height(8.dp))

                Text(msg.inviteTime, color = Silver, fontSize = 14.sp)
                Text(msg.inviteLocation, color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF303030), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("“${msg.inviteMessage}”", color = Silver, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Decline */ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4D4635)),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("婉拒")
                    }
                    Button(
                        onClick = { /* Accept */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("接受", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg.time,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
