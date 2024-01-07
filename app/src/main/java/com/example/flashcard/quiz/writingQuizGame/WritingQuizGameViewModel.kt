package com.example.flashcard.quiz.writingQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.quiz.multichoiceQuizGame.MultiChoiceGameCardModel
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WritingQuizGameViewModel: ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    var progress: Int = 0
    private val missedCards: ArrayList<ImmutableCard> = arrayListOf()
    private val _actualCard = MutableStateFlow<UiState<WritingQuizGameModel>>(UiState.Loading)
    val actualCard: StateFlow<UiState<WritingQuizGameModel>> = _actualCard.asStateFlow()
    private lateinit var cardList: List<ImmutableCard>
    lateinit var deck: ImmutableDeck
    private lateinit var originalCardList: List<ImmutableCard>

    fun initCardList(gameCards: List<ImmutableCard>) {
        cardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard>) {
        originalCardList = gameCards
    }

    private val onCardWord
        get() = cardList[currentCardPosition].cardContent
    private val answer
        get() = cardList[currentCardPosition].cardDefinition

    fun onCardMissed() {
        val missedCard = cardList[currentCardPosition]
        if (missedCard !in missedCards) {
            missedCards.add(missedCard)
        }
    }

    fun getMissedCard(): List<ImmutableCard> {
        val newCards = arrayListOf<ImmutableCard>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards.toList()
    }

    fun cardSum() = cardList.size
    fun getMissedCardSum() = missedCards.size
    fun getKnownCardSum() = cardSum() - getMissedCardSum()

    fun initWritingQuizGame() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
    }

    fun swipe(): Boolean {
        progress += 100/cardSum()
        currentCardPosition += 1
        updateCard()
        return currentCardPosition != cardSum()
    }

    fun updateCard() {
        if (currentCardPosition == cardList.size) {
            _actualCard.value = UiState.Error("Quiz Complete")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                    _actualCard.value = UiState.Success(
                        WritingQuizGameModel(onCardWord!!, answer!!)
                    )

            }
        }
    }

}