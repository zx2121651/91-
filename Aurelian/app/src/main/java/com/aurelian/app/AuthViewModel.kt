package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val token: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, inviteCode: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("邮箱/会员号不能为空")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // 1. Authenticate with backend
                val loginResponse = NetworkClient.apiService.login(LoginRequest(email, "123456")) // "123456" is mock code based on API doc
                val loginData = loginResponse.data

                // 2. Check if new user and needs invite code verification
                if (loginData.isNewUser) {
                    if (inviteCode.isBlank()) {
                        _uiState.value = AuthUiState.Error("新会员必须输入高定邀请码")
                        return@launch
                    }
                    val verifyResponse = NetworkClient.apiService.verifyInvite(VerifyInviteRequest(inviteCode))
                    if (!verifyResponse.data.valid) {
                        _uiState.value = AuthUiState.Error("无效的邀请码")
                        return@launch
                    }
                }

                // 3. Success, return token
                _uiState.value = AuthUiState.Success(loginData.token)

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Authentication error", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "认证失败，请检查网络")
            }
        }
    }

    fun biometricLogin() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Mock biometric payload
                NetworkClient.apiService.biometricAuth(BiometricRequest("deviceId_123", "signature_abc"))
                _uiState.value = AuthUiState.Success("mock_token_from_biometric")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Biometric auth error", e)
                _uiState.value = AuthUiState.Error("生物识别验证失败")
            }
        }
    }
}
