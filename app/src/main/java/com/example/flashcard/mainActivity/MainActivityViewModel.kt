package com.example.flashcard.mainActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivityViewModel(private val repository: FlashCardRepository): ViewModel() {

    private var _allCards = MutableStateFlow<UiState<List<ImmutableCard>>>(UiState.Loading)
    val allCards: StateFlow<UiState<List<ImmutableCard>>> = _allCards.asStateFlow()

    private var fetchJob: Job? = null

    fun getAllCards() {
        fetchJob?.cancel()
        _allCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            repository.allCards().collect {
                try {
                    if (it.isEmpty()) {
                        _allCards.value = UiState.Error("Card Update Failed")
                    } else {
                        _allCards.value = UiState.Success(it)
                    }
                } catch (e: IOException) {
                    _allCards.value = UiState.Error(e.toString())
                }

            }
        }
    }

    fun updateCard(card: Card) = viewModelScope.launch {
        repository.updateCard(card)
    }

}

class MainActivityViewModelFactory(private val repository: FlashCardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}