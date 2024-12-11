package com.ssoaharison.recall.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class CardViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _deckWithAllCards = MutableStateFlow<UiState<ImmutableDeckWithCards>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<ImmutableDeckWithCards>> = _deckWithAllCards.asStateFlow()
    private var fetchJob: Job? = null
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()

    fun getDeckWithCards(deckId: String) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getImmutableDeckWithCards(deckId).collect {
                    if (it.cards?.isEmpty()!!) {
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

    fun getBoxLevels(): List<ImmutableSpaceRepetitionBox>? {
       return spaceRepetitionHelper.getActualBoxLevels()
    }

    fun insertCards(cards: List<ImmutableCard>, externalDeck: ImmutableDeck) = viewModelScope.launch {
        val cardsToAdd = cards.reversed()
        repository.insertCards(cardsToAdd, externalDeck)
    }

    fun updateCard(card: ImmutableCard) = viewModelScope.launch {
        repository.updateCard(card)
    }

    fun deleteCard(card: ImmutableCard?) = viewModelScope.launch {
        repository.deleteCard(card)
    }

    fun searchCard(searchQuery: String, deckId: String): LiveData<Set<ImmutableCard?>> {
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