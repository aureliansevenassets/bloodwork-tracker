package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodworktracker.data.repository.BloodworkRepository
import com.bloodworktracker.data.database.entities.BloodTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    val recentTests = repository.getAllBloodTests().map { tests ->
        tests.take(3) // Show only 3 most recent tests
    }
    
    val totalTestsCount = repository.getAllBloodTests().map { tests ->
        tests.size
    }
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Dashboard data is loaded via flows
                _uiState.value = _uiState.value.copy(
                    isLoaded = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unbekannter Fehler"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class DashboardUiState(
    val isLoaded: Boolean = false,
    val error: String? = null
)