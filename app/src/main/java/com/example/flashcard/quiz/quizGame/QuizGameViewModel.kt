package com.example.flashcard.quiz.quizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.CardType.SINGLE_ANSWER_CARD
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
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

    fun getDeckColorCode() = deck?.deckColorCode ?: "black"

    fun getMissedCard(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    fun getOriginalCardList() = originalCardList

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
                _externalQuizGameCards.value = UiState.Error(e.toString())
            }
        }
    }

    fun initLocalQuizGameCards(cards: List<ImmutableCard?>) {
        localQuizGameCards = cards.map { card ->
            val externalDefinitions = toQuizGameCardDefinitionModel(card?.cardId!!, card.cardType!!, card.cardDefinition!!).shuffled()
            QuizGameCardModel(
                card.cardId,
                card.cardContent,
                externalDefinitions,
                card.cardType,
                card.cardStatus
            )
        }.toMutableList()
    }

    fun getQuizGameCardsSum() = localQuizGameCards.size

    fun getMissedCardSum() = missedCards.size

    fun getKnownCardSum() = getQuizGameCardsSum() - getMissedCardSum()

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
        localQuizGameCards.sortBy { it.cardStatus }
    }

    fun shuffleCards() {
        localQuizGameCards.shuffle()
    }

    fun sortByCreationDate() {
        localQuizGameCards.sortBy { it.cardId }
    }

    fun cardToReviseOnly() {
        val carToRevise = originalCardList?.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
        initLocalQuizGameCards(carToRevise.toList())
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

    fun updateCardOnKnownOrKnownNot(
        answer: QuizGameCardDefinitionModel,
        knownOrNot: Boolean
    ) {
        originalCardList?.forEach {
            if (it?.cardId == answer.cardId) {
                upOrDowngradeCard(knownOrNot, it)
                if (!knownOrNot) {
                    missedCards.add(it)
                }
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