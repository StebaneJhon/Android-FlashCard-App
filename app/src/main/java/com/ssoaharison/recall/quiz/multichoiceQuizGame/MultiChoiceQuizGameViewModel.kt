package com.ssoaharison.recall.quiz.multichoiceQuizGame

import android.text.format.DateUtils.formatElapsedTime
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.helper.RoteLearningAlgorithmHelper
import com.ssoaharison.recall.util.CardLevel
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.Calculations
import com.ssoaharison.recall.util.ImmutableCardWithPosition
import com.ssoaharison.recall.util.LanguageUtil
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextType.DEFINITION
import com.ssoaharison.recall.util.TextWithLanguageModel
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

class MultiChoiceQuizGameViewModel(
    private val repository: FlashCardRepository
) : ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    private var progress: Int = 0
    private var attemptTime: Int = 0
    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
    private lateinit var cardList: MutableList<ImmutableCard?>
    lateinit var deck: ImmutableDeck
    private lateinit var originalCardList: List<ImmutableCard?>
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
    private val roteLearningHelper = RoteLearningAlgorithmHelper()
    private var passedCards: Int = 0
    private var restCard = 0
    private var revisedCardsCount = 0
    private var missedAnswersCount = 0

    fun detectDefaultContentLanguages(): String? {
        var contentLanguage: String? = null
        if (deck.cardContentDefaultLanguage.isNullOrBlank()) {
            LanguageUtil().detectLanguage(
                text = cardList.first()?.cardContent?.content!!,
                onError = { contentLanguage = null },
                onLanguageUnIdentified = { contentLanguage = null },
                onLanguageNotSupported = { contentLanguage = null },
                onSuccess = { contentLanguage = it }
            )
        } else {
            contentLanguage =  deck.cardContentDefaultLanguage
        }
        return contentLanguage
    }



    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
        initRestCards()
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

    fun onRestartQuiz() {
        stopTimer()
        missedAnswersCount = 0
        initTimedFlashCard()
        initPassedCards()
        initRestCards()
        initAttemptTime()
    }

    private fun getAlternatives(
        correctAlternative: MultiChoiceCardDefinitionModel,
        cardOrientation: String,
        actualCardId: String,
    ): List<MultiChoiceCardDefinitionModel> {

        val temporaryMultiChoiceCardDefinitionList = arrayListOf<MultiChoiceCardDefinitionModel>()
        val addedDefinitionTexts = arrayListOf<String>()
        temporaryMultiChoiceCardDefinitionList.add(correctAlternative)
        addedDefinitionTexts.add(correctAlternative.definition.text)

        while (temporaryMultiChoiceCardDefinitionList.size < 4) {
            val randomCard = originalCardList.random()
            var randomDefinition: Any?
            var selectedDefinitionLanguage: String?
            if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
                randomDefinition = randomCard?.cardDefinition?.random()
                selectedDefinitionLanguage =
                    randomCard?.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
            } else {
                randomDefinition = randomCard?.cardContent
                selectedDefinitionLanguage =
                    randomCard?.cardContentLanguage ?: deck.cardContentDefaultLanguage!!
            }
            if (randomDefinition!!::class.java.typeName.equals("com.ssoaharison.recall.backend.entities.CardDefinition")) {
                if ((randomDefinition as CardDefinition).definition !in addedDefinitionTexts) {
                    temporaryMultiChoiceCardDefinitionList.add(
                        MultiChoiceCardDefinitionModel(
                            cardId = actualCardId,
                            definition = TextWithLanguageModel(
                                randomDefinition.cardId,
                                randomDefinition.definition,
                                DEFINITION,
                                selectedDefinitionLanguage
                            ),
                            isCorrect = false,
                            isSelected = false
                        )
                    )
                    addedDefinitionTexts.add(randomDefinition.definition)
                }
            } else {
                if ((randomDefinition as CardContent).content !in addedDefinitionTexts) {
                    temporaryMultiChoiceCardDefinitionList.add(
                        MultiChoiceCardDefinitionModel(
                            cardId = actualCardId,
                            definition = TextWithLanguageModel(
                                randomDefinition.cardId,
                                randomDefinition.content,
                                CONTENT,
                                selectedDefinitionLanguage
                            ),
                            isCorrect = false,
                            isSelected = false
                        )
                    )
                    addedDefinitionTexts.add(randomDefinition.content)
                }
            }
        }

        val shuffledCards = temporaryMultiChoiceCardDefinitionList.shuffled()

        return shuffledCards.mapIndexed { index, item ->
            item.position = index
            item
        }
    }

    private fun getRandomCorrectDefinition(definitions: List<CardDefinition>?): String? {
        val correctDefinitions =
            definitions?.let { defins -> defins.filter { isCorrect(it.isCorrectDefinition) } }
        return correctDefinitions?.random()?.definition
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

    private fun increaseAttemptTime() {
        attemptTime += 1
    }

    fun increaseCurrentCardPosition(cardCount: Int): Boolean {
        if (currentCardPosition < cardCount) {
            currentCardPosition += 1
        }
        return currentCardPosition != cardCount
    }

    fun decreaseCurrentCardPosition() {
        if (currentCardPosition > 0) {
            currentCardPosition -= 1
        }
    }

    fun initAttemptTime() {
        attemptTime = 0
    }

    fun getCurrentCardPosition() = currentCardPosition
    fun cardSum() = cardList.size
    fun getMissedCardSum() = missedCards.size
    fun getKnownCardSum(cardCount: Int) = cardCount - getMissedCardSum()

    fun cardLeft() = restCard

    fun initTimedFlashCard() {
        stopTimer()
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
        missedAnswersCount = 0
        localMultiChoiceCards.forEach {
            it.attemptTime = 0
            it.isCorrectlyAnswered = false
            it.alternatives.forEach { a ->
                a.isSelected = false
            }
        }
        revisedCardsCount = localMultiChoiceCards.size
    }

    private fun getLocalCardWithPositionById(cardId: String): ImmutableCardWithPosition {
        var i = 0
        while (true) {
            val c = cardList[i]
            if (c?.cardId == cardId) {
                return ImmutableCardWithPosition(c, i)
            }
            i++
        }
    }

    private fun getOriginalCardById(cardId: String): ImmutableCard? {
        originalCardList.forEach { card ->
            if (card?.cardId == cardId) {
                return card
            }
        }
        return null
    }

    fun getRevisedCardsCount() = revisedCardsCount

    fun isUserChoiceCorrect(
        userChoice: MultiChoiceCardDefinitionModel,
        cardOrientation: String,
        currentCardPosition: Int
        ): Boolean {
        if (userChoice.isSelected || userChoice.position != null) {
            if (!userChoice.isCorrect) {
                missedAnswersCount++
            }
            val temporaryCard = localMultiChoiceCards[currentCardPosition]
            val temporaryAlternative = temporaryCard.alternatives[userChoice.position!!]
            temporaryAlternative.isSelected = userChoice.isSelected
            temporaryCard.attemptTime++
            temporaryCard.isCorrectlyAnswered = userChoice.isCorrect
            updateCardOnKnownOrKnownNot(userChoice)
            if (attemptTime < 1) {
                onUserAnswered(userChoice.isSelected && userChoice.isCorrect, temporaryCard.cardId)
                if (!(userChoice.isSelected && userChoice.isCorrect)) {
                    val currentCard = localCardToMultiChoiceGameCardMode(getOriginalCardById(temporaryCard.cardId), cardOrientation)
                    val currentNewCardPosition = roteLearningHelper.onRepeatCardPosition(localMultiChoiceCards, currentCardPosition)
                    localMultiChoiceCards.add(currentNewCardPosition, currentCard)
                }
            }

            increaseAttemptTime()
            return userChoice.isCorrect
        }
        return false
    }

    fun getAttemptTime() = attemptTime

    private fun updateCardOnKnownOrKnownNot(
        answer: MultiChoiceCardDefinitionModel
    ): Boolean {
        // To be optimized
        originalCardList.forEach {
            if (it?.cardId == answer.cardId) {
                if (answer.isSelected && !answer.isCorrect) {
                    if (it !in missedCards) {
                        missedCards.add(it)
                    }
                }
                return answer.isSelected && !answer.isCorrect
            }
        }
        return false
    }

    fun setCardAsActualOrPassedByPosition(position: Int) {
        localMultiChoiceCards[position].setAsActualOrPassed()
    }

    fun setCardAsNotActualOrNotPassedByPosition(position: Int) {
        localMultiChoiceCards[position].setAsNotActualOrNotPassed()
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
        cardList = originalCardList.toMutableList()
    }

    private lateinit var localMultiChoiceCards: MutableList<MultiChoiceGameCardModel>
    private var flowOfLocalMultiChoiceCards: Flow<List<MultiChoiceGameCardModel>> = flow {
        emit(localMultiChoiceCards)
    }
    private var _externalMultiChoiceCards =
        MutableStateFlow<UiState<List<MultiChoiceGameCardModel>>>(UiState.Loading)
    val externalMultiChoiceCards: StateFlow<UiState<List<MultiChoiceGameCardModel>>> =
        _externalMultiChoiceCards.asStateFlow()

    fun getMultiChoiceCards() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                flowOfLocalMultiChoiceCards.collect {
                    if (it.isEmpty()) {
                        _externalMultiChoiceCards.value = UiState.Error("No Cards To Revise")
                    } else {
                        _externalMultiChoiceCards.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _externalMultiChoiceCards.value = UiState.Error(e.message.toString())
            }
        }
    }

    fun getAnswersCount(): Int {
        var answersCount = 0
        localMultiChoiceCards.forEach { card ->
            answersCount += card.alternatives.size
        }
        return answersCount
    }

    fun getUserAnswerAccuracy(): Int {
        val answerCount = getAnswersCount()
        return Calculations().percentageOfRest(answerCount, missedAnswersCount)
    }

    fun getUserAnswerAccuracyFraction() = Calculations().fractionOfPart(getAnswersCount(), missedAnswersCount)

    fun updateActualCards(amount: Int, cardOrientation: String) {
        val cards = getCardsAmount(amount)
        localMultiChoiceCards = cards!!.map { card ->
            localCardToMultiChoiceGameCardMode(card, cardOrientation)
        }.toMutableList()
        localMultiChoiceCards.first().setAsActualOrPassed()
        revisedCardsCount = localMultiChoiceCards.size
    }

    private fun localCardToMultiChoiceGameCardMode(
        card: ImmutableCard?,
        cardOrientation: String
    ): MultiChoiceGameCardModel {
        val correctAlternative = getRandomCorrectDefinition(card?.cardDefinition)!!
        return if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
            val correctAlternativeDefinitionModel = MultiChoiceCardDefinitionModel(
                cardId = card?.cardId!!,
                definition = TextWithLanguageModel(
                    card.cardId,
                    correctAlternative,
                    DEFINITION,
                    card.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
                ),
                isCorrect = true,
                isSelected = false,
            )
            val definitions = getAlternatives(correctAlternativeDefinitionModel, cardOrientation, card.cardId)
            MultiChoiceGameCardModel(
                card.cardId,
                TextWithLanguageModel(
                    card.cardId,
                    card.cardContent?.content!!,
                    CONTENT,
                    card.cardContentLanguage ?: deck.cardContentDefaultLanguage
                ),
                definitions,
            )
        } else {
            val correctAlternativeDefinitionModel = MultiChoiceCardDefinitionModel(
                cardId = card?.cardId!!,
                definition = TextWithLanguageModel(
                    card.cardId,
                    card.cardContent?.content!!,
                    CONTENT,
                    card.cardContentLanguage ?: deck.cardContentDefaultLanguage
                ),
                isCorrect = true,
                isSelected = false,
            )
            val definitions = getAlternatives(correctAlternativeDefinitionModel, cardOrientation, card.cardId)
            MultiChoiceGameCardModel(
                card.cardId,
                TextWithLanguageModel(
                    card.cardId,
                    correctAlternative,
                    DEFINITION,
                    card.cardDefinitionLanguage ?: deck.cardDefinitionDefaultLanguage
                ),
                definitions,
            )
        }
    }

    fun updateActualCardsWithMissedCards(cardOrientation: String) {
        localMultiChoiceCards = missedCards.map { card ->
            localCardToMultiChoiceGameCardMode(card, cardOrientation)
        }.toMutableList()
        revisedCardsCount = localMultiChoiceCards.size
        missedCards.clear()
    }

    fun isNextCardAnsweredCorrectly(): Boolean {
        val nextCardPosition = currentCardPosition + 1
        if (nextCardPosition < localMultiChoiceCards.size) {
            val nexCard = localMultiChoiceCards[nextCardPosition]
            nexCard.alternatives.forEach {
                if (it.isCorrect && it.isSelected) {
                    return true
                }
            }
        }
        return false
    }

    private fun getCardsAmount(amount: Int): List<ImmutableCard?>? {
        return when {
            cardList.isEmpty() -> {
                null
            }

            cardList.size == amount -> {
                cardList
            }

            else -> {
                var result = listOf<ImmutableCard?>()
                if (passedCards <= cardList.size) {
                    if (restCard >= amount) {
                        result = cardList.slice(passedCards..passedCards.plus(amount).minus(1))
                        passedCards += amount
                    } else {
                        result = cardList.slice(passedCards..cardList.size.minus(1)).toMutableList()
                        passedCards += cardList.size.plus(50)
                    }

                }
                restCard = cardList.size - passedCards
                result
            }
        }
    }

    private fun onUserAnswered(isKnown: Boolean, cardId: String) {
        val cardWithPosition = getLocalCardWithPositionById(cardId)
        val card = cardWithPosition.card
        val cardPosition = cardWithPosition.position
        val newCard = spaceRepetitionHelper.rescheduleCard(card, isKnown)
        updateCard(newCard, cardPosition)
    }

    fun updateCard(card: ImmutableCard, position: Int) = viewModelScope.launch {
        cardList[position] = card
        repository.updateCardWithContentAndDefinition(card)
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

class MultiChoiceQuizGameViewModelFactory(
    private val repository: FlashCardRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MultiChoiceQuizGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MultiChoiceQuizGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }

}