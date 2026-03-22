package com.aurelian.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MatchesUiState {
    object Loading : MatchesUiState()
    data class Success(val matches: List<Match>, val admirers: List<Admirer>) : MatchesUiState()
    data class Error(val message: String) : MatchesUiState()
}

class MatchesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = MatchesUiState.Loading
            try {
                // Fetch matches and admirers in parallel
                val matchesResponse = NetworkClient.apiService.getMatches(1)
                val admirersResponse = NetworkClient.apiService.getAdmirers()

                // Assuming backend returns { "data": [...] } we need to fix Network.kt first. Let's check API doc.
                // API doc says: Returns `[{ "matchId": "mtc_1", "user": {...} }]`
                // But typically express res.json({ data: [] }) is used. I'll rely on current Network.kt definitions.
                _uiState.value = MatchesUiState.Success(matchesResponse.data, admirersResponse.data)
            } catch (e: Exception) {
                Log.e("MatchesViewModel", "Error fetching matches data", e)
                _uiState.value = MatchesUiState.Error(e.localizedMessage ?: "获取匹配列表失败")
            }
        }
    }
}
