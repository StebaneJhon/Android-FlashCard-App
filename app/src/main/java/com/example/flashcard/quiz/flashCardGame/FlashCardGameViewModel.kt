package com.example.flashcard.quiz.flashCardGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.CardLevel.L1
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashCardGameViewModel(
    private val repository: FlashCardRepository
): ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
    private val _actualCards = MutableStateFlow<UiState<FlashCardGameModel>>(UiState.Loading)
    val actualCards: StateFlow<UiState<FlashCardGameModel>> = _actualCards.asStateFlow()
    private var originalCardList: List<ImmutableCard?>? = null
    private var cardList: MutableList<ImmutableCard?>? = null
    var deck: ImmutableDeck? = null
    var progress: Int = 0

    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()


    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
        originalCardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    private val topCard
        get() = cardList?.get(currentCardPosition)
    private val bottomCard
        get() = cardList?.let { getBottomCard(it, currentCardPosition) }

    fun sortCardsByLevel() {
        cardList?.sortBy { it?.cardStatus }
    }

    fun shuffleCards() {
        cardList?.shuffle()
    }

    fun sortByCreationDate() {
        cardList?.sortBy { it?.cardId }
    }

    fun unknownCardsOnly() {
        cardList = cardList?.filter { it?.cardStatus == L1 } as MutableList<ImmutableCard?>
    }

    fun cardToReviseOnly() {
        cardList = cardList?.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
    }

    fun restoreCardList() {
        cardList = originalCardList?.toMutableList()
    }

    fun swipe(isKnown: Boolean): Boolean {
        val isQuizComplete = currentCardPosition == cardList?.size?.minus(1)
        if (!isKnown) {
            missedCards.add(cardList!![currentCardPosition])
        } else {
            progress += 100/getTotalCards()
        }
        onCardSwiped(isKnown)
        if (!isQuizComplete) {
            currentCardPosition += 1
            updateOnScreenCards()
        }

        return isQuizComplete
    }

    fun rewind() {
        currentCardPosition -= 1
        cardList?.get(currentCardPosition)?.let {
            if (it in missedCards) {
                missedCards.remove(it)
            } else {
                progress -= 100/getTotalCards()
            }
        }
        updateOnScreenCards()
    }

    fun getKnownCardSum(): Int {
        cardList?.let { return it.size - missedCards.size }
        return 0
    }

    fun getTotalCards(): Int {
        cardList?.let { return it.size }
        return 0
    }

    fun getCardBackground() = deck?.deckColorCode

    fun getCurrentCardNumber() = currentCardPosition.plus(1)

    fun getMissedCardSum() = missedCards.size

    fun getMissedCards(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    private fun getBottomCard(
        cards: List<ImmutableCard?>,
        currentCartPosition: Int
    ): ImmutableCard? {
        return if (currentCartPosition > cards.size-2) {
            null
        } else {
            cards[currentCartPosition + 1]
        }
    }

    fun initFlashCard() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
    }

    fun updateOnScreenCards() {
        cardList?.let {cards ->
            if (cards.size == 0) {
                _actualCards.value = UiState.Error("No Cards To Revise")
            } else {
                fetchJob?.cancel()
                _actualCards.value = UiState.Loading
                fetchJob = viewModelScope.launch {
                        _actualCards.value = UiState.Success(
                            FlashCardGameModel(
                                top = topCard!!,
                                bottom = bottomCard
                            )
                    )
                }
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

class FlashCardGameViewModelFactory(private val repository: FlashCardRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashCardGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashCardGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }

}