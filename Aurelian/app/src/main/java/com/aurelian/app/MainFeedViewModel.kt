package com.aurelian.app

import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import android.net.Uri

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val users: List<User>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class MainFeedViewModel : ViewModel() {

    // Simple manual injection of our new Repository
    private val repository = FeedRepository()

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        fetchVideos()
    }

    fun fetchVideos() {
        viewModelScope.launch {
            if (_uiState.value !is FeedUiState.Success) {
                _uiState.value = FeedUiState.Loading
            }

            repository.getFeedVideos().collectLatest { result ->
                result.onSuccess { users ->
                    _uiState.value = FeedUiState.Success(users)
                    preloadVideo(users.firstOrNull()?.videoUrl)
                }.onFailure { exception ->
                    Log.e("MainFeedViewModel", "Failed to fetch videos from repository", exception)
                    if (_uiState.value !is FeedUiState.Success) {
                        _uiState.value = FeedUiState.Error(exception.localizedMessage ?: "网络错误或服务器未响应")
                    }
                }
            }
        }
    }
    private val _matchEvent = MutableSharedFlow<User>()
    val matchEvent: SharedFlow<User> = _matchEvent.asSharedFlow()


    @androidx.media3.common.util.UnstableApi
    fun preloadVideo(url: String?) {
        if (url == null) return
        viewModelScope.launch {
            try {
                // Delegate to our new dedicated VideoPrefetcher which efficiently fetches only 2MB
                VideoPrefetcher.prefetch(url)
            } catch (e: Exception) {
                Log.e("MainFeedViewModel", "Error preloading $url", e)
            }
        }
    }

    // Optional: Expose cancel prefetch if we decide to wire it to UI events
    @androidx.media3.common.util.UnstableApi
    fun cancelPreload(url: String?) {
        VideoPrefetcher.cancelPrefetch(url)
    }

    fun likeUser(user: User) {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.likeUser(LikeRequest(user.id))
                if (response.data.matched) {
                    _matchEvent.emit(user)
                }
            } catch (e: Exception) {
                Log.e("MainFeedViewModel", "Error liking user", e)
                // Optionally handle error
            }
        }
    }
}
