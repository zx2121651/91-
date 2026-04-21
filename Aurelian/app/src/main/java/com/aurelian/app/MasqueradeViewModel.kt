package com.aurelian.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MasqueradeUiState {
    object Loading : MasqueradeUiState()
    data class Error(val message: String) : MasqueradeUiState()

    // Status Open: 允许提交答卷
    data class Open(val question: String, val endTime: Long) : MasqueradeUiState()

    // Status Closed/Submitted: 舞会未开始或已经提交了答卷
    data class ClosedOrSubmitted(val question: String) : MasqueradeUiState()
}

class MasqueradeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MasqueradeUiState>(MasqueradeUiState.Loading)
    val uiState: StateFlow<MasqueradeUiState> = _uiState

    // 提交动作的状态
    private val _submitState = MutableStateFlow<Boolean>(false)
    val submitState: StateFlow<Boolean> = _submitState

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.value = MasqueradeUiState.Loading
            try {
                val response = NetworkClient.apiService.getMasqueradeStatus()
                val data = response.data
                if (data.isOpen) {
                    _uiState.value = MasqueradeUiState.Open(data.question, data.endTime)
                } else {
                    _uiState.value = MasqueradeUiState.ClosedOrSubmitted(data.question)
                }
            } catch (e: Exception) {
                _uiState.value = MasqueradeUiState.Error("无法探寻午夜秘密: \${e.message}")
            }
        }
    }

    fun submitAnswer(answer: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _submitState.value = true
            try {
                val response = NetworkClient.apiService.submitMasqueradeAnswer(SubmitAnswerRequest(answer))
                if (response.data.status == "SUBMITTED") {
                    // 状态流转到已提交
                    val currentState = _uiState.value
                    if (currentState is MasqueradeUiState.Open) {
                        _uiState.value = MasqueradeUiState.ClosedOrSubmitted("您的回声已落入舞池，管家正在寻觅有缘人。")
                    }
                    onSuccess()
                } else {
                    onError("提交失败，未知状态。")
                }
            } catch (e: Exception) {
                onError("递交失败，面具滑落: \${e.message}")
            } finally {
                _submitState.value = false
            }
        }
    }
}
