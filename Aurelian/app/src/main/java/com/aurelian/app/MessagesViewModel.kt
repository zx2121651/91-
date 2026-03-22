package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MessagesUiState {
    object Loading : MessagesUiState()
    data class Success(val conversations: List<Conversation>) : MessagesUiState()
    data class Error(val message: String) : MessagesUiState()
}

class MessagesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        fetchConversations()
    }

    fun fetchConversations() {
        viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading
            try {
                val response = NetworkClient.apiService.getConversations()
                _uiState.value = MessagesUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "Error fetching conversations", e)
                _uiState.value = MessagesUiState.Error(e.localizedMessage ?: "获取私信列表失败")
            }
        }
    }
}
