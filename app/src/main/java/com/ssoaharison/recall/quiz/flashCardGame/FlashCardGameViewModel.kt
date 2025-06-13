package com.ssoaharison.recall.quiz.flashCardGame

import android.text.format.DateUtils.formatElapsedTime
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.helper.RoteLearningAlgorithmHelper
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.Calculations
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

class FlashCardGameViewModel(
    private val repository: FlashCardRepository
) : ViewModel() {

    private var fetchJob: Job? = null
    private var flashCardCardsFetchJob: Job? = null
    private var currentCardPosition: Int = 0

    private var originalCardList: List<ImmutableCard?>? = null
    private lateinit var cardList: MutableList<ImmutableCard?>

    private lateinit var cardToRevise: MutableList<FlashCardCardModel?>
    private var flowOfCardToRevise: Flow<List<FlashCardCardModel?>?> = flow {
        emit(cardToRevise)
    }
    private val missedCards: ArrayList<FlashCardCardModel?> = arrayListOf()

    var deck: ImmutableDeck? = null
    private var progress: Int = 0
    private var passedCards = 0
    private var restCards = 0
    private var missedCardsCount = 0
    private var revisedCardsCount = 0

    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()
    private val roteLearningHelper = RoteLearningAlgorithmHelper()


    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
        initRestCards()
    }

    fun initOriginalCardList(gameCards: MutableList<ImmutableCard?>) {
        originalCardList = gameCards
    }

    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun updateCardToRevise(amount: Int) {
        val cards = getCardAmount(amount)
        cardToRevise = immutableCardsToFlashCardCards(cards).toMutableList()
        cardToRevise.first()?.setAsActualOrPassed()
        revisedCardsCount = cardToRevise.size
    }

    private fun setCardAsActualOrPassedByPosition(position: Int) {
        cardToRevise[position]?.setAsActualOrPassed()
    }

    private fun setCardAsNotActualOrNotPassedByPosition(position: Int) {
        cardToRevise[position]?.setAsNotActualOrNotPassed()
    }

    private fun getCardAmount(amount: Int): List<ImmutableCard?>? {
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
                    if (restCards >= amount) {
                        result = cardList.slice(passedCards..passedCards.plus(amount).minus(1))
                            .toMutableList()
                        passedCards += amount
                    } else {
                        result = cardList.slice(passedCards..cardList.size.minus(1)).toMutableList()
                        passedCards += cardList.size.plus(50)
                    }
                }
                restCards = cardList.size - passedCards
                result
            }
        }
    }

//    fun updateCardOnReviseMissedCards() {
//        cardToRevise = missedCards.clone() as MutableList<ImmutableCard?>
//        revisedCardsCount = cardToRevise?.size ?: 0
//        missedCards.clear()
//        missedCardsCount = 0
//    }

    fun immutableCardsToFlashCardCards(cards: List<ImmutableCard?>?): List<FlashCardCardModel> {
        val newList = mutableListOf<FlashCardCardModel>()
        cards?.forEach { card ->
            newList.add(immutableCardToFlashCardCard(card!!))
        }
        return newList
    }

    fun immutableCardToFlashCardCard(card: ImmutableCard): FlashCardCardModel {
        return FlashCardCardModel(
            card = card,
            isActualOrPassed = false
        )
    }

    fun sortCardsByLevel() {
        cardList.sortBy { it?.cardStatus }
    }

    fun shuffleCards() {
        cardList.shuffle()
    }

    fun getCardLeft() = restCards

    fun sortByCreationDate() {
        cardList.sortBy { it?.cardId }
    }

    fun unknownCardsOnly() {
        cardList = cardList.filter { it?.cardStatus == L1 } as MutableList<ImmutableCard?>
    }

    fun cardToReviseOnly() {
        cardList =
            cardList.filter { spaceRepetitionHelper.isToBeRevised(it!!) } as MutableList<ImmutableCard?>
    }

    fun restoreCardList() {
        cardList = originalCardList?.toMutableList()!!
    }

    fun swipe(isKnown: Boolean): Boolean {
        if (!isKnown) {
            val actualCard = cardToRevise[currentCardPosition]
            missedCards.add(actualCard)
            missedCardsCount++
            val cardToRepeatNewPosition =
                roteLearningHelper.onRepeatCardPosition(cardToRevise, currentCardPosition)
            val cardToRepeat = actualCard?.copy(isActualOrPassed = false)
            cardToRevise.add(cardToRepeatNewPosition, cardToRepeat)
        }
        onCardSwiped(isKnown)
        val isQuizComplete = currentCardPosition == cardToRevise.size.minus(1)
        if (!isQuizComplete) {
            currentCardPosition += 1
            setCardAsActualOrPassedByPosition(currentCardPosition)
            updateOnScreenCards()
        }

        return isQuizComplete
    }

    fun rewind() {
        setCardAsNotActualOrNotPassedByPosition(currentCardPosition)
        currentCardPosition -= 1
        val currentCard = cardToRevise[currentCardPosition]
        if (currentCard in missedCards) {
            missedCards.remove(currentCard)
            missedCardsCount--
        } else {
            progress -= 100 / getTotalCards()
        }
        updateOnScreenCards()
    }

    fun getKnownCardSum() = cardToRevise.size - missedCardsCount

    fun getTotalCards() = cardToRevise.size

    fun getCardBackground() = deck?.deckColorCode

    fun getCurrentCardNumber() = currentCardPosition.plus(1)

    fun getMissedCardSum() = missedCardsCount

