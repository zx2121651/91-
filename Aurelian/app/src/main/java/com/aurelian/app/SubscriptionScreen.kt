package com.aurelian.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("AURELIAN 会籍", color = Gold, style = Typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF131313),
                titleContentColor = Gold
            )
        )

        HorizontalDivider(color = Color(0xFF303030), thickness = 1.dp)

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "提升您的私密社交体验",
            color = Silver,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "专为世界上最具影响力的 1% 精英打造的三个阶层。",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tier 1: The Elite
        MembershipCard(
            title = "The Elite 菁英",
            price = "¥1,999 /月",
            features = listOf("无限次点赞与私信", "基础高净值身份认证", "每月 1 次全球私密沙龙资格"),
            isPopular = false,
            cardColor = Color(0xFF1B1B1B)
        )

        // Tier 2: The Royal (Featured)
        MembershipCard(
            title = "The Royal 皇家",
            price = "¥5,999 /月",
            features = listOf("包含 The Elite 所有特权", "全球漫游与隐身无痕模式", "优先匹配当地 TOP 10% 佳宾", "专属私人礼宾部 (工作日)"),
            isPopular = true,
            cardColor = Color(0xFF2A2A2A),
            borderColor = Gold
        )

        // Tier 3: The Sovereign
        MembershipCard(
            title = "The Sovereign 尊贵黑卡",
            price = "仅限内推邀请",
            features = listOf("无需排队，直接对接财阀及名流", "24/7 全天候黑金级私人管家", "所有全球高定闭门派对 VIP 席位", "完全脱离线上算法的定制化引荐"),
            isPopular = false,
            cardColor = Black,
            borderColor = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun MembershipCard(
    title: String,
    price: String,
    features: List<String>,
    isPopular: Boolean,
    cardColor: Color,
    borderColor: Color = Color.Transparent
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (borderColor != Color.Transparent) 1.dp else 0.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (isPopular) {
                Text(
                    text = "最受高净值人群青睐",
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(title, color = if (isPopular) Gold else Silver, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = Typography.bodyLarge.fontFamily)
            Spacer(modifier = Modifier.height(8.dp))
            Text(price, color = Silver, fontSize = 20.sp, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(24.dp))

            features.forEach { feature ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(feature, color = Silver, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (title.contains("Sovereign")) Color(0xFF303030) else Gold,
                    contentColor = if (title.contains("Sovereign")) Silver else Black
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (title.contains("Sovereign")) "提交资产认证" else "选择该会籍",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
