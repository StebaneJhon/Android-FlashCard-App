package com.ssoaharison.recall.quiz.quizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.util.CardType.MULTIPLE_ANSWER_CARD
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
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

    lateinit var cardList: MutableList<ImmutableCard?>
    private var originalCardList: List<ImmutableCard?>? = null
    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
    var deck: ImmutableDeck? = null

    //    private var attemptTime = 0
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
    private var passedCards: Int = 0
    private var restCard = 0

    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
    }

    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
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

    fun getDeckColorCode() = deck?.deckColorCode ?: "black"


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

    fun updateActualCards(amount: Int) {
        val cards = getCardsAmount(cardList, amount)
        localQuizGameCards = cards?.map { card ->
            localCardToQuizGameCardModel(card)
        }!!.toMutableList()
    }

    fun updateActualCardsWithMissedCards() {
        localQuizGameCards = missedCards.map { card ->
            localCardToQuizGameCardModel(card)
        }.toMutableList()
        missedCards.clear()
    }

    private fun localCardToQuizGameCardModel(card: ImmutableCard?): QuizGameCardModel {
        val externalDefinitions = toQuizGameCardDefinitionModel(
            card?.cardId!!,
            card.cardType!!,
            card.cardDefinition!!
        )
        return QuizGameCardModel(
            card.cardId,
            card.cardContent,
            card.cardContentLanguage ?: deck?.cardContentDefaultLanguage,
            externalDefinitions,
            card.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage,
            card.cardType,
            card.cardStatus
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
        d: List<CardDefinition>
    ): List<QuizGameCardDefinitionModel> {
        val formatedDefinitions = d.map {
            QuizGameCardDefinitionModel(
                definitionId = it.definitionId,
                cardId = cardId,
                definition = it.definition,
                cardType = cardType,
                isCorrect = it.isCorrectDefinition,
                isSelected = false
            )
        }.shuffled()
        return formatedDefinitions.mapIndexed { index, item ->
            item.position = index
            item
        }

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

    fun cardToReviseOnly() {
        cardList =
            cardList.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
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
            missedCards.add(getLocalCardById(actualCard.cardId))
            val currentCard = localCardToQuizGameCardModel(getLocalCardById(answer.cardId))
            localQuizGameCards.add(currentCard)
        }
    }

    fun updateSingleAnsweredCardOnKnownOrKnownNot(
        knownOrNot: Boolean,
        cardPosition: Int,
    ) {
        val actualCard = localQuizGameCards[cardPosition]
        upOrDowngradeCard(knownOrNot, actualCard.cardId)
        if (!knownOrNot) {
            missedCards.add(getLocalCardById(actualCard.cardId))
            val currentCard = localCardToQuizGameCardModel(getLocalCardById(actualCard.cardId))
            localQuizGameCards.add(currentCard)
        } else {
            actualCard.isCorrectlyAnswered = true
        }
        initCardFlipCount(cardPosition)
    }

    private fun getLocalCardById(cardId: String): ImmutableCard? {
        var i = 0
        while (i < cardList.size) {
            val c = cardList[i]
            if (c?.cardId == cardId) {
                return c
            }
            i++
        }
        return null
    }

    private fun getCardsAmount(
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

    private fun upOrDowngradeCard(isKnown: Boolean, cardId: String) {
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
                card.cardContentLanguage,
                card.cardDefinitionLanguage
            )
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