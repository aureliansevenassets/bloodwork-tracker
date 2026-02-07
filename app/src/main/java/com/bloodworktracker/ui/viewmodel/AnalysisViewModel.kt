package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodworktracker.data.model.BloodTestWithResults
import com.bloodworktracker.data.model.ConstellationAnalysis
import com.bloodworktracker.data.repository.BloodworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalysisUiState(
    val test: BloodTestWithResults? = null,
    val analyses: List<ConstellationAnalysis> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isAnalyzing: Boolean = false
)

class AnalysisViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    fun loadTestAndAnalyze(testId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val test = repository.getBloodTestWithResults(testId)
                _uiState.value = _uiState.value.copy(
                    test = test,
                    isLoading = false
                )
                
                // Start analysis
                analyzeTest(testId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load test",
                    isLoading = false
                )
            }
        }
    }

    fun analyzeTest(testId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true)
            try {
                val analyses = repository.analyzeConstellation(testId)
                _uiState.value = _uiState.value.copy(
                    analyses = analyses,
                    isAnalyzing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to analyze test",
                    isAnalyzing = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}