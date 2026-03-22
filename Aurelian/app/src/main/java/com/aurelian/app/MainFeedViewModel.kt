package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val users: List<User>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class MainFeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        fetchVideos()
    }

    fun fetchVideos() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            try {
                val response = NetworkClient.apiService.getFeedVideos()
                _uiState.value = FeedUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("MainFeedViewModel", "Error fetching videos", e)
                _uiState.value = FeedUiState.Error(e.localizedMessage ?: "网络错误或服务器未启动")
            }
        }
    }
}
