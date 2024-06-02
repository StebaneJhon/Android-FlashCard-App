package com.example.flashcard.quiz.writingQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.CardLevel
import com.example.flashcard.util.FlashCardMiniGameRef
import com.example.flashcard.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WritingQuizGameViewModel(
    private val repository: FlashCardRepository
): ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    var progress: Int = 0
    var attemptTime: Int = 0
    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
    private val _actualCard = MutableStateFlow<UiState<List<WritingQuizGameModel>>>(UiState.Loading)
    val actualCard: StateFlow<UiState<List<WritingQuizGameModel>>> = _actualCard.asStateFlow()
    private lateinit var cardList: MutableList<ImmutableCard?>
    lateinit var deck: ImmutableDeck
    private var originalCardList: List<ImmutableCard?>? = null




    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()

    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
        originalCardList = gameCards
    }

    fun onCardMissed() {
        val missedCard = cardList[currentCardPosition]
        if (missedCard !in missedCards) {
            missedCards.add(missedCard)
        }
        increaseAttemptTime()
    }

    fun getMissedCard(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    fun cardSum() = cardList.size
    fun getMissedCardSum() = missedCards.size
    fun getKnownCardSum() = cardSum() - getMissedCardSum()
    fun initWritingQuizGame() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
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
        return currentCardPosition != cardSum()
    }

    fun getCurrentCardPosition() = currentCardPosition

    fun isUserAnswerCorrect(userAnswer: String, correctAnswers: List<String>): Boolean {
        var isCorrect = false
        correctAnswers.forEach { answer ->
            if (answer.trim().lowercase() == userAnswer) {
                isCorrect = true
            }
        }
        if (isCorrect == false) {
            onCardMissed()
            onUserAnswered(isCorrect)
        }
        if (attemptTime == 0 && isCorrect) {
            onUserAnswered(isCorrect)
        }
        return isCorrect
    }

    fun sortCardsByLevel() {
        cardList.sortBy { it?.cardStatus }
    }

    fun shuffleCards() {
        cardList.shuffle()
    }

    fun sortByCreationDate() {
        cardList.sortBy { it?.cardId }
    }

    fun unknownCardsOnly() {
        cardList = cardList.filter { it?.cardStatus == CardLevel.L1 } as MutableList<ImmutableCard?>
    }

    fun cardToReviseOnly() {
        cardList = cardList.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
    }

    fun restoreCardList() {
        cardList = originalCardList!!.toMutableList()
    }

    private fun cardToWritingQuizGameItem(cards: List<ImmutableCard?>, cardOrientation: String): List<WritingQuizGameModel> {
        val newList = mutableListOf<WritingQuizGameModel>()
        if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
            cards.forEach { item ->
                val correctAlternatives = getCorrectDefinitions(item?.cardDefinition)!!
                newList.add(
                    WritingQuizGameModel(
                        item?.cardContent?.content!!,
                        correctAlternatives
                    )
                )
            }
        } else {
            cards.forEach { item ->
                val correctAlternatives = getCorrectDefinitions(item?.cardDefinition)!!
                newList.add(
                    WritingQuizGameModel(
                        correctAlternatives.random(),
                        listOf(item?.cardContent?.content!!)
                    )
                )
            }
        }
        return newList
    }

    private fun getCorrectDefinitions(definitions: List<CardDefinition>?): List<String>? {
        val correctDefinitions = definitions?.let {defins -> defins.filter { it.isCorrectDefinition!! }}
        val correctAlternative = mutableListOf<String>()
        correctDefinitions?.forEach {
            correctAlternative.add(it.definition!!)
        }
        return  correctAlternative
    }

    fun updateCard(cardOrientation: String) {
        if (cardList.size == 0) {
            _actualCard.value = UiState.Error("No Cards To Revise")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                    _actualCard.value = UiState.Success(
                        cardToWritingQuizGameItem(cardList, cardOrientation)
                    )

            }
        }
    }

    private fun onUserAnswered(isKnown: Boolean) {
        cardList.let {cards ->
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

class WritingQuizGameViewModelFactory(private val repository: FlashCardRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WritingQuizGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WritingQuizGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}