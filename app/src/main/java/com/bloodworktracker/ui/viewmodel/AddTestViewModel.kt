package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodworktracker.data.database.entities.BloodTest
import com.bloodworktracker.data.database.entities.BloodTestResult
import com.bloodworktracker.data.model.BloodValueCategory
import com.bloodworktracker.data.model.Gender
import com.bloodworktracker.data.repository.BloodworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class AddTestUiState(
    val testDate: Date = Date(),
    val labName: String = "",
    val notes: String = "",
    val bloodValueCategories: List<BloodValueCategory> = emptyList(),
    val selectedValues: Map<Long, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val testSaved: Boolean = false
)

class AddTestViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTestUiState())
    val uiState: StateFlow<AddTestUiState> = _uiState.asStateFlow()

    init {
        loadBloodValueCategories()
    }

    private fun loadBloodValueCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getBloodValuesByCategory().collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        bloodValueCategories = categories,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load blood values",
                    isLoading = false
                )
            }
        }
    }

    fun updateTestDate(date: Date) {
        _uiState.value = _uiState.value.copy(testDate = date)
    }

    fun updateLabName(labName: String) {
        _uiState.value = _uiState.value.copy(labName = labName)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateBloodValue(valueId: Long, value: Double?) {
        val currentValues = _uiState.value.selectedValues.toMutableMap()
        if (value != null) {
            currentValues[valueId] = value
        } else {
            currentValues.remove(valueId)
        }
        _uiState.value = _uiState.value.copy(selectedValues = currentValues)
    }

    fun saveTest() {
        if (_uiState.value.selectedValues.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Bitte wählen Sie mindestens einen Blutwert aus")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, testSaved = false)
            try {
                val currentState = _uiState.value
                
                // Create blood test
                val bloodTest = BloodTest(
                    testDate = currentState.testDate,
                    labName = currentState.labName,
                    notes = currentState.notes
                )
                
                val testId = repository.insertBloodTest(bloodTest)
                
                // Create blood test results
                val results = currentState.selectedValues.map { (bloodValueId, value) ->
                    val bloodValue = repository.getBloodValueById(bloodValueId)
                    val status = bloodValue?.let { 
                        repository.calculateValueStatus(value, it, Gender.MALE) // Default to male for now
                    } ?: com.bloodworktracker.data.database.entities.ValueStatus.NORMAL
                    
                    BloodTestResult(
                        testId = testId,
                        bloodValueId = bloodValueId,
                        value = value,
                        status = status
                    )
                }
                
                repository.insertResults(results)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false, 
                    testSaved = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to save test",
                    isSaving = false,
                    testSaved = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}