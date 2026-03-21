package com.aurelian.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasqueradeScreen(onBack: () -> Unit) {
    var answerText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Blurred Background / Silhouette
        AsyncImage(
            model = "https://images.unsplash.com/photo-1542282088-72c9c27ed0cd?auto=format&fit=crop&w=800&q=80",
            contentDescription = "Blurred Silhouette",
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 32.dp),
            contentScale = ContentScale.Crop
        )

        // Radial Gradient overlay for spotlight effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x33D4AF37), Color(0xEE000000), DeepBlack),
                        radius = 1200f
                    )
                )
        )

        // Top Bar
        TopAppBar(
            title = { Text("午夜假面", color = Gold, style = Typography.titleLarge, fontFamily = Typography.bodyLarge.fontFamily) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Gold
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Venetian Mask / Identity
            AsyncImage(
                model = "https://images.unsplash.com/photo-1534062024765-b77ddf40d8aa?auto=format&fit=crop&w=400&q=80",
                contentDescription = "Mask",
                modifier = Modifier
                    .size(160.dp)
                    .padding(16.dp),
                contentScale = ContentScale.Inside,
                alpha = 0.8f
            )

            Text(
                text = "揭开面纱倒计时",
                color = Silver,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "02:14:59",
                color = Gold,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Typography.bodyLarge.fontFamily
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Taste Match Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xBB131313)),
                border = BorderStroke(1.dp, Gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "品味契合",
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "“您最偏爱的单一麦芽威士忌是哪一款，以及为何？”",
                        color = Silver,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input and Submit
            OutlinedTextField(
                value = answerText,
                onValueChange = { answerText = it },
                placeholder = { Text("输入您的独特见解...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color(0xFF4D4635),
                    focusedTextColor = Silver,
                    unfocusedTextColor = Silver,
                    cursorColor = Gold,
                    focusedContainerColor = Color(0x88000000),
                    unfocusedContainerColor = Color(0x88000000)
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Submit Answer */ },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("发送并尝试匹配", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
