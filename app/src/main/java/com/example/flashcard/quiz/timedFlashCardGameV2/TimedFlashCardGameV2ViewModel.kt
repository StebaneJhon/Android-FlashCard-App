package com.example.flashcard.quiz.timedFlashCardGameV2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimedFlashCardGameV2ViewModel(
): ViewModel() {

    private var fetchJob: Job? = null

    private var currentCardPosition: Int = 0
    private var knownCardsSum: Int = 0
    private val unKnownCards: ArrayList<ImmutableCard> = arrayListOf()

    private val _actualCards = MutableStateFlow<UiState<TimedFlashCardGameModel>>(UiState.Loading)
    val actualCards: StateFlow<UiState<TimedFlashCardGameModel>> = _actualCards.asStateFlow()

    private var cardList: List<ImmutableCard>? = null
    var deck: ImmutableDeck? = null

    fun initCardList(gameCards: List<ImmutableCard>) {
        cardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    private val topCard
        get() = cardList?.get(currentCardPosition % cardList!!.size)
    private val bottomCard
        get() = cardList?.get((currentCardPosition + 1) % cardList!!.size)

    fun swipe() {
        currentCardPosition += 1
        updateCards()
    }

    fun updateCards() {
        cardList?.let {cards ->
                fetchJob?.cancel()
                _actualCards.value = UiState.Loading
                fetchJob = viewModelScope.launch {
                    _actualCards.value = UiState.Success(TimedFlashCardGameModel(
                        top = topCard!!,
                        bottom = bottomCard!!
                    ))
                }
        }
    }

}