package com.example.flashcard.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.entities.Card
import com.example.flashcard.entities.Deck
import com.example.flashcard.entities.relations.DeckWithCards
import kotlinx.coroutines.launch

class CardViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _deckWithAllCards: LiveData<List<DeckWithCards>>? = null
    val deckWithAllCards: LiveData<List<DeckWithCards>>
        get() = _deckWithAllCards !!
    fun getDeckWithCards(deckId: Int) {
        _deckWithAllCards= repository.getDeckWithCards(deckId).asLiveData()
    }

    fun insertCard(card: Card) = viewModelScope.launch {
        repository.insertCard(card)
    }

    fun updateCard(card: Card) = viewModelScope.launch {
        repository.updateCard(card)
    }

    fun updateDeck(deck: Deck) = viewModelScope.launch {
        repository.updateDeck(deck)
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