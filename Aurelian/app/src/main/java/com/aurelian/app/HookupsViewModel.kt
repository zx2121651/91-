package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HookupsUiState {
    object Loading : HookupsUiState()
    data class Success(
        val cards: List<HookupCard>,
        val selectedCity: String?,
        val selectedIntent: String?,
        val nextCursor: String?,
        val requestStates: Map<String, String>
    ) : HookupsUiState()

    data class Error(val message: String) : HookupsUiState()
}

class HookupsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HookupsUiState>(HookupsUiState.Loading)
    val uiState: StateFlow<HookupsUiState> = _uiState.asStateFlow()

    private var selectedCity: String? = null
    private var selectedIntent: String? = null
    private var nextCursor: String? = null
    private val requestStates = mutableMapOf<String, String>()

    init {
        fetchCards(reset = true)
    }

    fun applyFilters(city: String?, intent: String?) {
        selectedCity = city
        selectedIntent = intent
        fetchCards(reset = true)
    }

    fun loadMore() {
        if (nextCursor == null) return
        fetchCards(reset = false)
    }

    fun fetchCards(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                _uiState.value = HookupsUiState.Loading
                nextCursor = null
            }
            try {
                val response = NetworkClient.apiService.getHookupCards(
                    city = selectedCity,
                    intent = selectedIntent,
                    limit = 10,
                    cursor = if (reset) null else nextCursor
                )

                val oldCards = if (!reset && _uiState.value is HookupsUiState.Success) {
                    (_uiState.value as HookupsUiState.Success).cards
                } else {
                    emptyList()
                }

                nextCursor = response.nextCursor
                _uiState.value = HookupsUiState.Success(
                    cards = oldCards + response.data,
                    selectedCity = selectedCity,
                    selectedIntent = selectedIntent,
                    nextCursor = nextCursor,
                    requestStates = requestStates.toMap()
                )
            } catch (e: Exception) {
                Log.e("HookupsViewModel", "Error fetching hookup cards", e)
                _uiState.value = HookupsUiState.Error(e.localizedMessage ?: "加载约会卡片失败")
            }
        }
    }

    fun sendRequest(targetUserId: String, note: String = "") {
        viewModelScope.launch {
            try {
                requestStates[targetUserId] = "SENDING"
                emitSuccessWithCurrentCards()

                val response = NetworkClient.apiService.sendHookupRequest(
                    HookupRequest(targetUserId = targetUserId, note = note, safeMode = true, meetingType = "DRINK")
                )

                requestStates[targetUserId] = response.data.status
                emitSuccessWithCurrentCards()

                pollRequestStatus(targetUserId, response.data.requestId)
            } catch (e: Exception) {
                Log.e("HookupsViewModel", "Error sending hookup request", e)
                requestStates[targetUserId] = "FAILED"
                emitSuccessWithCurrentCards()
            }
        }
    }

    private suspend fun pollRequestStatus(targetUserId: String, requestId: String) {
        repeat(3) {
            delay(2500)
            try {
                val status = NetworkClient.apiService.getHookupRequestStatus(requestId).data.status
                requestStates[targetUserId] = status
                emitSuccessWithCurrentCards()
                if (status == "RESPONDED") return
            } catch (e: Exception) {
                Log.e("HookupsViewModel", "Error polling request status", e)
            }
        }
    }

    private fun emitSuccessWithCurrentCards() {
        val current = _uiState.value
        if (current is HookupsUiState.Success) {
            _uiState.value = current.copy(requestStates = requestStates.toMap())
        }
    }
}
