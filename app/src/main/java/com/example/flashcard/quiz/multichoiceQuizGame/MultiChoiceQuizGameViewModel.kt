package com.example.flashcard.quiz.multichoiceQuizGame

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

class MultiChoiceQuizGameViewModel: ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    var progress: Int = 0
    private val missedCards: ArrayList<ImmutableCard> = arrayListOf()
    private val _actualCard = MutableStateFlow<UiState<MultiChoiceGameCardModel>>(UiState.Loading)
    val actualCard: StateFlow<UiState<MultiChoiceGameCardModel>> = _actualCard.asStateFlow()
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
    private val wordAlternatives
        get() = getWordAlternatives(originalCardList, answer!!, 3)

    private fun getWordAlternatives(
        cards: List<ImmutableCard>,
        onCardWordTranslation: String,
        sum: Int
    ): List<String> {
        val temporaryList = arrayListOf<String>()
        temporaryList.add(onCardWordTranslation)
        while (temporaryList.size < sum) {
            val randomWordTranslation = cards.random().cardDefinition
            if (randomWordTranslation !in temporaryList) {
                if (randomWordTranslation != null) {
                    temporaryList.add(randomWordTranslation)
                }
            }
        }
        return temporaryList.shuffled()
    }
    fun swipe(): Boolean {
        progress += 100/cardSum()
        currentCardPosition += 1
        updateCard()
        return currentCardPosition != cardSum()
    }
    fun cardSum() = cardList.size
    fun getMissedCardSum() = missedCards.size
    fun getKnownCardSum() = cardSum() - getMissedCardSum()
    fun getMissedCard(): List<ImmutableCard> {
        val newCards = arrayListOf<ImmutableCard>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards.toList()
    }
    fun initTimedFlashCard() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
    }

    fun onCardMissed() {
        missedCards.add(cardList[currentCardPosition])
    }

    fun updateCard() {
        if (currentCardPosition == cardList.size) {
            _actualCard.value = UiState.Error("Quiz Complete")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                val wordsAL = wordAlternatives
                _actualCard.value = UiState.Success(MultiChoiceGameCardModel(
                    onCardWord = onCardWord!!,
                    answer = answer!!,
                    alternative1 = wordsAL[0],
                    alternative2 = wordsAL[1],
                    alternative3 = wordsAL[2]
                ))
            }
        }
    }

}