//    fun getMissedCards(): MutableList<ImmutableCard?> {
//        val newCards = arrayListOf<ImmutableCard?>()
//        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
//        return newCards
//    }

    fun getUserAnswerAccuracy() = Calculations().percentageOfRest(cardToRevise.size, missedCardsCount)

    fun getUserAnswerAccuracyFraction() = Calculations().fractionOfPart(cardToRevise.size, missedCardsCount)

    private fun getBottomCard(
        cards: List<FlashCardCardModel?>,
        currentCartPosition: Int
    ): ImmutableCard? {
        return if (currentCartPosition > cards.size - 2) {
            null
        } else {
            cards[currentCartPosition + 1]?.card
        }
    }

    private fun getTopCard(
        cards: List<FlashCardCardModel?>,
        currentCartPosition: Int
    ): ImmutableCard? {
        return cards[currentCartPosition]?.card
    }

    fun getRevisedCardsCount() = revisedCardsCount

    private fun getActualFlashCardGameModel(
        cards: List<FlashCardCardModel?>,
    ): FlashCardGameModel {
        return FlashCardGameModel(
            top = getTopCard(cards, currentCardPosition)!!,
            bottom = getBottomCard(cards, currentCardPosition)
        )
    }

    fun initFlashCard() {
        stopTimer()
        missedCards.clear()
        missedCardsCount = 0
        progress = 0
        currentCardPosition = 0
    }

    private fun initPassedCards() {
        passedCards = 0
    }

    private fun initRestCards() {
        restCards = cardList.size
    }

    fun onRestartQuizWithAllCards() {
        initFlashCard()
        initPassedCards()
        initRestCards()
    }

    fun initProgress() {
        progress = 0
    }

    fun initCurrentCardPosition() {
        currentCardPosition = 0
    }

    private val _actualCards = MutableStateFlow<UiState<FlashCardGameModel>>(UiState.Loading)
    val actualCards: StateFlow<UiState<FlashCardGameModel>> = _actualCards.asStateFlow()
    fun updateOnScreenCards() {
        _actualCards.value = UiState.Loading
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                flowOfCardToRevise.collect { cards ->
                    if (cards.isNullOrEmpty()) {
                        _actualCards.value = UiState.Error("No Cards To Revise")
                    } else {
                        _actualCards.value = UiState.Success(
                            getActualFlashCardGameModel(cards)
                        )
                    }
                }
            } catch (e: IOException) {
                _actualCards.value = UiState.Error(e.message.toString())
            }
        }
    }

    private val _flashCardCardsCardsToRevise =
        MutableStateFlow<UiState<List<FlashCardCardModel?>>>(UiState.Loading)
    val flashCardCardsCardsToRevise: StateFlow<UiState<List<FlashCardCardModel?>>> =
        _flashCardCardsCardsToRevise.asStateFlow()

    fun getFlashCardCards() {
        _flashCardCardsCardsToRevise.value = UiState.Loading
        flashCardCardsFetchJob?.cancel()
        flashCardCardsFetchJob = viewModelScope.launch {
            try {
                flowOfCardToRevise.collect { cards ->
                    if (cards.isNullOrEmpty()) {
                        _flashCardCardsCardsToRevise.value = UiState.Error("No Cards To Revise")
                    } else {
                        _flashCardCardsCardsToRevise.value = UiState.Success(
                            cards
                        )
                    }
                }
            } catch (e: IOException) {
                _flashCardCardsCardsToRevise.value = UiState.Error(e.message.toString())
            }
        }
    }

    private fun onCardSwiped(isKnown: Boolean) {
        val card = cardToRevise[currentCardPosition]
        if (card != null) {
            val newCard = spaceRepetitionHelper.rescheduleCard(card.card, isKnown)
            updateCard(newCard)
        }
    }

    fun updateCard(
        card: ImmutableCard,
    ) = viewModelScope.launch {
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

class FlashCardGameViewModelFactory(private val repository: FlashCardRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashCardGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashCardGameViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }

}