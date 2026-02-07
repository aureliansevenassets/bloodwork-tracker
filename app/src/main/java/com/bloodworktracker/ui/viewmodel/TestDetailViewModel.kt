package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodworktracker.data.database.entities.BloodTestResult
import com.bloodworktracker.data.model.BloodTestResultWithValue
import com.bloodworktracker.data.model.BloodTestWithResults
import com.bloodworktracker.data.repository.BloodworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TestDetailUiState(
    val test: BloodTestWithResults? = null,
    val groupedResults: Map<String, List<BloodTestResultWithValue>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleting: Boolean = false
)

class TestDetailViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestDetailUiState())
    val uiState: StateFlow<TestDetailUiState> = _uiState.asStateFlow()

    fun loadTest(testId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val test = repository.getBloodTestWithResults(testId)
                val results = repository.getResultsWithValuesByTestId(testId)
                val groupedResults = results.groupBy { it.category }

                _uiState.value = _uiState.value.copy(
                    test = test,
                    groupedResults = groupedResults,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load test",
                    isLoading = false
                )
            }
        }
    }

    fun updateResult(result: BloodTestResultWithValue, newValue: Double) {
        viewModelScope.launch {
            try {
                val currentTest = _uiState.value.test ?: return@launch
                
                // Calculate new status
                val bloodValue = repository.getBloodValueById(result.bloodValueId)
                val newStatus = bloodValue?.let { 
                    repository.calculateValueStatus(newValue, it) 
                } ?: result.status
                
                val updatedResult = BloodTestResult(
                    id = result.id,
                    testId = result.testId,
                    bloodValueId = result.bloodValueId,
                    value = newValue,
                    status = newStatus,
                    notes = result.notes
                )
                
                repository.updateResult(updatedResult)
                
                // Reload test to get updated data
                loadTest(currentTest.bloodTest.id)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update result"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}