package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodworktracker.data.database.entities.BloodValue
import com.bloodworktracker.data.model.BloodValueCategory
import com.bloodworktracker.data.repository.BloodworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ValueCategoriesUiState(
    val categories: List<BloodValueCategory> = emptyList(),
    val allValues: List<BloodValue> = emptyList(),
    val filteredValues: List<BloodValue> = emptyList(),
    val searchQuery: String = "",
    val expandedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ValueCategoriesViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ValueCategoriesUiState())
    val uiState: StateFlow<ValueCategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getBloodValuesByCategory().collect { categories ->
                    val allValues = categories.flatMap { it.values }
                    
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        allValues = allValues,
                        filteredValues = if (_uiState.value.searchQuery.isBlank()) emptyList() else filterValues(_uiState.value.searchQuery, allValues),
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

    fun updateSearchQuery(query: String) {
        val filteredValues = if (query.isBlank()) {
            emptyList()
        } else {
            filterValues(query, _uiState.value.allValues)
        }
        
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredValues = filteredValues
        )
    }

    private fun filterValues(query: String, values: List<BloodValue>): List<BloodValue> {
        val searchTerm = query.lowercase()
        return values.filter { value ->
            value.nameDe.lowercase().contains(searchTerm) ||
            value.nameEn.lowercase().contains(searchTerm) ||
            value.abbreviation.lowercase().contains(searchTerm) ||
            value.category.lowercase().contains(searchTerm)
        }
    }

    fun toggleCategoryExpansion(categoryName: String) {
        val currentExpanded = _uiState.value.expandedCategories
        val newExpanded = if (currentExpanded.contains(categoryName)) {
            currentExpanded - categoryName
        } else {
            currentExpanded + categoryName
        }
        
        _uiState.value = _uiState.value.copy(expandedCategories = newExpanded)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}