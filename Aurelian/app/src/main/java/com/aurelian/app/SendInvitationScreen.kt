package com.aurelian.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendInvitationScreen(onBack: () -> Unit, onSend: () -> Unit) {
    var selectedEventType by remember { mutableStateOf("米其林晚宴") }
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var personalMessage by remember { mutableStateOf("") }

    val eventTypes = listOf("米其林晚宴", "私人画廊", "高尔夫", "品酒会")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        TopAppBar(
            title = { Text("专属邀约", color = Gold, style = Typography.titleLarge, fontFamily = Typography.bodyLarge.fontFamily) },
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

        Column(modifier = Modifier.padding(24.dp)) {
            Text("邀约类型", color = Silver, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Chips for event types
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                eventTypes.take(2).forEach { type ->
                    EventChip(
                        text = type,
                        isSelected = selectedEventType == type,
                        onClick = { selectedEventType = type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                eventTypes.drop(2).forEach { type ->
                    EventChip(
                        text = type,
                        isSelected = selectedEventType == type,
                        onClick = { selectedEventType = type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs
            OutlinedTextField(
                value = eventDate,
                onValueChange = { eventDate = it },
                label = { Text("选择时间 (如: 11月18日, 19:30)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color(0xFF303030),
                    focusedTextColor = Silver,
                    unfocusedTextColor = Silver
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = eventLocation,
                onValueChange = { eventLocation = it },
                label = { Text("赴约地点 (如: 上海宝格丽酒店)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color(0xFF303030),
                    focusedTextColor = Silver,
                    unfocusedTextColor = Silver
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = personalMessage,
                onValueChange = { personalMessage = it },
                label = { Text("致意附言...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color(0xFF303030),
                    focusedTextColor = Silver,
                    unfocusedTextColor = Silver
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSend,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("发送高定请柬", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EventChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Gold else Color.Transparent,
            contentColor = if (isSelected) Black else Silver
        ),
        border = BorderStroke(1.dp, if (isSelected) Gold else Color(0xFF4D4635)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(text)
    }
}
