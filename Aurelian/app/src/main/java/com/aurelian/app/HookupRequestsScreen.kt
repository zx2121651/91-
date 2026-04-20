package com.aurelian.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HookupRequestsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit // 这里一般传 userName 或 userId 都可以
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var requests by remember { mutableStateOf<List<HookupRequestItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadRequests(type: String) {
        isLoading = true
        coroutineScope.launch {
            try {
                val res = NetworkClient.apiService.getHookupRequests(type = type)
                requests = res.data
            } catch (e: Exception) {
                Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedTab) {
        loadRequests(if (selectedTab == 0) "RECEIVED" else "SENT")
    }

    val respondToRequest = { id: String, action: String, userName: String ->
        coroutineScope.launch {
            try {
                val response = NetworkClient.apiService.respondHookupRequest(id, RespondHookupRequest(action))
                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                if (action == "ACCEPT" && response.data?.conversationId != null) {
                    onNavigateToChat(userName)
                } else {
                    loadRequests("RECEIVED")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "处理异常", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("专属邀约信箱", color = Silver, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Silver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Tab 切换：收到的邀约 vs 发出的邀约
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepBlack,
                contentColor = Gold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Gold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("收到的邀约", color = if (selectedTab == 0) Gold else Silver) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("发出的心意", color = if (selectedTab == 1) Gold else Silver) }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            } else if (requests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("这里很安静，暂时没有新邀约。", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(requests) { req ->
                        HookupRequestCard(
                            item = req,
                            isReceived = selectedTab == 0,
                            onProfileClick = { onNavigateToProfile(req.userId) },
                            onAccept = { respondToRequest(req.requestId, "ACCEPT", req.name) },
                            onReject = { respondToRequest(req.requestId, "REJECT", req.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HookupRequestCard(
    item: HookupRequestItem,
    isReceived: Boolean,
    onProfileClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.DarkGray, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                AsyncImage(
                    model = item.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.DarkGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name, color = Silver, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val statusText = when(item.status) {
                        "PENDING" -> "等待回应"
                        "ACCEPTED" -> "已接受"
                        "REJECTED" -> "已婉拒"
                        "EXPIRED" -> "已过期"
                        else -> item.status
                    }
                    Text(text = "[\${item.meetingType}] · \$statusText", color = Gold, fontSize = 12.sp)
                }
            }

            // Note Box
            if (item.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().background(DeepBlack, RoundedCornerShape(8.dp)).padding(12.dp)
                ) {
                    Text(text = "“\${item.note}”", color = Color.White.copy(alpha=0.8f), fontSize = 14.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                }
            }

            // Action Buttons (Only for PENDING and RECEIVED)
            if (isReceived && item.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("婉拒", color = Silver)
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text("赴约", color = DeepBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
