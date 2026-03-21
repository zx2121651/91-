package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(onNavigateToSettings: () -> Unit, onNavigateToReferral: () -> Unit, onNavigateToSubscription: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
                contentDescription = "个人主页头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DeepBlack),
                            startY = 600f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text("林静恩, 27", color = Silver, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("中国, 北京 (静安区)", color = Gold, fontSize = 16.sp)
            }
        }

        Text(
            text = "古典乐与现代主义建筑的鉴赏者。寻找一位能共同探索世界、在智识上产生共鸣的伴侣。",
            color = Silver,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            modifier = Modifier.padding(24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("无感", letterSpacing = 1.5.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("心动", letterSpacing = 1.5.sp)
            }
        }

        // Extra Management Options
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onNavigateToSubscription,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B1B), contentColor = Gold),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("尊享会籍管理", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onNavigateToReferral,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B1B), contentColor = Gold),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("内推引荐通道", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onNavigateToSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B1B), contentColor = Silver),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("偏好设置", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
