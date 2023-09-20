package com.example.flashcard.quiz.multichoiceQuizGame

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

class MultiChoiceQuizGameViewModel: ViewModel() {

    private var fetchJob: Job? = null
    private var currentCardPosition: Int = 0
    private val missedCards: ArrayList<ImmutableCard> = arrayListOf()
    private val _actualCard = MutableStateFlow<UiState<MultiChoiceGameCardModel>>(UiState.Loading)
    val actualCard: StateFlow<UiState<MultiChoiceGameCardModel>> = _actualCard.asStateFlow()
    private lateinit var cardList: List<ImmutableCard>
    lateinit var deck: ImmutableDeck

    fun initCardList(gameCards: List<ImmutableCard>) {
        cardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    private val onCardWord
        get() = cardList[currentCardPosition].cardContent
    private val wordAlternatives
        get() = getWordAlternatives(cardList, cardList[currentCardPosition].cardDefinition!!, 3)

    private fun getWordAlternatives(
        cards: List<ImmutableCard>,
        onCardWordTranslation: String,
        sum: Int
    ): List<String> {
        val temporaryList = mutableSetOf<String>()
        temporaryList.add(onCardWordTranslation)
        while (temporaryList.size < sum) {
            val randomWordTranslation = cards.random().cardDefinition
            temporaryList.add(randomWordTranslation!!)
        }
        return temporaryList.toList()
    }

    fun swipe() {
        currentCardPosition += 1
        updateCard()
    }

    fun cardSum() = cardList.size

    fun updateCard() {
        if (currentCardPosition == cardList.size) {
            _actualCard.value = UiState.Error("Quiz Complete")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _actualCard.value = UiState.Success(MultiChoiceGameCardModel(
                    onCardWord = onCardWord!!,
                    alternative1 = wordAlternatives[0],
                    alternative2 = wordAlternatives[1],
                    alternative3 = wordAlternatives[2]
                ))
            }
        }
    }

}