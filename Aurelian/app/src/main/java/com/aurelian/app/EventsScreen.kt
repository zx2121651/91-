package com.aurelian.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun EventsScreen(onNavigateToEventDetails: (String) -> Unit) {
    val events = listOf(
        EventItemData("云端流金·鸡尾酒会", "11月5日", "晚上 7:00", "碎片大厦 (The Shard)", "https://images.unsplash.com/photo-1574096079513-d8259312b785?auto=format&fit=crop&w=800&q=80"),
        EventItemData("当代艺术鉴赏夜", "11月12日", "晚上 6:00", "新邦德街私立画廊", "https://images.unsplash.com/photo-1544158498-8422fb813db1?auto=format&fit=crop&w=800&q=80")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "私密沙龙",
                color = Silver,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Hero Event
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clickable { onNavigateToEventDetails("金秋假面舞会") }
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1519671482749-fd09871171dd?auto=format&fit=crop&w=1200&q=80",
                        contentDescription = "主打活动",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DeepBlack),
                                    startY = 400f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Text("本周倾呈", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("金秋假面舞会", color = Silver, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("10月31日 • 贝尔格莱维亚庄园", color = Silver, fontSize = 14.sp)
                    }
                }
            }

            item {
                Text(
                    text = "即将举行",
                    color = Silver,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            items(events) { event ->
                EventListItem(event, onClick = { onNavigateToEventDetails(event.title) })
            }
        }
    }
}

data class EventItemData(val title: String, val date: String, val time: String, val location: String, val imageUrl: String)

@Composable
fun EventListItem(event: EventItemData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = event.imageUrl,
            contentDescription = "活动图片",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title, color = Silver, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${event.date} • ${event.time}", color = Gold, fontSize = 14.sp)
            Text(event.location, color = Color.Gray, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B1B), contentColor = Gold),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("敬请赐复", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
