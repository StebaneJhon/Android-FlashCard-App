package com.example.flashcard.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toLocal
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class CardViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _deckWithAllCards = MutableStateFlow<UiState<DeckWithCards>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<DeckWithCards>> = _deckWithAllCards.asStateFlow()
    private var fetchJob: Job? = null

    fun getDeckWithCards(deckId: Int) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getDeckWithCards(deckId).collect {
                    if (it.cards.isEmpty()) {
                        _deckWithAllCards.value = UiState.Error("Empty deck")
                    } else {
                        _deckWithAllCards.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _deckWithAllCards.value = UiState.Error(e.toString())
            }
        }
    }

    fun insertCard(card: Card, localDeck: ImmutableDeck) = viewModelScope.launch {
        val externalDeck = localDeck.toLocal()
        repository.insertCard(card, externalDeck)
    }

    fun updateCard(card: Card) = viewModelScope.launch {
        repository.updateCard(card)
    }

    fun deleteCard(card: Card, localDeck: ImmutableDeck) = viewModelScope.launch {
        val externalDeck = localDeck.toLocal()
        repository.deleteCard(card, externalDeck)
    }

    fun searchCard(searchQuery: String, deckId: Int): LiveData<List<ImmutableCard>> {
        return repository.searchCard(searchQuery, deckId).asLiveData()
    }

}

class CardViewModelFactory(private val repository: FlashCardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}