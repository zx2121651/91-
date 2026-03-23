package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EventDetailsUiState {
    object Loading : EventDetailsUiState()
    data class Success(val eventDetails: EventDetailsData) : EventDetailsUiState()
    data class Error(val message: String) : EventDetailsUiState()
}

class EventDetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EventDetailsUiState>(EventDetailsUiState.Loading)
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    private val _rsvpEvent = MutableSharedFlow<String>()
    val rsvpEvent: SharedFlow<String> = _rsvpEvent.asSharedFlow()

    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            _uiState.value = EventDetailsUiState.Loading
            try {
                val response = NetworkClient.apiService.getEventDetails(eventId)
                _uiState.value = EventDetailsUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("EventDetailsViewModel", "Error fetching event details", e)
                _uiState.value = EventDetailsUiState.Error(e.localizedMessage ?: "获取活动详情失败")
            }
        }
    }

    fun rsvpEvent(eventId: String, partySize: Int = 1) {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.rsvpEvent(eventId, RsvpRequest(partySize))
                if (response.data.status == "REQUESTED") {
                    _rsvpEvent.emit("已提交参与申请，请留意礼宾部消息")
                } else {
                    _rsvpEvent.emit("报名状态异常")
                }
            } catch (e: Exception) {
                Log.e("EventDetailsViewModel", "Error submitting RSVP", e)
                _rsvpEvent.emit("网络异常，报名失败")
            }
        }
    }
}
