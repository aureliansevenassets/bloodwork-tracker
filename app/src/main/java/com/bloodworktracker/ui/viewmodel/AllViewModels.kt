package com.bloodworktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bloodworktracker.data.repository.BloodworkRepository

// Placeholder ViewModels for the basic navigation to work
// These will be expanded as features are implemented

class TestListViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {
    // TODO: Implement test list functionality
}

class AddTestViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {
    // TODO: Implement add test functionality
}

class TestDetailViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {
    // TODO: Implement test detail functionality
}

class ValueCategoriesViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {
    // TODO: Implement value categories functionality
}

class ValueDetailViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {
    // TODO: Implement value detail functionality
}

class AnalysisViewModel(
    private val repository: BloodworkRepository
) : ViewModel() {
    // TODO: Implement analysis functionality
}