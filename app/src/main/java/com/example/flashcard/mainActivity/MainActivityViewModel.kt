package com.example.flashcard.mainActivity

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivityViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _allCards = MutableStateFlow<UiState<List<ImmutableCard?>>>(UiState.Loading)
    val allCards: StateFlow<UiState<List<ImmutableCard?>>> = _allCards.asStateFlow()

    private var _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var fetchJob: Job? = null

    init {
        getAllCards()
        viewModelScope.launch {
            _allCards.collect { state ->
                when (state) {
                    is UiState.Error -> {}
                    is UiState.Loading -> {}
                    is UiState.Success -> {
                        updateAllCardsStatus(state.data)
                    }
                }
            }
        }
    }

    fun getAllCards() {
        fetchJob?.cancel()
        _allCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            repository.allCards().collect {
                try {
                    _allCards.value = success(it)
                } catch (e: IOException) {
                    _allCards.value = UiState.Error(e.toString())
                }

            }
        }
    }

    private fun success(it: List<ImmutableCard?>) =
        UiState.Success(it)

    private fun updateAllCardsStatus(data: List<ImmutableCard?>) {
        data.forEach { card ->
            updateCardStatus(card)
        }
        _isLoading.value = false
    }

    private fun updateCardStatus(card: ImmutableCard?) {
        if (card != null) {
            val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
            val isCardForgotten = spaceRepetitionHelper.isForgotten(card)
            if (isCardForgotten) {
                val newStatus = spaceRepetitionHelper.status(card, false)
                val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, false, newStatus)
                val nextForgettingDate =
                    spaceRepetitionHelper.nextForgettingDate(card, false, newStatus)
                val newCard = ImmutableCard(
                    card.cardId,
                    card.cardContent,
                    card.cardDefinition,
                    card.deckId,
                    card.isFavorite,
                    card.revisionTime,
                    card.missedTime,
                    card.creationDate,
                    card.lastRevisionDate,
                    newStatus,
                    nextForgettingDate,
                    nextRevision,
                    card.cardType,
                )
                updateCard(newCard)
            }
        }

    }

    fun updateCard(card: ImmutableCard) = viewModelScope.launch {
        repository.updateCard(card)
    }

}

class MainActivityViewModelFactory(private val repository: FlashCardRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}