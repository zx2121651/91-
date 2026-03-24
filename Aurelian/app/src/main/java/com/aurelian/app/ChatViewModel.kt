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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<Message>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConvId: String = ""

    fun loadMessages(convId: String) {
        currentConvId = convId
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            try {
                val response = NetworkClient.apiService.getMessages(convId, 20)
                _uiState.value = ChatUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching messages", e)
                _uiState.value = ChatUiState.Error(e.localizedMessage ?: "获取聊天记录失败")
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || currentConvId.isBlank()) return
        val currentList = (uiState.value as? ChatUiState.Success)?.messages?.toMutableList() ?: mutableListOf()

        // Optimistic UI update
        val tempMsg = Message(
            id = "temp_${System.currentTimeMillis()}",
            sender = User(id = "me", name = "我", bio = "", location = ""),
            content = content,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
        currentList.add(tempMsg)
        _uiState.value = ChatUiState.Success(currentList.toList())

        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.sendMessage(SendMessageRequest(currentConvId, content))
                // Confirm UI with real ID if necessary, but list is already updated optimistically
                val confirmedMsg = tempMsg.copy(id = response.data.msgId)
                val idx = currentList.indexOf(tempMsg)
                if (idx != -1) {
                    currentList[idx] = confirmedMsg
                    _uiState.value = ChatUiState.Success(currentList.toList())
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                // Remove pessimistic message on failure
                currentList.remove(tempMsg)
                _uiState.value = ChatUiState.Success(currentList.toList())
                // Optional: Emit a snackbar event here
            }
        }
    }

    private val _actionEvent = MutableSharedFlow<String>()
    val actionEvent = _actionEvent.asSharedFlow()

    fun respondToInvitation(inviteId: String, action: String) {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.respondToInvitation(inviteId, RespondInviteRequest(action))
                if (response.data.status == "ACCEPTED") {
                    _actionEvent.emit("您已接受邀约，期待相见")
                } else if (response.data.status == "DECLINED") {
                    _actionEvent.emit("您已婉拒邀约")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error responding to invite", e)
                _actionEvent.emit("操作失败，网络异常")
            }
        }
    }
}
