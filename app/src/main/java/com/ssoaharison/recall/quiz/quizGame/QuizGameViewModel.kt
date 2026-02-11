package com.ssoaharison.recall.quiz.quizGame

import android.text.format.DateUtils.formatElapsedTime
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ExternalCardDefinition
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.toLocal
import com.ssoaharison.recall.helper.RoteLearningAlgorithmHelper
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.helper.Calculations
import com.ssoaharison.recall.util.ExternalCardWithContentAndDefinitionAndPosition
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.IOException

class TestQuizGameViewModel(
    private val repository: FlashCardRepository
) : ViewModel() {

    lateinit var cardList: MutableList<ExternalCardWithContentAndDefinitions>
    private var originalCardList: List<ExternalCardWithContentAndDefinitions>? = null
    private val missedCards: ArrayList<ExternalCardWithContentAndDefinitions> = arrayListOf()
    var deck: ExternalDeck? = null

    //    private var attemptTime = 0
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
    private val roteLearningHelper = RoteLearningAlgorithmHelper()
    private var passedCards: Int = 0
    private var restCard = 0
    private var revisedCardsCount = 0
    private var missedAnswersCount = 0

    fun initCardList(gameCards: MutableList<ExternalCardWithContentAndDefinitions>) {
        cardList = gameCards
    }

    fun initDeck(gameDeck: ExternalDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ExternalCardWithContentAndDefinitions>) {
        originalCardList = gameCards
        initRestCards()
    }

    private fun initRestCards() {
        restCard = cardList.size
    }

    private fun initPassedCards() {
        passedCards = 0
    }

    fun initMissedCards() {
        missedCards.clear()
    }

    fun getDeckColorCode() = deck?.deckBackground ?: "black"


    private lateinit var localQuizGameCards: MutableList<QuizGameCardModel>
    private var flowOfLocalQuizGameCards: Flow<List<QuizGameCardModel>> = flow {
        emit(localQuizGameCards)
    }
    private var _externalQuizGameCards =
        MutableStateFlow<UiState<List<QuizGameCardModel>>>(UiState.Loading)
    val externalQuizGameCards: StateFlow<UiState<List<QuizGameCardModel>>> =
        _externalQuizGameCards.asStateFlow()
    private var quizGameJob: Job? = null

    fun getQuizGameCards() {
        quizGameJob?.cancel()
        quizGameJob = viewModelScope.launch {
            try {
                flowOfLocalQuizGameCards.collect {
                    if (it.isEmpty()) {
                        _externalQuizGameCards.value = UiState.Error("No Card")
                    } else {
                        _externalQuizGameCards.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _externalQuizGameCards.value = UiState.Error(e.message.toString())
            }
        }
    }


    fun onRestartQuiz() {
        initPassedCards()
        initRestCards()
    }

    fun setCardAsActualOrPassedByPosition(position: Int) {
        localQuizGameCards[position].setAsActualOrPassed()
    }

    fun setCardAsNotActualOrNotPassedByPosition(position: Int) {
        localQuizGameCards[position].setAsNotActualOrNotPassed()
    }

    fun updateActualCards(amount: Int) {
        val cards = getCardsAmount(cardList, amount)
        localQuizGameCards = cards?.map { card ->
            externalCardWithContentAndDefinitionsToQuizGameCardModel(card)
        }!!.toMutableList()
        localQuizGameCards.first().setAsActualOrPassed()
        revisedCardsCount = localQuizGameCards.size
    }

    fun updateActualCardsWithMissedCards() {
        localQuizGameCards = missedCards.map { card ->
            externalCardWithContentAndDefinitionsToQuizGameCardModel(card)
        }.toMutableList()
        revisedCardsCount = localQuizGameCards.size
        missedCards.clear()
    }

    private fun externalCardWithContentAndDefinitionsToQuizGameCardModel(cardWithContentAndDefinitions: ExternalCardWithContentAndDefinitions): QuizGameCardModel {
        val externalDefinitions = toQuizGameCardDefinitionModel(
            cardWithContentAndDefinitions.card.cardId,
            cardWithContentAndDefinitions.card.cardType!!,
            cardWithContentAndDefinitions.contentWithDefinitions.definitions
        )
        return QuizGameCardModel(
            cardWithContentAndDefinitions.card.cardId,
            cardWithContentAndDefinitions.contentWithDefinitions.content,
            cardWithContentAndDefinitions.card.cardContentLanguage ?: deck?.cardContentDefaultLanguage,
            externalDefinitions,
            cardWithContentAndDefinitions.card.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage,
            cardWithContentAndDefinitions.card.cardType,
            cardWithContentAndDefinitions.card.cardLevel
        )
    }

    fun getQuizGameCardsSum() = localQuizGameCards.size

    fun getMissedCardSum() = missedCards.size

    fun getKnownCardSum() = getQuizGameCardsSum() - getMissedCardSum()

    fun cardLeft() = restCard

    fun submitUserAnswer(answer: QuizGameCardDefinitionModel, cardPosition: Int) {
        val actualCard = localQuizGameCards[cardPosition]
        actualCard.onDefinitionSelected(answer.position!!, answer.isSelected)

    }

    fun getCardByPosition(position: Int) = localQuizGameCards[position]

    fun isAllAnswerSelected(answer: QuizGameCardDefinitionModel, actualCardPosition: Int): Boolean {
        val actualCard = localQuizGameCards[actualCardPosition]
        return actualCard.isAllAnswerSelected()
    }

    private fun toQuizGameCardDefinitionModel(
        cardId: String,
        cardType: String,
        d: List<ExternalCardDefinition>
    ): List<QuizGameCardDefinitionModel> {
        val formatedDefinitions = d.map {
            QuizGameCardDefinitionModel(
                definitionId = it.definitionId,
                cardId = cardId,
                definition = it.definitionText ?: "No text found",
                cardType = cardType,
                isCorrect = it.isCorrectDefinition,
                isSelected = false,
                definitionImage = it.definitionImage,
                definitionAudio = it.definitionAudio,
            )
        }.shuffled()
        return formatedDefinitions.mapIndexed { index, item ->
            item.position = index
            item
        }

    }


    fun sortCardsByLevel() {
        cardList.sortBy { it.card.cardLevel }
    }

    fun shuffleCards() {
        cardList.shuffle()
    }

    fun sortByCreationDate() {
        cardList.sortBy { it.card.creationDate }
    }

    fun cardToReviseOnly() {
        cardList = cardList.filter { spaceRepetitionHelper.isToBeRevised(it.card) } as MutableList<ExternalCardWithContentAndDefinitions>
    }

    fun restoreCardList() {
        cardList = originalCardList!!.toMutableList()
    }

    fun initQuizGame() {
        localQuizGameCards.forEach {
            it.isFlipped = false
            it.isCorrectlyAnswered = false
            it.attemptTime = 0
            it.cardDefinition.forEach { d ->
                d.isSelected = false
            }
        }
        revisedCardsCount = localQuizGameCards.size
        missedAnswersCount = 0
    }

    fun resetLocalQuizGameCardsState() {
        localQuizGameCards.forEach {
            it.isFlipped = false
            it.isCorrectlyAnswered = false
            it.attemptTime = 0
            it.cardDefinition.forEach { d ->
                d.isSelected = false
            }
        }
        missedAnswersCount = 0
    }

    fun initCardFlipCount(cardPosition: Int) {
        localQuizGameCards[cardPosition].flipCount = 0
    }

    fun increaseCardFlipCount(cardPosition: Int) {
        localQuizGameCards[cardPosition].flipCount++
    }

    fun updateMultipleAnswerAndChoiceCardOnAnswered(
        answer: QuizGameCardDefinitionModel,
        cardPosition: Int,
    ) {
        val actualCard = localQuizGameCards[cardPosition]
        if (answer.giveFeedbackOnSelected()) {
            upOrDowngradeCard(true, answer.cardId)
        } else {
            upOrDowngradeCard(false, answer.cardId)
            getExternalCardWithContentAndDefinitionsAndPositionById(actualCard.cardId)?.card?.let {
                missedCards.add(it)
                val cardToRepeat = externalCardWithContentAndDefinitionsToQuizGameCardModel(it)
                val cardToRepeatNewPosition = roteLearningHelper.onRepeatCardPosition(localQuizGameCards, cardPosition)
                localQuizGameCards.add(cardToRepeatNewPosition, cardToRepeat)
                missedAnswersCount++
            }
        }
    }

    fun updateSingleAnsweredCardOnKnownOrKnownNot(
        knownOrNot: Boolean,
        cardPosition: Int,
    ) {
        val actualCard = localQuizGameCards[cardPosition]
        upOrDowngradeCard(knownOrNot, actualCard.cardId)
        if (!knownOrNot) {
            getExternalCardWithContentAndDefinitionsAndPositionById(actualCard.cardId)?.card?.let {
                missedCards.add(it)
                val currentCard = externalCardWithContentAndDefinitionsToQuizGameCardModel(it)
                localQuizGameCards.add(currentCard)
                missedAnswersCount++
            }
        } else {
            actualCard.isCorrectlyAnswered = true
        }
        initCardFlipCount(cardPosition)
    }

    fun getAnswersCount(): Int {
        var answersCount = 0
        localQuizGameCards.forEach { card ->
            answersCount += card.cardDefinition.size
        }
        return answersCount
    }

    fun getUserAnswerAccuracyFraction() = Calculations().fractionOfPart(getAnswersCount(), missedAnswersCount)

    fun getUserAnswerAccuracy() = Calculations().percentageOfRest(getAnswersCount(), missedAnswersCount)

    private fun getExternalCardWithContentAndDefinitionsAndPositionById(cardId: String): ExternalCardWithContentAndDefinitionAndPosition? {
        var i = 0
        while (i < cardList.size) {
            val c = cardList[i]
            if (c.card.cardId == cardId) {
                return ExternalCardWithContentAndDefinitionAndPosition(c, i)
            }
            i++
        }
        return null
    }

    private fun getCardsAmount(
        quizCardList: List<ExternalCardWithContentAndDefinitions>,
        amount: Int
    ): List<ExternalCardWithContentAndDefinitions>? {

        return when {
            quizCardList.isEmpty() -> {
                null
            }

            quizCardList.size == amount -> {
                quizCardList
            }

            else -> {
                var result = listOf<ExternalCardWithContentAndDefinitions>()
                if (passedCards <= quizCardList.size) {
                    if (restCard > amount) {
                        result = quizCardList.slice(passedCards..passedCards.plus(amount).minus(1))
                        passedCards += amount
                    } else {
                        result = quizCardList.slice(passedCards..quizCardList.size.minus(1))
                            .toMutableList()
                        passedCards += quizCardList.size.plus(50)
                    }
                }
                restCard = quizCardList.size - passedCards
                result
            }
        }
    }

    fun getRevisedCardsCount() = revisedCardsCount

    private fun upOrDowngradeCard(isKnown: Boolean, cardId: String) {
        val cardWithPosition = getExternalCardWithContentAndDefinitionsAndPositionById(cardId)
        val card = cardWithPosition?.card
        val cardPosition = cardWithPosition?.position
        if (card != null) {
            val newCard = spaceRepetitionHelper.rescheduleExternalCardWithContentAndDefinitions(card, isKnown)
            updateCard(newCard, cardPosition!!)
        }
    }

    fun updateCard(
        card: ExternalCardWithContentAndDefinitions,
        position: Int
    ) = viewModelScope.launch {
        cardList[position] = card
        repository.updateCard(card.card.toLocal())
    }

    fun updateCardContentLanguage(cardId: String, language: String) = viewModelScope.launch {
        repository.updateCardContentLanguage(cardId, language)
    }

    fun updateCardDefinitionLanguage(cardId: String, language: String) = viewModelScope.launch {
        repository.updateCardDefinitionLanguage(cardId, language)
    }

    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()
    private var timerJob: Job? = null

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timer.value++
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun stopTimer() {
        _timer.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun formatTime(seconds: Long): String {
        return formatElapsedTime(seconds)
    }

}

class TestQuizGameViewModelFactory(
    private val repository: FlashCardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestQuizGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestQuizGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}