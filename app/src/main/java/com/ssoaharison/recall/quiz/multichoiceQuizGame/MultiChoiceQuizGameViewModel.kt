package com.ssoaharison.recall.quiz.multichoiceQuizGame

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

class MultiChoiceQuizGameViewModel(
    private val repository: FlashCardRepository
): ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    var progress: Int = 0
    var attemptTime: Int = 0
    private val missedCards: ArrayList<ImmutableCard?> = arrayListOf()
    private val _actualCards = MutableStateFlow<UiState<List<MultiChoiceGameCardModel>>>(UiState.Loading)
    val actualCards: StateFlow<UiState<List<MultiChoiceGameCardModel>>> = _actualCards.asStateFlow()
    private lateinit var cardList: MutableList<ImmutableCard?>
    lateinit var deck: ImmutableDeck
    private lateinit var originalCardList: List<ImmutableCard?>
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
    private var passedCards: Int = 0
    private var restCard = 0

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

    fun initMissedCards() {
        missedCards.clear()
    }

    fun onRestartQuiz() {
        initTimedFlashCard()
        initPassedCards()
        initRestCards()
    }

    fun getOriginalCardList() = originalCardList

    private fun getWordAlternatives(
        cards: List<ImmutableCard?>,
        correctAlternative: String,
        sum: Int,
        cardOrientation: String
    ): List<String> {
        val temporaryList = arrayListOf<String>()
        temporaryList.add(correctAlternative)
        while (temporaryList.size < sum) {
            val randomWordTranslation = if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
                cards.random()?.cardDefinition?.random()?.definition
            } else {
                cards.random()?.cardContent?.content
            }
            if (randomWordTranslation !in temporaryList) {
                if (randomWordTranslation != null) {
                    temporaryList.add(randomWordTranslation)
                }
            }
        }
        return temporaryList.shuffled()
    }

    private fun getRandomCorrectDefinition(definitions: List<CardDefinition>?): String? {
        val correctDefinitions = definitions?.let {defins -> defins.filter { isCorrect(it.isCorrectDefinition) }}
        return correctDefinitions?.random()?.definition
    }

    private fun getCorrectDefinitions(definitions: List<CardDefinition>?): List<String>? {
        val correctDefinitions = definitions?.let {defins -> defins.filter { isCorrect(it.isCorrectDefinition) }}
        val correctAlternative = mutableListOf<String>()
        correctDefinitions?.forEach {
            correctAlternative.add(it.definition)
        }
        return  correctAlternative
    }

    fun isCorrect(index: Int?) = index == 1
    fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

    fun increaseAttemptTime() {
        attemptTime += 1
    }

    fun swipe(cardCount: Int): Boolean {
        if (attemptTime == 0) {
            progress += 100/cardSum()
        } else {
            attemptTime = 0
        }
        currentCardPosition += 1
        return currentCardPosition != cardCount
    }
    fun getCurrentCardPosition() = currentCardPosition
    fun cardSum() = cardList.size
    fun getMissedCardSum() = missedCards.size
    fun getKnownCardSum(cardCount: Int) = cardCount - getMissedCardSum()
    fun getMissedCard(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    fun cardLeft() = restCard

    fun initTimedFlashCard() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
    }

    fun onCardMissed(cardId: String) {
        val missedCard = getLocalCardById(cardId)
        if (missedCard !in missedCards) {
            missedCards.add(missedCard)
        }
        increaseAttemptTime()
    }

    private fun getLocalCardById(cardId: String): ImmutableCard? {
        var i = 0
        while (true) {
            val c = cardList[i]
            if (c?.cardId == cardId) {
                return c
            }
            i++
        }
        return null
    }

    fun isUserChoiceCorrect(userChoice: String, correctChoice: List<String>, cardId: String): Boolean {
        val isCorrect = userChoice in correctChoice
        if (!isCorrect) {
            onCardMissed(cardId)
            onUserAnswered(isCorrect, cardId)
        }
        if (attemptTime == 0 && isCorrect) {
            onUserAnswered(isCorrect, cardId)
        }
        return isCorrect
    }

    private fun toListOfMultiChoiceQuizGameCardModel(cards: List<ImmutableCard?>, cardOrientation: String): List<MultiChoiceGameCardModel> {
        val temporaryList = mutableListOf<MultiChoiceGameCardModel>()
        cards.forEach {
            val correctAlternative = getRandomCorrectDefinition(it?.cardDefinition)!!
            val correctAlternatives = getCorrectDefinitions(it?.cardDefinition)!!
            if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
                val alternatives = getWordAlternatives(originalCardList, correctAlternative, 4, cardOrientation)
                temporaryList.add(
                    MultiChoiceGameCardModel(
                        it?.cardId!!,
                        it.cardContent?.content!!,
                        correctAlternatives,
                        alternatives[0],
                        alternatives[1],
                        alternatives[2],
                        alternatives[3],
                    )
                )
            } else {
                val alternatives = getWordAlternatives(originalCardList, it?.cardContent?.content!!, 4, cardOrientation)
                temporaryList.add(
                    MultiChoiceGameCardModel(
                        it.cardId,
                        correctAlternative,
                        listOf(it.cardContent.content),
                        alternatives[0],
                        alternatives[1],
                        alternatives[2],
                        alternatives[3],
                    )
                )
            }

        }
        return temporaryList
    }

    fun updateCard(cardOrientation: String, cardCount: Int) {
        if (cardList.size == 0) {
            _actualCards.value = UiState.Error("No Cards To Revise")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                val cards = getCardsAmount(cardCount)
                if (cards.isNullOrEmpty()) {
                    _actualCards.value = UiState.Error("No Cards To Revise")
                } else {
                    _actualCards.value = UiState.Success(toListOfMultiChoiceQuizGameCardModel(cards, cardOrientation))
                }
            }
        }
    }

    fun updateCardOnReviseMissedCards(cardOrientation: String) {
        if (missedCards.isEmpty()) {
            _actualCards.value = UiState.Error("No Cards To Revise")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _actualCards.value = UiState.Success(toListOfMultiChoiceQuizGameCardModel(missedCards, cardOrientation))
                missedCards.clear()
            }
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

    fun unknownCardsOnly() {
        cardList = cardList.filter { it?.cardStatus == CardLevel.L1 } as MutableList<ImmutableCard?>
    }

    fun cardToReviseOnly() {
        cardList = cardList.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
    }

    fun restoreCardList() {
        cardList = originalCardList.toMutableList()
    }

    private fun getCardsAmount( amount: Int ): List<ImmutableCard?>? {
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
            val card = getLocalCardById(cardId)
            if (card != null) {
                val newStatus = spaceRepetitionHelper.status(card, isKnown)
                val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, isKnown, newStatus)
                val lastRevision = spaceRepetitionHelper.today()
                val nextForgettingDate = spaceRepetitionHelper.nextForgettingDate(card, isKnown, newStatus)
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

class MultiChoiceQuizGameViewModelFactory(
    private val repository: FlashCardRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MultiChoiceQuizGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MultiChoiceQuizGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }

}