package com.example.flashcard.quiz.matchQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.util.MatchQuizGameBorderSize
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchQuizGameViewModel: ViewModel() {

    var fetchJob: Job? = null
    private val _actualCards = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val actualCards: StateFlow<UiState<List<String>>> = _actualCards.asStateFlow()
    private lateinit var cardList: List<ImmutableCard>
    lateinit var deck: ImmutableDeck
    private lateinit var originalCardList: List<ImmutableCard>
    private var passedCards: Int = 0

    fun initCardList(gameCards: List<ImmutableCard>) {
        cardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }
    fun initOriginalCardList(gameCards: List<ImmutableCard>) {
        originalCardList = gameCards
    }

    fun updateBoard() {
        if (passedCards == cardList.size) {
            _actualCards.value =UiState.Error("Quiz Complete")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                val cards = getCards(cardList, MatchQuizGameBorderSize.DEFAULT.getHeight())
                if (cards.isNullOrEmpty()) {
                    _actualCards.value =UiState.Error("Too Few Cards")
                } else {
                    _actualCards.value = UiState.Success(getChoices(cards))
                }
            }
        }
    }


    private fun getChoices(gameCards: List<ImmutableCard>): List<String> {
        val result = mutableListOf<String>()
        for (card in gameCards) {
            result.add(card.cardContent!!)
            result.add(card.cardDefinition!!)
        }

        return result.shuffled().toList()
    }

    fun getCards(quizCardList: List<ImmutableCard>, borderHeight: Int): List<ImmutableCard>? {
        if (quizCardList.size == borderHeight) {
            return quizCardList
        } else if (quizCardList.size < borderHeight) {
            return null
        } else if (quizCardList.size > borderHeight) {
            if (passedCards <= quizCardList.size) {
                val restCard = quizCardList.size - passedCards
                return if (restCard > borderHeight) {
                    quizCardList.slice(passedCards..borderHeight)
                } else {
                    val result = quizCardList.slice(passedCards..quizCardList.size.minus(1)).toMutableList()
                    while (result.size != borderHeight) {
                        val randomCard = quizCardList.random()
                        if (randomCard !in result) {
                            result.add(randomCard)
                        }
                    }
                    result
                }
            }
        }
        return null
    }

}