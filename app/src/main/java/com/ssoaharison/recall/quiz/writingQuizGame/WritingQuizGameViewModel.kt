package com.ssoaharison.recall.quiz.writingQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.Model.ImmutableCard
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.util.CardLevel
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.ssoaharison.recall.util.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WritingQuizGameViewModel(
    private val repository: FlashCardRepository
) : ViewModel() {

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
    private var passedCards: Int = 0
    private var restCard = 0


    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()

    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
        restCard = cardSum()
    }

    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
        originalCardList = gameCards
    }

    fun initCurrentCardPosition() {
        currentCardPosition = 0
    }

    fun initProgress() {
        progress = 0
    }

    private fun initPassedCards() {
        passedCards = 0
    }

    private fun initRestCards() {
        restCard = cardSum()
    }

    fun initMissedCards() {
        missedCards.clear()
    }

    fun onRestartQuiz() {
        initWritingQuizGame()
        initPassedCards()
        initRestCards()
    }

    fun onCardMissed(cardId: String) {
        val missedCard = getLocalCardById(cardId)
        if (missedCard !in missedCards) {
            missedCards.add(missedCard)
        }
        increaseAttemptTime()
    }

    fun getLocalCardById(cardId: String): ImmutableCard? {
        var i = 0
        var result: ImmutableCard? = null
        while (true) {
            val actualCard = cardList[i]
            if (actualCard?.cardId == cardId) {
                result = actualCard
                break
            }
            i++
        }
        return result
    }

    fun getMissedCard(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    fun cardLeft() = restCard

    fun cardSum() = cardList.size
    fun getMissedCardSum() = missedCards.size
    fun getKnownCardSum(cardCount: Int) = cardCount - getMissedCardSum()
    fun initWritingQuizGame() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
    }

    fun increaseAttemptTime() {
        attemptTime += 1
    }

    fun swipe(cardCount: Int): Boolean {
        if (attemptTime == 0) {
            progress += 100 / cardSum()
        } else {
            attemptTime = 0
        }
        currentCardPosition += 1
        return currentCardPosition != cardCount
    }

    fun getCurrentCardPosition() = currentCardPosition

    fun isUserAnswerCorrect(
        userAnswer: String,
        correctAnswers: List<String>,
        cardId: String
    ): Boolean {
        var isCorrect = false
        correctAnswers.forEach { answer ->
            if (answer.trim().lowercase() == userAnswer) {
                isCorrect = true
            }
        }
        if (isCorrect == false) {
            onCardMissed(cardId)
            onUserAnswered(isCorrect, cardId)
        }
        if (attemptTime == 0 && isCorrect) {
            onUserAnswered(isCorrect, cardId)
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
        cardList =
            cardList.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
    }

    fun restoreCardList() {
        cardList = originalCardList!!.toMutableList()
    }

    private fun cardToWritingQuizGameItem(
        cards: List<ImmutableCard?>,
        cardOrientation: String
    ): List<WritingQuizGameModel> {
        val newList = mutableListOf<WritingQuizGameModel>()
        if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
            cards.forEach { item ->
                val correctAlternatives = getCorrectDefinitions(item?.cardDefinition)
                newList.add(
                    WritingQuizGameModel(
                        item?.cardId!!,
                        item.cardContent?.content!!,
                        correctAlternatives
                    )
                )
            }
        } else {
            cards.forEach { item ->
                val correctAlternatives = getCorrectDefinitions(item?.cardDefinition)
                newList.add(
                    WritingQuizGameModel(
                        item?.cardId!!,
                        correctAlternatives.random(),
                        listOf(item.cardContent?.content!!)
                    )
                )
            }
        }
        return newList
    }

    private fun getCorrectDefinitions(definitions: List<CardDefinition>?): List<String> {
        val correctDefinitions =
            definitions?.let { defins -> defins.filter { isCorrect(it.isCorrectDefinition) } }
        val correctAlternative = mutableListOf<String>()
        correctDefinitions?.forEach {
            correctAlternative.add(it.definition)
        }
        return correctAlternative
    }

    fun isCorrect(index: Int?) = index == 1
    fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

    fun updateOnscreenCard(cardOrientation: String, cardCount: Int) {
        when {
            passedCards == cardList.size -> {
                _actualCard.value = UiState.Error("Quiz Complete")
            }

            cardList.size == 0 -> {
                _actualCard.value = UiState.Error("No Cards To Revise")
            }

            else -> {
                fetchJob?.cancel()
                fetchJob = viewModelScope.launch {
                    val cards = getCardsAmount(cardList, cardCount)
                    if (cards.isNullOrEmpty()) {
                        _actualCard.value = UiState.Error("Too Few Cards")
                    } else {
                        _actualCard.value = UiState.Success(
                            cardToWritingQuizGameItem(cards, cardOrientation)
                        )
                    }
                }
            }
        }
    }

    fun updateCardOnReviseMissedCards(cardOrientation: String) {
        when {
            cardList.size == 0 -> {
                _actualCard.value = UiState.Error("No Cards To Revise")
            }

            else -> {
                fetchJob?.cancel()
                fetchJob = viewModelScope.launch {
                    _actualCard.value = UiState.Success(
                        cardToWritingQuizGameItem(missedCards, cardOrientation)
                    )
                    missedCards.clear()
                }
            }
        }
    }

    fun getCardsAmount(
        quizCardList: List<ImmutableCard?>,
        amount: Int
    ): List<ImmutableCard?>? {

        return when {
            quizCardList.isEmpty() -> {
                null
            }

            quizCardList.size == amount -> {
                quizCardList
            }

            else -> {
                var result = listOf<ImmutableCard?>()
                if (passedCards <= quizCardList.size) {
                    if (restCard > amount) {
                        result = quizCardList.slice(passedCards..passedCards.plus(amount).minus(1))
                        passedCards += amount
                    } else {
                        result = quizCardList.slice(passedCards..quizCardList.size.minus(1)).toMutableList()
                        passedCards += quizCardList.size.plus(50)
                    }
                }
                restCard = quizCardList.size - passedCards
                result
            }
        }
    }

    private fun onUserAnswered(
        isKnown: Boolean,
        cardId: String
    ) {
        val card = getLocalCardById(cardId)
        if (card != null) {
            val newStatus = spaceRepetitionHelper.status(card, isKnown)
            val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, isKnown, newStatus)
            val lastRevision = spaceRepetitionHelper.today()
            val nextForgettingDate =
                spaceRepetitionHelper.nextForgettingDate(card, isKnown, newStatus)
            val newCard = ImmutableCard(
                card.cardId,
                card.cardContent,
                card.cardDefinition,
                card.deckId,
                card.isFavorite,
                card.revisionTime,
                card.missedTime,
                card.creationDate,
                lastRevision,
                newStatus,
                nextForgettingDate,
                nextRevision,
                card.cardType,
            )
            updateCard(newCard)
        }
    }

    private fun updateCard(card: ImmutableCard) = viewModelScope.launch {
        repository.updateCard(card)
    }


}

class WritingQuizGameViewModelFactory(private val repository: FlashCardRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WritingQuizGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WritingQuizGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}