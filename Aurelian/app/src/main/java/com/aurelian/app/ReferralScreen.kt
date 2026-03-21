package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
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
fun ReferralScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        TopAppBar(
            title = { Text("内推引荐", color = Gold, style = Typography.titleLarge) },
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

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "拓展精英圈层",
            color = Silver,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Typography.bodyLarge.fontFamily,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aurelian 依靠会员的卓越品味而不断成长。请引荐那些能够代表并认同我们核心价值观的友人加入。",
            color = Color.Gray,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Remaining Referrals Box
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFF1B1B1B), RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("剩余内推名额", color = Silver, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("3", color = Gold, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Invite Link
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text("您的专属高定邀请码", color = Silver, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AURE-X79M-VQ2P", color = Gold, fontSize = 18.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { /* TODO: Copy */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Copy", tint = Silver)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* TODO */ },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("立即引荐友人", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }

        Text(
            text = "通过内推申请入会的成员，其资质审核将被加急处理，但通过率不予保证。",
            color = Color.DarkGray,
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
