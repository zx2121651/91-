package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeaCeremonyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1544256718-3b623862210c?auto=format&fit=crop&w=1200&q=80",
                contentDescription = "Oriental Elegance Hero",
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
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "东方雅集",
                color = Gold,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Typography.bodyLarge.fontFamily,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("宋婉清", color = Silver, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("高级茶艺师 / 评茶员", color = Gold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "传承宋代点茶与武夷岩茶的古法冲泡，专注于顶级稀有老茶的品鉴与沉浸式文化体验。",
                color = Silver,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color(0xFF303030), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Text("私享服务", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))

            val services = listOf("明前龙井私享", "百年普洱品鉴", "宋代点茶体验")
            services.forEach { service ->
                Text("• $service", color = Silver, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B1B1B), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("仅限预约 / 每日仅限一席", color = Color.Gray, fontSize = 14.sp, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("预约私人品茗", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
