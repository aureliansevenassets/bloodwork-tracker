package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodworktracker.data.database.entities.BloodTest
import com.bloodworktracker.data.database.entities.ValueStatus
import com.bloodworktracker.data.model.BloodTestWithResults
import com.bloodworktracker.data.repository.BloodworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TestListItem(
    val id: Long,
    val testDate: java.util.Date,
    val labName: String,
    val numberOfValues: Int,
    val normalCount: Int,
    val abnormalCount: Int,
    val criticalCount: Int
)

data class TestListUiState(
    val tests: List<TestListItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

class TestListViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestListUiState())
    val uiState: StateFlow<TestListUiState> = _uiState.asStateFlow()

    init {
        loadTests()
    }

    private fun loadTests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getAllBloodTestsWithResults().collect { testsWithResults ->
                    val testItems = testsWithResults
                        .sortedByDescending { it.bloodTest.testDate }
                        .map { testWithResults ->
                            val normalCount = testWithResults.results.count { 
                                it.status == ValueStatus.NORMAL 
                            }
                            val abnormalCount = testWithResults.results.count { 
                                it.status == ValueStatus.HIGH || it.status == ValueStatus.LOW 
                            }
                            val criticalCount = testWithResults.results.count { 
                                it.status == ValueStatus.CRITICAL_HIGH || it.status == ValueStatus.CRITICAL_LOW 
                            }
                            
                            TestListItem(
                                id = testWithResults.bloodTest.id,
                                testDate = testWithResults.bloodTest.testDate,
                                labName = testWithResults.bloodTest.labName,
                                numberOfValues = testWithResults.results.size,
                                normalCount = normalCount,
                                abnormalCount = abnormalCount,
                                criticalCount = criticalCount
                            )
                        }
                    
                    _uiState.value = _uiState.value.copy(
                        tests = testItems,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load tests",
                    isLoading = false
                )
            }
        }
    }

    fun deleteTest(testId: Long) {
        viewModelScope.launch {
            try {
                val bloodTest = BloodTest(id = testId, testDate = java.util.Date()) // Only ID is needed for deletion
                repository.deleteBloodTest(bloodTest)
                _uiState.value = _uiState.value.copy(deleteSuccess = true)
                // Tests will be automatically refreshed through the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete test"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }

    fun refreshTests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                repository.getAllBloodTestsWithResults().collect { testsWithResults ->
                    val testItems = testsWithResults
                        .sortedByDescending { it.bloodTest.testDate }
                        .map { testWithResults ->
                            val normalCount = testWithResults.results.count { 
                                it.status == ValueStatus.NORMAL 
                            }
                            val abnormalCount = testWithResults.results.count { 
                                it.status == ValueStatus.HIGH || it.status == ValueStatus.LOW 
                            }
                            val criticalCount = testWithResults.results.count { 
                                it.status == ValueStatus.CRITICAL_HIGH || it.status == ValueStatus.CRITICAL_LOW 
                            }
                            
                            TestListItem(
                                id = testWithResults.bloodTest.id,
                                testDate = testWithResults.bloodTest.testDate,
                                labName = testWithResults.bloodTest.labName,
                                numberOfValues = testWithResults.results.size,
                                normalCount = normalCount,
                                abnormalCount = abnormalCount,
                                criticalCount = criticalCount
                            )
                        }
                    
                    _uiState.value = _uiState.value.copy(
                        tests = testItems,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh tests",
                    isRefreshing = false
                )
            }
        }
    }
}