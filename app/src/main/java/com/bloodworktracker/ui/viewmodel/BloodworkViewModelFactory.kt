package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bloodworktracker.data.repository.BloodworkRepository

class BloodworkViewModelFactory(
    private val repository: BloodworkRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TestListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AddTestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTestViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TestDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestDetailViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(ValueCategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ValueCategoriesViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(ValueDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ValueDetailViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AnalysisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalysisViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel() as T // Note: SettingsViewModel doesn't need repository
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}