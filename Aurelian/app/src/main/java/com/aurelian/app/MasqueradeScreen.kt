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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasqueradeScreen(
    onBack: () -> Unit,
    viewModel: MasqueradeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var answerText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        when (val state = uiState) {
            is MasqueradeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            }
            is MasqueradeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "加载失败", color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message, color = Silver)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchStatus() }) {
                            Text("重试")
                        }
                    }
                }
            }
            is MasqueradeUiState.Success -> {
                val status = state.status
                // Blurred Background / Silhouette
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1542282088-72c9c27ed0cd?auto=format&fit=crop&w=800&q=80",
                    contentDescription = "Masquerade Silhouette",
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(16.dp),
                    contentScale = ContentScale.Crop
                )

                // Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x99000000), DeepBlack),
                                startY = 300f
                            )
                        )
                )

                // Close Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .statusBarsPadding()
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Silver)
                }

                // Content
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "午夜盲盒",
                        color = Gold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The Midnight Masquerade",
                        color = Silver,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))

                    if (!status.isOpen) {
                        Text(
                            text = "未在开放时间\n(敬请期待...)",
                            color = Silver,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                    } else {
                        // The Question
                        Text(
                            text = "本期灵魂拷问：\n" + status.question,
                            color = Silver,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Answer Input
                        OutlinedTextField(
                            value = answerText,
                            onValueChange = { answerText = it },
                            placeholder = { Text("输入您的答案以开启匹配...", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = Color(0x33000000),
                                unfocusedBorderColor = Gold,
                                focusedBorderColor = Silver,
                                focusedTextColor = Silver,
                                unfocusedTextColor = Silver
                            ),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // Submit Button
                        Button(
                            onClick = { /* TODO: submit answer */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black)
                        ) {
                            Text("开启匹配", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "回答契合度高的灵魂，将在午夜相遇。",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
