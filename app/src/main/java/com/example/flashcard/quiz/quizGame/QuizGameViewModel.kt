package com.example.flashcard.quiz.quizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.CardLevel
import com.example.flashcard.util.Constant
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TestQuizGameViewModel(
    private val repository: FlashCardRepository
) : ViewModel() {

    lateinit var cardList: MutableList<ImmutableCard?>
    private var originalCardList: List<ImmutableCard?>? = null
    private lateinit var modelCardList: MutableList<ModelCard?>
    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
    var deck: ImmutableDeck? = null
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()


    private var currentCardPosition: Int = 0
    //var progress: Int = 0

    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
        originalCardList = gameCards
    }

    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
        originalCardList = gameCards
    }

    fun initModelCardList(gameCards: MutableList<ImmutableCard?>) {
        modelCardList = gameCards.map { ModelCard(it) }.toMutableList()
    }

    fun getDeckColorCode() = deck?.deckColorCode ?: "black"

    fun getModelCardsNonStream() = modelCardList

    fun getModelCardsSum() = modelCardList.size

    fun getMissedCard(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    fun getOriginalCardList() = originalCardList

    fun getProgress() = getKnownCardSum() * 100 / getModelCardsSum()

    fun getMissedCardSum() = missedCards.size

    fun getKnownCardSum() = getModelCardsSum() - getMissedCardSum()


    private val _modelCards = MutableStateFlow<UiState<List<ModelCard?>>>(UiState.Loading)
    val modelCards: StateFlow<UiState<List<ModelCard?>>> = _modelCards.asStateFlow()
    private var fetchJob: Job? = null
    fun getCards() {
        if (cardList.size == 0) {
            _modelCards.value = UiState.Error("No Cards To Revise")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _modelCards.value = UiState.Success(modelCardList)
            }
        }
    }


    fun sortCardsByLevel() {
        modelCardList.sortBy { it?.cardDetails?.cardStatus }
    }

    fun shuffleCards() {
        modelCardList.shuffle()
    }

    fun sortByCreationDate() {
        modelCardList.sortBy { it?.cardDetails?.cardId }
    }

    fun unknownCardsOnly() {
        modelCardList = modelCardList.filter { it?.cardDetails?.cardStatus == CardLevel.L1 } as MutableList<ModelCard?>
    }

    fun cardToReviseOnly() {
        modelCardList = modelCardList.filter { spaceRepetitionHelper.isToBeRevised(it?.cardDetails!!) } as MutableList<ModelCard?>
    }

    fun onFlipCard(cardPosition: Int) {
        val cardToFlip = modelCardList[cardPosition]
        cardToFlip?.isFlipped = !cardToFlip?.isFlipped!!
    }

    fun onCorrectAnswer(cardPosition: Int) {
        val card = modelCardList[cardPosition]
        card?.correctAnswerSum = card?.correctAnswerSum?.plus(1)!!
    }

    fun onNotCorrectAnswer(card: ImmutableCard?): String {
        if (card !in missedCards && card != null) {
            missedCards.add(card)

        } else {
            return Constant.FAILED
        }
        return Constant.SUCCEED
    }

    fun onDrag(cardPosition: Int) {
        val answeredCard = modelCardList[cardPosition]
        answeredCard?.isAnswered = true
    }

    fun isNextCardAnswered(actualCardPosition: Int): Boolean {
        val nextCardPosition = actualCardPosition.plus(1)
        if (nextCardPosition < modelCardList.size) {
            val nextCard = modelCardList[nextCardPosition]
            return nextCard?.isAnswered ?: false
        }
        return false
    }

    fun restoreCardList() {
        cardList = originalCardList!!.toMutableList()
    }

    fun initTest() {
        missedCards.clear()
        modelCardList.forEach { modelCard ->
            modelCard?.isFlipped = false
            modelCard?.isAnswered = false
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