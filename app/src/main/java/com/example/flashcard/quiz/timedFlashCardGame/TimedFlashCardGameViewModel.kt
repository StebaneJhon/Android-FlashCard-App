package com.example.flashcard.quiz.timedFlashCardGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimedFlashCardGameViewModel(
): ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    private val missedCards: ArrayList<ImmutableCard> = arrayListOf()
    private val _actualCards = MutableStateFlow<UiState<TimedFlashCardGameModel>>(UiState.Loading)
    val actualCards: StateFlow<UiState<TimedFlashCardGameModel>> = _actualCards.asStateFlow()
    private var cardList: List<ImmutableCard>? = null
    var deck: ImmutableDeck? = null

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
        if (!isKnown) { missedCards.add(cardList!![currentCardPosition]) }
        currentCardPosition += 1
        updateCards()
        return currentCardPosition != cardList!!.size
    }

    fun getKnownCardSum(): Int {
        cardList?.let { return it.size - missedCards.size }
        return 0
    }

    fun getTotalCards(): Int {
        cardList?.let { return it.size }
        return 0
    }

    fun getMissedCardSum() = missedCards.size

    fun getMissedCard(): List<ImmutableCard> {
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

    fun initTimedFlashCard() {
        missedCards.clear()
        currentCardPosition = 0
    }

    fun updateCards() {
        cardList?.let {cards ->
            if (currentCardPosition == cards.size) {
                _actualCards.value = UiState.Error("Quiz Complete")
            } else {
                fetchJob?.cancel()
                _actualCards.value = UiState.Loading
                fetchJob = viewModelScope.launch {
                    _actualCards.value = UiState.Success(TimedFlashCardGameModel(
                        top = topCard!!,
                        bottom = bottomCard
                    ))
                }
            }
        }
    }

}