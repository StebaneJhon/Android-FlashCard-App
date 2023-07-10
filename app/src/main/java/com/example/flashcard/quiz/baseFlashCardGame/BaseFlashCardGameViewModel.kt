package com.example.flashcard.quiz.baseFlashCardGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class BaseFlashCardGameViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var fetchJob: Job? = null

    private var cardPosition: Int = 0
    private var _deckWithAllCards = MutableStateFlow<UiState<List<DeckWithCards>>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<List<DeckWithCards>>> = _deckWithAllCards.asStateFlow()

    fun getDeckWithCards(deckId: Int) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getDeckWithCards(deckId).collect {
                    _deckWithAllCards.value = UiState.Success(it)
                }
            } catch (e: IOException) {
                _deckWithAllCards.value = UiState.Error(e.toString())
            }
        }
    }

    private val _actualCard = MutableStateFlow<UiState<ImmutableCard>>(UiState.Loading)
    val actualCard: StateFlow<UiState<ImmutableCard>> = _actualCard.asStateFlow()

    fun getActualCard(cardList: List<ImmutableCard>) {
        if (cardPosition >= cardList.size) {
            _actualCard.value = UiState.Error("Quiz Complete")
            cardPosition = 0
        } else {
            fetchJob?.cancel()
            _actualCard.value = UiState.Loading
            fetchJob = viewModelScope.launch {
                _actualCard.value = UiState.Success(cardList[cardPosition])
                cardPosition += 1
            }
        }
    }

}

class BaseFlashCardGameViewModelFactory(private val repository: FlashCardRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BaseFlashCardGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BaseFlashCardGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}