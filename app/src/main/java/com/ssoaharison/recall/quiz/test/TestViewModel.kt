package com.ssoaharison.recall.quiz.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import com.ssoaharison.recall.util.CardType.MULTIPLE_ANSWER_CARD
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
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


class TestViewModel(
    private val repository: FlashCardRepository
) : ViewModel() {

    var deck: ImmutableDeck? = null

    private lateinit var originalCards: List<ImmutableCard?>
    private lateinit var localCards: List<TestCardModel>
    private var localTestCards: Flow<List<TestCardModel>> = flow {
        emit(localCards)
    }
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()


    private var _testCards = MutableStateFlow<UiState<List<TestCardModel>>>(UiState.Loading)
    val testCards: StateFlow<UiState<List<TestCardModel>>> = _testCards.asStateFlow()
    private var testCardJob: Job? = null

    fun getTestCards() {
        testCardJob?.cancel()
        testCardJob = viewModelScope.launch {
            try {
                localTestCards.collect {
                    if (it.isEmpty()) {
                        _testCards.value = UiState.Error("No Card")
                    } else {
                        _testCards.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _testCards.value = UiState.Error(e.toString())
            }
        }
    }

    fun setCardAsActualOrPassedByPosition(position: Int) {
        localCards[position].setAsActualOrPassed()
    }

    fun setCardAsNotActualOrNotPassedByPosition(position: Int) {
        localCards[position].setAsNotActualOrNotPassed()
    }

    fun initLocalCards(cards: List<ImmutableCard?>) {
        localCards = cards.map { card ->
            val cardDefinitions = if (card?.cardType == SINGLE_ANSWER_CARD) {
                getCardDefinitions(card.cardType, card.cardDefinition!!, card.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage, 4, card.cardId)
            } else {
                getCardDefinitions(card?.cardType!!, card.cardDefinition!!, card.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage, card.cardDefinition.size, card.cardId)
            }
            TestCardModel(
                cardId = card.cardId,
                cardContent = card.cardContent!!,
                cardContentLanguage = card.cardContentLanguage ?: deck?.cardContentDefaultLanguage,
                cardType = card.cardType,
                cardDefinition = cardDefinitions,
                cardDefinitionLanguage = card.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage,
            )
        }
        localCards.first().setAsActualOrPassed()
    }

    fun onRetakeTest() {
        localCards.forEach { tc ->
            tc.cardDefinition.forEach { df ->
                df.isSelected = false
            }
            tc.setAsNotActualOrNotPassed()
        }
        localCards.first().setAsActualOrPassed()
    }

    fun initOriginalCards(cards: List<ImmutableCard?>) {
        originalCards = cards
    }

    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun noteSingleUserAnswer(answer: TestCardDefinitionModel) {
        if (answer.cardType == MULTIPLE_ANSWER_CARD) {
            noteUserAnswerOnMultipleAnswerCard(answer)
        } else {
            noteUserAnswerOnSingleAnswerCard(answer)
        }
    }

    private fun noteUserAnswerOnSingleAnswerCard(answer: TestCardDefinitionModel) {
        localCards.forEach {
            if (it.cardId == answer.attachedCardId) {
                it.cardDefinition.forEach { def ->
                    if (def.definitionId == answer.definitionId) {
                        def.isSelected = answer.isSelected
                    } else {
                        def.isSelected = false
                    }
                }
            }
        }
    }

    private fun noteUserAnswerOnMultipleAnswerCard(answer: TestCardDefinitionModel) {
        localCards.forEach {
            if (it.cardId == answer.attachedCardId) {
                it.cardDefinition.forEach { def ->
                    if (def.definitionId == answer.definitionId) {
                        def.isSelected = answer.isSelected
                    }
                }
            }
        }
    }

    private fun getCardDefinitions(
        cardType: String,
        definitions: List<CardDefinition>,
        cardDefinitionLanguage: String?,
        requiredAlternativeSum: Int,
        cardId: String,
    ): List<TestCardDefinitionModel> {

//        val localDefinitions = getAnswerAlternatives(originalCards, definitions, requiredAlternativeSum)

        val temporaryTestCardDefinitionModel = definitions.map {
            TestCardDefinitionModel(
                definitionId = it.definitionId,
                attachedCardId = cardId,
                cardId = it.cardId,
                definition = TextWithLanguageModel(it.cardId, it.definition, DEFINITION, cardDefinitionLanguage),
                cardType = cardType,
                isCorrect = it.isCorrectDefinition,
                isSelected = false
            )
        }.toMutableList()

        val temporaryDefinitionList = definitions.toMutableList()

        while (temporaryTestCardDefinitionModel.size < requiredAlternativeSum) {
            val randomCard = originalCards.random()
            val randomDefinition = randomCard?.cardDefinition?.random()
            if (randomDefinition !in temporaryDefinitionList) {
                if (randomDefinition != null) {
                    val randomCardDefinitionLanguage = randomCard.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage
                    temporaryTestCardDefinitionModel.add(
                        TestCardDefinitionModel(
                            definitionId = randomDefinition.definitionId,
                            attachedCardId = cardId,
                            cardId = randomCard.cardId,
                            definition = TextWithLanguageModel(randomDefinition.cardId, randomDefinition.definition, DEFINITION, randomCardDefinitionLanguage),
                            cardType = cardType,
                            isCorrect = randomDefinition.isCorrectDefinition,
                            isSelected = false
                        )
                    )
                    temporaryDefinitionList.add(randomDefinition)
                }
            }
        }
        return temporaryTestCardDefinitionModel.shuffled()
    }

    private fun getAnswerAlternatives(
        cards: List<ImmutableCard?>,
        cardAlternative: List<CardDefinition>,
        requiredAlternativeSum: Int,
    ): List<CardDefinition> {
        val temporaryList: MutableList<CardDefinition> = cardAlternative.toMutableList()
        while (temporaryList.size < requiredAlternativeSum) {
            val randomDefinition = cards.random()?.cardDefinition?.random()

            if (randomDefinition !in temporaryList) {
                if (randomDefinition != null) {
                    temporaryList.add(randomDefinition)
                }
            }
        }
        return temporaryList.shuffled()
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

    fun submitResult(card: TestCardModel, isKnown: Boolean) {
        originalCards.forEach { c ->
            if (c?.cardId == card.cardId) {
                upOrDowngradeCard(isKnown, c)
            }
        }
    }

    private fun upOrDowngradeCard(isKnown: Boolean, card: ImmutableCard?) {
        if (card != null) {
            val newCard = spaceRepetitionHelper.rescheduleCard(card, isKnown)
            updateCard(newCard)
        }
    }

    fun updateCard(card: ImmutableCard) = viewModelScope.launch {
        repository.updateCard(card)
    }

    fun updateCardContentLanguage(cardId: String, language: String) = viewModelScope.launch {
        repository.updateCardContentLanguage(cardId, language)
    }

    fun updateCardDefinitionLanguage(cardId: String, language: String) = viewModelScope.launch {
        repository.updateCardDefinitionLanguage(cardId, language)
    }

}

fun Long.formatTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val remainingSeconds = this % 60
    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}

class TestViewModelFactory(
    private val repository: FlashCardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}