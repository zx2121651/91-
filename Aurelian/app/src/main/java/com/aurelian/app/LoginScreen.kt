package com.aurelian.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> {
                val msg = (uiState as AuthUiState.Error).message
                snackbarHostState.showSnackbar(msg)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AURELIAN",
                color = Gold,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Typography.bodyLarge.fontFamily,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "THE NIGHT IS YOURS",
                color = Silver,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("电子邮箱 / 会员号", color = Color.Gray) },
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
                value = password,
                onValueChange = { password = it },
                label = { Text("密码", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
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
                value = inviteCode,
                onValueChange = { inviteCode = it },
                label = { Text("高定邀请码 (新会员)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color(0xFF303030),
                    focusedTextColor = Silver,
                    unfocusedTextColor = Silver
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.login(email, password, inviteCode) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = uiState != AuthUiState.Loading
            ) {
                if (uiState == AuthUiState.Loading) {
                    CircularProgressIndicator(color = Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("验证身份", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable(enabled = uiState != AuthUiState.Loading) { viewModel.biometricLogin() }
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Biometric", tint = Gold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("使用 Face ID / 指纹登录", color = Gold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "ESTABLISHED MMXXIV",
                color = Color.DarkGray,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )
        }
    }
}
