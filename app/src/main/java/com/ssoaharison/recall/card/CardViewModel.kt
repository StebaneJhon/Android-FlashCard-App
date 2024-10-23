package com.ssoaharison.recall.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.Model.ImmutableCard
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.backend.Model.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.Model.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.Model.toLocal
import com.ssoaharison.recall.util.SpaceRepetitionAlgorithmHelper
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

    fun deleteCard(card: ImmutableCard?, localDeck: ImmutableDeck) = viewModelScope.launch {
        val externalDeck = localDeck.toLocal()
        repository.deleteCard(card, externalDeck)
    }

    suspend fun searchCard(searchQuery: String): LiveData<List<ImmutableCard?>> {
        return repository.searchCard(searchQuery).asLiveData()
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