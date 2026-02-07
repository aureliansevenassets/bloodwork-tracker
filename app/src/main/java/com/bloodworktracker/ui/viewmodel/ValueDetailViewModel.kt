package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodworktracker.data.database.entities.BloodValue
import com.bloodworktracker.data.model.ChartData
import com.bloodworktracker.data.repository.BloodworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ValueDetailUiState(
    val bloodValue: BloodValue? = null,
    val chartData: ChartData? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ValueDetailViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ValueDetailUiState())
    val uiState: StateFlow<ValueDetailUiState> = _uiState.asStateFlow()

    fun loadValue(valueId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val bloodValue = repository.getBloodValueById(valueId)
                val chartData = repository.getChartDataForBloodValue(valueId)

                _uiState.value = _uiState.value.copy(
                    bloodValue = bloodValue,
                    chartData = chartData,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load blood value",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}