package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReferralUiState {
    object Loading : ReferralUiState()
    data class Success(val status: ReferralsStatusResponse) : ReferralUiState()
    data class Error(val message: String) : ReferralUiState()
}

class ReferralViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ReferralUiState>(ReferralUiState.Loading)
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    init {
        fetchStatus()
    }

    fun fetchStatus() {
        viewModelScope.launch {
            _uiState.value = ReferralUiState.Loading
            try {
                val response = NetworkClient.apiService.getReferralsStatus()
                _uiState.value = ReferralUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("ReferralViewModel", "Error fetching referral status", e)
                _uiState.value = ReferralUiState.Error(e.localizedMessage ?: "获取内推信息失败")
            }
        }
    }
}
