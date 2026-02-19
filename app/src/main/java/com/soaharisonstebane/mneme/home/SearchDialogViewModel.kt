package com.soaharisonstebane.mneme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soaharisonstebane.mneme.backend.FlashCardRepository

class SearchDialogViewModel(
    private val repository: FlashCardRepository
): ViewModel() {

}

class SearchDialogViewModelFactory(
    private val repository: FlashCardRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchDialogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchDialogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}