package com.aurelian.app
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
@Composable
fun InviteCodeScreen(
    pendingToken: String,
    onVerifySuccess: (String) -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(text = "THE VELVET ROPE", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Light, letterSpacing = 4.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Your identity is under review. If you have received an exclusive invitation code from a verified member, present it below.", color = Silver.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase() },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, letterSpacing = 8.sp, fontWeight = FontWeight.Bold, color = Gold),
                placeholder = { Text("______", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 24.sp, letterSpacing = 8.sp, color = Silver.copy(alpha = 0.2f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Gold
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = Gold.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.fillMaxWidth(0.8f).padding(top = 8.dp))
            Spacer(modifier = Modifier.height(32.dp))
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
            }
            Button(onClick = {
                if (inviteCode.isBlank()) return@Button
                isLoading = true; errorMessage = ""
                coroutineScope.launch {
                    try {
                        // Note: Using a custom authorized call here requiring the pending token in the header
                        // Retrofit apiService interface must have an @Header("Authorization") param for this endpoint
                        val response = NetworkClient.apiService.verifyInvite("Bearer " + pendingToken, VerifyInviteRequest(inviteCode))
                        if (response.data.valid) {
                            onVerifySuccess(response.data.newToken ?: pendingToken)
                        } else {
                            errorMessage = "Code rejected."; isLoading = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "Verification failed. The code may be invalid or expired."; isLoading = false
                    }
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.15f), contentColor = Gold), shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(color = Gold, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "SUBMIT", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = { /* Handle waitlist */ }) {
                Text("I don't have a code. Apply for Waitlist.", color = Silver.copy(alpha = 0.5f), fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
            }
        }
    }
}
