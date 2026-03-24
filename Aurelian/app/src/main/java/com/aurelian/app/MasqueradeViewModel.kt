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

sealed class MasqueradeUiState {
    object Loading : MasqueradeUiState()
    data class Success(val status: MasqueradeStatusResponse) : MasqueradeUiState()
    data class Error(val message: String) : MasqueradeUiState()
}

class MasqueradeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MasqueradeUiState>(MasqueradeUiState.Loading)
    val uiState: StateFlow<MasqueradeUiState> = _uiState.asStateFlow()

    init {
        fetchStatus()
    }

    fun fetchStatus() {
        viewModelScope.launch {
            _uiState.value = MasqueradeUiState.Loading
            try {
                val response = NetworkClient.apiService.getMasqueradeStatus()
                _uiState.value = MasqueradeUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("MasqueradeViewModel", "Error fetching status", e)
                _uiState.value = MasqueradeUiState.Error(e.localizedMessage ?: "获取盲盒状态失败")
            }
        }
    }

    private val _submitEvent = MutableSharedFlow<String>()
    val submitEvent: SharedFlow<String> = _submitEvent.asSharedFlow()

    fun submitAnswer(answer: String) {
        if (answer.isBlank()) {
            viewModelScope.launch { _submitEvent.emit("答案不能为空") }
            return
        }
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.submitMasqueradeAnswer(SubmitAnswerRequest(answer))
                if (response.data.status == "MATCHING_IN_PROGRESS") {
                    _submitEvent.emit("答案已提交，午夜系统正在为您匹配灵魂伴侣...")
                } else {
                    _submitEvent.emit("状态异常")
                }
            } catch (e: Exception) {
                Log.e("MasqueradeViewModel", "Error submitting answer", e)
                _submitEvent.emit("网络异常，提交失败")
            }
        }
    }
}
