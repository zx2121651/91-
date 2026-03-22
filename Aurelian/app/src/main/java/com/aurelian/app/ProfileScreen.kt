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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment


@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        }
        is ProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "加载失败", color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.message, color = Silver)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchProfile() }) {
                        Text("重试")
                    }
                }
            }
        }
        is ProfileUiState.Success -> {
            val profile = state.profile
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
                Text(profile.name, color = Silver, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("中国, 北京 (静安区)", color = Gold, fontSize = 16.sp)
            }
        }

        Text(
            text = "身份验证状态: " + (if (profile.isVerified) "已认证" else "未认证") + "\n会员等级: " + profile.membership,
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
    }
}