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
    var attemptTime: Int = 0
    private val missedCards: ArrayList<ImmutableCard> = arrayListOf()
    private val _actualCards = MutableStateFlow<UiState<List<MultiChoiceGameCardModel>>>(UiState.Loading)
    val actualCards: StateFlow<UiState<List<MultiChoiceGameCardModel>>> = _actualCards.asStateFlow()
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

    fun increaseAttemptTime() {
        attemptTime += 1
    }

    fun swipe(): Boolean {
        if (attemptTime == 0) {
            progress += 100/cardSum()
        } else {
            attemptTime = 0
        }
        currentCardPosition += 1
        //updateCard()
        return currentCardPosition != cardSum()
    }
    fun getCurrentCardPosition() = currentCardPosition
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
        val missedCard = cardList[currentCardPosition]
        if (missedCard !in missedCards) {
            missedCards.add(missedCard)
        }
        increaseAttemptTime()
    }

    fun isUserChoiceCorrect(userChoice: String, correctChoice: String): Boolean {
        val isCorrect = userChoice == correctChoice
        if (!isCorrect) {
            onCardMissed()
        }
        return isCorrect
    }

    private fun toListOfMultiChoiceQuizGameCardModel(cards: List<ImmutableCard>): List<MultiChoiceGameCardModel> {
        val temporaryList = mutableListOf<MultiChoiceGameCardModel>()
        cards.forEach {
            val alternatives = getWordAlternatives(originalCardList, it.cardDefinition!!, 4)
            temporaryList.add(
                MultiChoiceGameCardModel(
                    it.cardContent!!,
                    it.cardDefinition,
                    alternatives[0],
                    alternatives[1],
                    alternatives[2],
                    alternatives[3],
                )
            )
        }
        return temporaryList
    }

    fun updateCard() {
        if (currentCardPosition == cardList.size) {
            _actualCards.value = UiState.Error("Quiz Complete")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _actualCards.value = UiState.Success(toListOfMultiChoiceQuizGameCardModel(cardList))
            }
        }
    }

}