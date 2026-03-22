package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
}
