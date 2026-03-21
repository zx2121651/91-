package com.aurelian.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(eventName: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1519671482749-fd09871171dd?auto=format&fit=crop&w=1200&q=80",
                contentDescription = "活动头图",
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
                            startY = 500f
                        )
                    )
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Gold)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = eventName.uppercase(),
                color = Silver,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Typography.bodyLarge.fontFamily
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "日期", tint = Gold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("11月15日, 晚上 9:00", color = Silver, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = "地点", tint = Gold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("梅费尔顶层公寓, 伦敦", color = Silver, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color(0xFF303030), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "一个充满神秘与奢华的夜晚。加入我们精心策划的体验，在现场管弦乐演奏与定制美食的背景下，擦出意义非凡的火花。",
                color = Silver,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("着装礼仪", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Black Tie 与假面。敬请各位贵宾严格遵守着装规范。", color = Silver, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("请求获取请柬", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
