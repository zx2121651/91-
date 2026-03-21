package com.aurelian.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun MainFeedScreen() {
    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        // Simulated Full Screen Video/Image
        AsyncImage(
            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
            contentDescription = "Feed Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Gradient overlay for bottom text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xAA000000)),
                        startY = 500f
                    )
                )
        )

        // User Info Overlay (Bottom Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 80.dp) // Leave space for BottomNav
        ) {
            Text("苏婉, 26", color = Silver, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("上海, 中国", color = Gold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("独立艺术策展人，游历全球的旅者。", color = Silver, fontSize = 16.sp)
        }

        // Actions Overlay (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "喜欢", tint = Gold, modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MailOutline, contentDescription = "评论", tint = Gold, modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Share, contentDescription = "分享", tint = Gold, modifier = Modifier.size(32.dp))
            }
        }
    }
}
