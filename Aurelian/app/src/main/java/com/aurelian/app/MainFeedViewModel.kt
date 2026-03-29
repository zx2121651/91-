package com.aurelian.app

import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        fetchVideos()
    }

    fun fetchVideos() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            try {
                // Forcing the use of extreme mock data for UI boundary testing
                // Injecting real H.264/HEVC mock video streams for testing cache and rendering
                val mockData = listOf(
                    User(
                        id = "mock_user_1",
                        name = "Alexandre R.",
                        location = "Monaco Yacht Club",
                        bio = "Enjoying the summer breeze. #Monaco",
                        videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                    ),
                    User(
                        id = "mock_user_2",
                        name = "Eleanor V.",
                        location = "Paris, France",
                        bio = "Night stroll around the Louvre.",
                        videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                    ),
                    User(
                        id = "mock_user_3",
                        name = "Sebastian K.",
                        location = "Geneva, Switzerland",
                        bio = "Testing the limits of time.",
                        videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
                    )
                )

                // Pre-cache the first video immediately
                preloadVideo(mockData.firstOrNull()?.videoUrl)
                _uiState.value = FeedUiState.Success(mockData)
                // val response = NetworkClient.apiService.getFeedVideos()
                // _uiState.value = FeedUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("MainFeedViewModel", "Error fetching videos", e)
                _uiState.value = FeedUiState.Error(e.localizedMessage ?: "网络错误或服务器未启动")
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
