package com.example.flashcard.quiz.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.CardType.SINGLE_ANSWER_CARD
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
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

    fun initLocalCards(cards: List<ImmutableCard?>) {
        localCards = cards.map { card ->
            val cardDefinitions = if (card?.cardType == SINGLE_ANSWER_CARD) {
                getCardDefinitions(card.cardType, card.cardDefinition!!, 4, card.cardId)
            } else {
                getCardDefinitions(card?.cardType!!, card.cardDefinition!!, card.cardDefinition.size, card.cardId)
            }
            TestCardModel(
                cardId = card.cardId,
                cardContent = card.cardContent!!,
                cardType = card.cardType,
                cardDefinition = cardDefinitions
            )
        }
    }

    fun onRetakeTest() {
        localCards.forEach { tc ->
            tc.cardDefinition.forEach { df ->
                df.isSelected = false
            }
        }
    }

    fun initOriginalCards(cards: List<ImmutableCard?>) {
        originalCards = cards
    }

    fun noteSingleUserAnswer(answer: TestCardDefinitionModel) {
        if (answer.cardType == MULTIPLE_ANSWER_CARD) {
            moteUserAnswerOnMultipleAnswerCard(answer)
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

    private fun moteUserAnswerOnMultipleAnswerCard(answer: TestCardDefinitionModel) {
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
        requiredAlternativeSum: Int,
        cardId: String,
    ): List<TestCardDefinitionModel> {

        val localDefinitions =
            getAnswerAlternatives(originalCards, definitions, requiredAlternativeSum)
        return localDefinitions.map {
            TestCardDefinitionModel(
                definitionId = it.definitionId,
                attachedCardId = cardId,
                cardId = it.cardId,
                definition = it.definition,
                cardType = cardType,
                isCorrect = it.isCorrectDefinition,
                isSelected = false
            )
        }

    }

    private fun getAnswerAlternatives(
        cards: List<ImmutableCard?>,
        cardAlternative: List<CardDefinition>,
        requiredAlternativeSum: Int,
    ): List<CardDefinition> {
        val temporaryList: MutableList<CardDefinition> = cardAlternative.toMutableList()
//        temporaryList.add(cardAlternative)
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

    fun getOriginalCards() = originalCards


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

    fun updateCard(card: ImmutableCard) = viewModelScope.launch {
        repository.updateCard(card)
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