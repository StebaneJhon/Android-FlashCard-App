package com.example.flashcard.quiz.testQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TestQuizGameViewModel (
    private val repository: FlashCardRepository
): ViewModel() {

    private lateinit var cardList: MutableList<ImmutableCard?>
    private var originalCardList: List<ImmutableCard?>? = null
    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
    var deck: ImmutableDeck? = null
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()


    private var currentCardPosition: Int = 0
    var progress: Int = 0

    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
        originalCardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
        originalCardList = gameCards
    }

    fun getDeckColorCode() = deck?.deckColorCode ?: "black"

    private val _actualCards = MutableStateFlow<UiState<List<ImmutableCard?>>>(UiState.Loading)
    val actualCards: StateFlow<UiState<List<ImmutableCard?>>> = _actualCards.asStateFlow()
    private var fetchJob: Job? = null
    fun getCards() {
        if (cardList.size == 0) {
            _actualCards.value = UiState.Error("No Cards To Revise")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _actualCards.value = UiState.Success(cardList)
            }
        }
    }

    private fun onCardSwiped(isKnown: Boolean) {
        cardList?.let {cards ->
            val card = cards[currentCardPosition]
            if (card != null) {
                val newStatus = spaceRepetitionHelper.status(card, isKnown)
                val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, isKnown, newStatus)
                val lastRevision = spaceRepetitionHelper.today()
                val nextForgettingDate = spaceRepetitionHelper.nextForgettingDate(card, isKnown, newStatus)
                val newCard = ImmutableCard(
                    card.cardId,
                    card.cardContent,
                    card.contentDescription,
                    card.cardDefinition,
                    card.valueDefinition,
                    card.deckId,
                    card.backgroundImg,
                    card.isFavorite,
                    card.revisionTime,
                    card.missedTime,
                    card.creationDate,
                    lastRevision,
                    newStatus,
                    nextForgettingDate,
                    nextRevision,
                    card.cardType,
                    card.creationDateTime
                )
                updateCard(newCard)
            }

        }
    }

    fun updateCard(card: ImmutableCard) = viewModelScope.launch {
        repository.updateCard(card)
    }

}

class TestQuizGameViewModelFactory(
    private val repository: FlashCardRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestQuizGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestQuizGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}