package com.example.flashcard.deck

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException

class DeckViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _allDecks = MutableStateFlow<UiState<List<ImmutableDeck>>>(UiState.Loading)
    val allDecks: StateFlow<UiState<List<ImmutableDeck>>> = _allDecks.asStateFlow()

    private var fetchJob: Job? = null
    private var fetchGetJob: Job? = null
    private var fetchDeckDeletionJob: Job? = null

    fun getAllDecks() {
        fetchJob?.cancel()
        _allDecks.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.allDecks().collect {
                    if (it.isEmpty()) {
                        _allDecks.value = UiState.Error("No Deck")
                    } else {
                        _allDecks.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _allDecks.value = UiState.Error(e.toString())
            }
        }
    }

    fun searchDeck(searchQuery: String): LiveData<List<ImmutableDeck>> {
        return repository.searchDeck(searchQuery).asLiveData()
    }

    fun insertDeck(deck: Deck) = viewModelScope.launch {
        repository.insertDeck(deck)
    }

    fun deleteDeck(deck: ImmutableDeck) {
        fetchDeckDeletionJob?.cancel()
        fetchDeckDeletionJob = viewModelScope.launch {
            repository.deleteCards(deck)
            repository.deleteDeck(deck)
        }
    }

    fun updateDeck(deck: Deck) = viewModelScope.launch {
        repository.updateDeck(deck)
    }

    private var _deckWithAllCards = MutableStateFlow<UiState<ImmutableDeckWithCards>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<ImmutableDeckWithCards>> = _deckWithAllCards.asStateFlow()
    fun getDeckWithCards(deckId: Int) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getImmutableDeckWithCards(deckId).collect {
                    _deckWithAllCards.value = UiState.Success(it)
                }
            } catch (e: IOException) {
                _deckWithAllCards.value = UiState.Error(e.toString())
            }
        }
    }

}

class DeckViewModelFactory(private val repository: FlashCardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeckViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}