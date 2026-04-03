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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun LoginScreen(onLoginSuccess: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isLogoVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); isLogoVisible = true }
    Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            AnimatedVisibility(visible = isLogoVisible, enter = fadeIn(animationSpec = tween(1500)), exit = fadeOut()) {
                Text(text = "AURELIAN NIGHT", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Light, letterSpacing = 6.sp, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(80.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email or Concierge ID", color = Silver.copy(alpha = 0.5f)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Silver.copy(alpha = 0.2f), focusedTextColor = Silver, unfocusedTextColor = Silver, cursorColor = Gold, focusedLabelColor = Gold), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
            }
            Button(onClick = {
                if (email.isBlank()) { errorMessage = "Identity required."; return@Button }
                isLoading = true; errorMessage = ""
                coroutineScope.launch {
                    try {
                        val response = NetworkClient.apiService.login(LoginRequest(email, "DUMMY_CODE"))
                        val token = response.data.token
                        val status = if (response.data.isNewUser) "PENDING" else "ACTIVE"
                        onLoginSuccess(token, status)
                    } catch (e: Exception) {
                        errorMessage = "The sanctuary is currently unreachable."; isLoading = false
                    }
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.15f), contentColor = Gold), shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(color = Gold, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "REQUEST ENTRY", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "By requesting entry, you agree to our strict code of conduct. Membership is highly curated.", color = Silver.copy(alpha = 0.4f), fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 16.sp, modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}
