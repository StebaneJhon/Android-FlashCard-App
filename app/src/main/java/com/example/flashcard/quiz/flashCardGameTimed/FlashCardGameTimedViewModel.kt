package com.example.flashcard.quiz.flashCardGameTimed

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.CardLevel
import com.example.flashcard.util.FlashCardTimedTimerStatus
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashCardGameTimedViewModel(
    private val repository: FlashCardRepository
): ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    private val missedCards: ArrayList<ImmutableCard> = arrayListOf()
    private val _actualCards = MutableStateFlow<UiState<FlashCardGameTimedModel>>(UiState.Loading)
    val actualCards: StateFlow<UiState<FlashCardGameTimedModel>> = _actualCards.asStateFlow()
    private var originalCardList: List<ImmutableCard>? = null
    private var cardList: MutableList<ImmutableCard>? = null
    var deck: ImmutableDeck? = null
    var progress: Int = 0

    private lateinit var timer: CountDownTimer
    private val _seconds = MutableStateFlow<UiState<String>>(UiState.Loading)
    val seconds: StateFlow<UiState<String>> = _seconds.asStateFlow()

    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()


    fun startTimer(seconds: Long) {
        _seconds.value = UiState.Success((seconds/1000).toString())
        timer = object : CountDownTimer(seconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val timeLeft = (millisUntilFinished/1000).plus(1)
                _seconds.value = UiState.Success(timeLeft.toInt().toString())
            }

            override fun onFinish() {
                _seconds.value = UiState.Success(FlashCardTimedTimerStatus.TIMER_FINISHED)
            }

        }.start()
    }

    fun stopTimer() {
        timer.cancel()
    }

    fun initCardList(gameCards: MutableList<ImmutableCard>) {
        cardList = gameCards
        originalCardList = gameCards
    }

    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    private val topCard
        get() = cardList?.get(currentCardPosition)
    private val bottomCard
        get() = getBottomCard(cardList!!, currentCardPosition)

    fun swipe(isKnown: Boolean): Boolean {
        val isQuizComplete = currentCardPosition == cardList?.size?.minus(1)
        if (!isKnown) {
            missedCards.add(cardList!![currentCardPosition])
        } else {
            progress += 100/getTotalCards()
        }
        onCardSwiped(isKnown)
        if (!isQuizComplete) {
            currentCardPosition += 1
            updateOnScreenCards()
        }
        return isQuizComplete
    }

    fun rewind() {
        currentCardPosition -= 1
        cardList?.get(currentCardPosition)?.let {
            if (it in missedCards) {
                missedCards.remove(it)
            } else {
                progress -= 100/getTotalCards()
            }
        }
        updateOnScreenCards()
    }

    fun getKnownCardSum(): Int {
        cardList?.let { return it.size - missedCards.size }
        return 0
    }

    fun getTotalCards(): Int {
        cardList?.let { return it.size }
        return 0
    }

    fun getCardBackground() = deck?.deckColorCode

    fun getCurrentCardNumber() = currentCardPosition.plus(1)

    fun getMissedCardSum() = missedCards.size

    fun getMissedCards(): MutableList<ImmutableCard> {
        val newCards = arrayListOf<ImmutableCard>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }

    private fun getBottomCard(
        cards: MutableList<ImmutableCard>,
        currentCartPosition: Int
    ): ImmutableCard? {
        return if (currentCartPosition > cards.size-2) {
            null
        } else {
            cards[currentCartPosition + 1]
        }
    }

    fun initFlashCard() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
    }

    fun sortCardsByLevel() {
        cardList?.sortBy { it.cardStatus }
    }

    fun shuffleCards() {
        cardList?.shuffle()
    }

    fun sortByCreationDate() {
        cardList?.sortBy { it.cardId }
    }

    fun unknownCardsOnly() {
        cardList = cardList?.filter { it.cardStatus == CardLevel.L1 } as MutableList<ImmutableCard>
    }

    fun restoreCardList() {
        cardList = originalCardList?.toMutableList()
    }

    fun updateOnScreenCards() {
        cardList?.let {cards ->
            if (cards.size == 0) {
                _actualCards.value = UiState.Error("No Cards To Revise")
            } else {
                fetchJob?.cancel()
                _actualCards.value = UiState.Loading
                fetchJob = viewModelScope.launch {
                    _actualCards.value = UiState.Success(
                        FlashCardGameTimedModel(
                            top = topCard!!,
                            bottom = bottomCard
                        )
                    )
                }
            }
        }
    }

    private fun onCardSwiped(isKnown: Boolean) {
        cardList?.let {cards ->
            val card = cards[currentCardPosition]
            val newStatus = spaceRepetitionHelper.status(card, isKnown)
            val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, isKnown, newStatus)
            val lastRevision = spaceRepetitionHelper.today()
            val newCard = Card(
                card.cardId,
                card.cardContent,
                card.contentDescription,
                card.cardDefinition,
                card.valueDefinition,
                card.deckId,
                card.backgroundImg,
                card.isFavorite,
                card.revisionTime,
                card.missedTime,
                card.creationDate,
                lastRevision,
                newStatus,
                nextRevision
            )
            updateCard(newCard)
        }
    }

    fun updateCard(card: Card) = viewModelScope.launch {
        repository.updateCard(card)
    }


}

class FlashCardGameTimedViewModelFactory(private val repository: FlashCardRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashCardGameTimedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashCardGameTimedViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }

}