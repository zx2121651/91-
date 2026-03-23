package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: ProfileData) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val response = NetworkClient.apiService.getProfile()
                _uiState.value = ProfileUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching profile", e)
                _uiState.value = ProfileUiState.Error(e.localizedMessage ?: "获取个人主页失败")
            }
        }
    }

    private val _matchEvent = MutableSharedFlow<ProfileData>()
    val matchEvent: SharedFlow<ProfileData> = _matchEvent.asSharedFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    fun likeUser(profile: ProfileData) {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.likeUser(LikeRequest(profile.id))
                if (response.data.matched) {
                    _matchEvent.emit(profile)
                } else {
                    _messageEvent.emit("心动已发送，等待对方回应")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error liking user", e)
                _messageEvent.emit("网络错误，发送失败")
            }
        }
    }

    fun passUser(profile: ProfileData) {
        viewModelScope.launch {
            try {
                NetworkClient.apiService.passUser(PassRequest(profile.id))
                _messageEvent.emit("已对 ${profile.name} 无感")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error passing user", e)
                _messageEvent.emit("网络错误，操作失败")
            }
        }
    }
}
