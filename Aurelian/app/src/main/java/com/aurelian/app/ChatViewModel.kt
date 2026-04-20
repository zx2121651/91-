package com.aurelian.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<Message>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private var currentConvId: String? = null

    // Load initial messages
    fun loadMessages(convId: String) {
        currentConvId = convId
        viewModelScope.launch {
            try {
                _uiState.value = ChatUiState.Loading
                val response = NetworkClient.apiService.getMessages(convId, limit = 50)
                _uiState.value = ChatUiState.Success(response.data)
                startBurnEngine()
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error("信使遇到阻碍：\${e.message}")
            }
        }
    }

    // Send a message
    fun sendMessage(convId: String, text: String, isEphemeral: Boolean) {
        viewModelScope.launch {
            try {
                // Optimistic UI update could go here, but for simplicity we rely on refresh
                NetworkClient.apiService.sendMessage(
                    SendMessageRequest(convId, text, isEphemeral = isEphemeral)
                )
                // Refresh
                loadMessages(convId)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // Trigger read and burn countdown
    fun triggerRead(msgId: String) {
        val state = _uiState.value
        if (state is ChatUiState.Success) {
            viewModelScope.launch {
                try {
                    val res = NetworkClient.apiService.markMessageAsRead(msgId)
                    if (res.success) {
                        // Update local state with new expiresAt
                        val updatedList = state.messages.map {
                            if (it.msgId == msgId) {
                                it.copy(readAt = res.data.readAt, expiresAt = res.data.expiresAt, status = "READ")
                            } else {
                                it
                            }
                        }
                        _uiState.value = ChatUiState.Success(updatedList)
                    }
                } catch (e: Exception) {
                    // silently fail
                }
            }
        }
    }

    // Local loop to burn expired messages
    private fun startBurnEngine() {
        viewModelScope.launch {
            while (true) {
                delay(100) // Execute every 100ms
                val state = _uiState.value
                if (state is ChatUiState.Success) {
                    val now = System.currentTimeMillis()
                    var hasChanged = false

                    val newList = state.messages.mapNotNull { msg ->
                        if (msg.expiresAt != null) {
                            try {
                                val expireTime = sdf.parse(msg.expiresAt)?.time ?: Long.MAX_VALUE
                                if (now >= expireTime) {
                                    hasChanged = true
                                    null // 彻底烧除移除出列表
                                } else {
                                    msg
                                }
                            } catch (e: Exception) {
                                msg
                            }
                        } else {
                            msg
                        }
                    }

                    if (hasChanged) {
                        _uiState.value = ChatUiState.Success(newList)
                    }
                }
            }
        }
    }

    fun getRemainingTime(expiresAtStr: String?): Float {
        if (expiresAtStr == null) return 0f
        return try {
            val expireTime = sdf.parse(expiresAtStr)?.time ?: 0L
            val now = System.currentTimeMillis()
            val diff = (expireTime - now) / 1000f
            if (diff < 0) 0f else diff
        } catch (e: Exception) {
            0f
        }
    }
}
