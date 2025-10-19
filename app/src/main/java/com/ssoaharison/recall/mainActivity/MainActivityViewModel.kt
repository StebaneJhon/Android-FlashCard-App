package com.ssoaharison.recall.mainActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.models.ExternalCard
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.MainDeck
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivityViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _allCards = MutableStateFlow<UiState<List<ExternalCard?>>>(UiState.Loading)
    val allCards: StateFlow<UiState<List<ExternalCard?>>> = _allCards.asStateFlow()

    private var _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var fetchJob: Job? = null

    init {

        SpaceRepetitionAlgorithmHelper()
        viewModelScope.launch {
            val mainDeck = repository.getMainDeck()
            if (mainDeck == null) {
                repository.insertDeck(MainDeck().getMainDeck())
            }
            _isLoading.value = false
        }

//        getAllCards()
//        SpaceRepetitionAlgorithmHelper()
//        viewModelScope.launch {
//            _allCards.collect { state ->
//                when (state) {
//                    is UiState.Error -> {}
//                    is UiState.Loading -> {}
//                    is UiState.Success -> {
//                        updateAllCardsStatus(state.data)
//                    }
//                }
//            }
//        }
    }

    private fun getAllCards() {
        fetchJob?.cancel()
        _allCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            repository.allCards().collect {
                try {
                    _allCards.value = UiState.Success(it)
                } catch (e: IOException) {
                    _allCards.value = UiState.Error(e.toString())
                }

            }
        }
    }


    private fun updateAllCardsStatus(data: List<ExternalCard?>) {
        data.forEach { card ->
            card?.let {
                updateCardStatus(it)
            }
        }
        _isLoading.value = false
    }

    private fun updateCardStatus(card: ExternalCard) {
        val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
        val isCardForgotten = spaceRepetitionHelper.isForgotten(card)
        if (isCardForgotten) {
            val newStatus = spaceRepetitionHelper.status(card, false)
            val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, false, newStatus)
            val nextForgettingDate =
                spaceRepetitionHelper.nextForgettingDate(card, false, newStatus)
//                val newCard = ImmutableCard(
//                    card.cardId,
//                    card.cardContent,
//                    card.cardDefinition,
//                    card.deckId,
//                    card.isFavorite,
//                    card.revisionTime,
//                    card.missedTime,
//                    card.creationDate,
//                    card.lastRevisionDate,
//                    newStatus,
//                    nextForgettingDate,
//                    nextRevision,
//                    card.cardType,
//                    card.cardContentLanguage,
//                    card.cardDefinitionLanguage,
//                )

            val newCard = Card(
                card.cardId,
                card.deckOwnerId,
                newStatus,
                card.cardType,
                card.revisionTime,
                card.missedTime,
                card.creationDate,
                card.lastRevisionDate,
                nextForgettingDate,
                nextRevision,
                card.cardContentLanguage,
                card.cardDefinitionLanguage,
            )
            updateCard(newCard)
        }
    }

    fun updateCard(card: Card) = viewModelScope.launch {
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