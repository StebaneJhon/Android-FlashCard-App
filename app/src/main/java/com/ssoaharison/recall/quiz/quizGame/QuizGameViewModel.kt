package com.ssoaharison.recall.quiz.quizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.Model.ImmutableCard
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.util.CardType.MULTIPLE_ANSWER_CARD
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import com.ssoaharison.recall.util.SpaceRepetitionAlgorithmHelper
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
    private var attemptTime = 0
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

    fun getDeckColorCode() = deck?.deckColorCode ?: "black"

    fun getMissedCard(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    fun getOriginalCardList() = originalCardList

    fun increaseAttemptTime() {
        attemptTime += 1
    }

    fun initAttemptTime() {
        attemptTime = 0
    }

    fun getAttemptTime() = attemptTime

    private lateinit var localQuizGameCards: MutableList<QuizGameCardModel>
    private var flowOfLocalQuizGameCards: Flow<List<QuizGameCardModel>> = flow {
        emit(localQuizGameCards)
    }
    private var _externalQuizGameCards = MutableStateFlow<UiState<List<QuizGameCardModel>>>(UiState.Loading)
    val externalQuizGameCards: StateFlow<UiState<List<QuizGameCardModel>>> = _externalQuizGameCards.asStateFlow()
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

//    fun initLocalQuizGameCards(cards: List<ImmutableCard?>) {
//        localQuizGameCards = cards.map { card ->
//            val externalDefinitions = toQuizGameCardDefinitionModel(card?.cardId!!, card.cardType!!, card.cardDefinition!!).shuffled()
//            QuizGameCardModel(
//                card.cardId,
//                card.cardContent,
//                externalDefinitions,
//                card.cardType,
//                card.cardStatus
//            )
//        }.toMutableList()
//    }

    fun onRestartQuiz() {
//        initQuizGame()
        initPassedCards()
        initRestCards()
    }

    fun updateActualCards(amount: Int) {
        val cards = getCardsAmount(cardList, amount)
        localQuizGameCards = cards?.map { card ->
            val externalDefinitions = toQuizGameCardDefinitionModel(card?.cardId!!, card.cardType!!, card.cardDefinition!!).shuffled()
            QuizGameCardModel(
                card.cardId,
                card.cardContent,
                card.cardContentLanguage ?: deck?.cardContentDefaultLanguage!!,
                externalDefinitions,
                card.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage!!,
                card.cardType,
                card.cardStatus
            )
        }!!.toMutableList()
    }

    fun updateActualCardsWithMissedCards() {
        localQuizGameCards = missedCards.map { card ->
            val externalDefinitions = toQuizGameCardDefinitionModel(card?.cardId!!, card.cardType!!, card.cardDefinition!!).shuffled()
            QuizGameCardModel(
                card.cardId,
                card.cardContent,
                card.cardContentLanguage ?: deck?.cardContentDefaultLanguage!!,
                externalDefinitions,
                card.cardDefinitionLanguage ?: deck?.cardDefinitionDefaultLanguage!!,
                card.cardType,
                card.cardStatus
            )
        }.toMutableList()
        missedCards.clear()
    }

    fun getQuizGameCardsSum() = localQuizGameCards.size

    fun getMissedCardSum() = missedCards.size

    fun getKnownCardSum() = getQuizGameCardsSum() - getMissedCardSum()

    fun cardLeft() = restCard

    fun submitUserAnswer(answer: QuizGameCardDefinitionModel) {
        when (answer.cardType) {
            MULTIPLE_ANSWER_CARD -> { onSubmitMultiAnswerCardAnswer(answer) }
            SINGLE_ANSWER_CARD -> {onSubmitSingleAnswerCardAnswer(answer)}
            else -> { onSubmitMultiChoiceCardAnswer(answer) }
        }
    }

    private fun onSubmitSingleAnswerCardAnswer(answer: QuizGameCardDefinitionModel) {
        localQuizGameCards.forEach {
            if (it.cardId == answer.cardId) {
                it.cardDefinition.first().isSelected = answer.isSelected
                it.isFlipped = !it.isFlipped
            }
        }
    }

    private fun onSubmitMultiChoiceCardAnswer(answer: QuizGameCardDefinitionModel) {
        localQuizGameCards.forEach {
            if (it.cardId == answer.cardId) {
                it.cardDefinition.forEach { d ->
                    if (d.definitionId == answer.definitionId) {
                        d.isSelected = answer.isSelected
                    } else {
                        d.isSelected = false
                    }
                }
            }
        }
    }

    private fun onSubmitMultiAnswerCardAnswer(answer: QuizGameCardDefinitionModel) {
        localQuizGameCards.forEach {
            if (it.cardId == answer.cardId) {
                it.cardDefinition.forEach { d ->
                    if(d.definitionId == answer.definitionId) {
                        d.isSelected = answer.isSelected
                    }
                }
            }
        }
    }

    fun isAllAnswerSelected(answer: QuizGameCardDefinitionModel): Boolean {
        localQuizGameCards.forEach {
            if (it.cardId == answer.cardId) {
                it.cardDefinition.forEach { d ->
                    if (d.isCorrect == 1 && !d.isSelected) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun toQuizGameCardDefinitionModel(
        cardId: String,
        cardType: String,
        d: List<CardDefinition>
    ): List<QuizGameCardDefinitionModel> {
        return d.map {
            QuizGameCardDefinitionModel(
                it.definitionId,
                cardId,
                it.definition,
                cardType,
                it.isCorrectDefinition,
                false
            )
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
        cardList = cardList.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
    }

    fun isNextCardAnswered(actualCardPosition: Int): Boolean {
        val nextCardPosition = actualCardPosition.plus(1)
        if (nextCardPosition < localQuizGameCards.size) {
            val nextCard = localQuizGameCards[nextCardPosition]

            return isAllAnswerSelected(nextCard.cardDefinition.first())
        }
        return false
    }

    fun restoreCardList() {
        cardList = originalCardList!!.toMutableList()
    }

    fun initQuizGame() {
        missedCards.clear()
        localQuizGameCards.forEach {
            it.isFlipped = false
            it.cardDefinition.forEach { d ->
                d.isSelected = false
            }
        }
    }

    fun resetLocalQuizGameCardsState() {
        localQuizGameCards.forEach {
            it.isFlipped = false
            it.cardDefinition.forEach { d ->
                d.isSelected = false
            }
        }
    }

    fun updateCardOnKnownOrKnownNot(
        answer: QuizGameCardDefinitionModel,
        knownOrNot: Boolean?
    ) {
        originalCardList?.forEach {
            if (it?.cardId == answer.cardId) {
                if (knownOrNot != null) {
                    upOrDowngradeCard(knownOrNot, it)
                    if (!knownOrNot) {
                        missedCards.add(it)
                    }
                } else {
                    if (answer.isSelected && answer.isCorrect == 1) {
                        upOrDowngradeCard(true, it)
                    } else {
                        upOrDowngradeCard(false, it)
                        missedCards.add(it)
                    }
                }
            }
        }
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
                        result = quizCardList.slice(passedCards..quizCardList.size.minus(1)).toMutableList()
                        passedCards += quizCardList.size.plus(50)
                    }
                }
                restCard = quizCardList.size - passedCards
                result
            }
        }
    }

    fun upOrDowngradeCard(isKnown: Boolean, card: ImmutableCard?) {
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