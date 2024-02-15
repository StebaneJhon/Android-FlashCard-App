package com.example.flashcard.quiz.flashCardGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashCardGameViewModel(): ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    private val missedCards: ArrayList<ImmutableCard> = arrayListOf()
    private val _actualCards = MutableStateFlow<UiState<FlashCardGameModel>>(UiState.Loading)
    val actualCards: StateFlow<UiState<FlashCardGameModel>> = _actualCards.asStateFlow()
    private var cardList: List<ImmutableCard>? = null
    var deck: ImmutableDeck? = null
    var progress: Int = 0

    fun initCardList(gameCards: List<ImmutableCard>) {
        cardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    private val topCard
        get() = cardList?.get(currentCardPosition)
    private val bottomCard
        get() = getBottomCard(cardList!!, currentCardPosition)

    fun swipe(isKnown: Boolean): Boolean {
        if (!isKnown) {
            missedCards.add(cardList!![currentCardPosition])
        } else {
            progress += 100/getTotalCards()
        }
        currentCardPosition += 1
        updateOnScreenCards()
        return currentCardPosition != cardList!!.size
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

    fun getMissedCards(): List<ImmutableCard> {
        val newCards = arrayListOf<ImmutableCard>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards.toList()
    }

    private fun getBottomCard(
        cards: List<ImmutableCard>,
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
            if (currentCardPosition == cards.size) {
                _actualCards.value = UiState.Error("Quiz Complete")
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

}