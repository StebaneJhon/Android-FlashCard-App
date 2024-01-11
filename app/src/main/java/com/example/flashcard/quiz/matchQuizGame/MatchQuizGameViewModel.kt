package com.example.flashcard.quiz.matchQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.MatchQuizGameItemModel
import com.example.flashcard.util.MatchQuizGameBorderSize
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchQuizGameViewModel : ViewModel() {

    var fetchJob: Job? = null
    private val _actualCards = MutableStateFlow<UiState<List<MatchQuizGameItemModel>>>(UiState.Loading)
    val actualCards: StateFlow<UiState<List<MatchQuizGameItemModel>>> = _actualCards.asStateFlow()
    private lateinit var cardList: List<ImmutableCard>
    lateinit var deck: ImmutableDeck
    private lateinit var originalCardList: List<ImmutableCard>
    private var passedCards: Int = 0
    var boardSize = MatchQuizGameBorderSize.DEFAULT
    private val onBoarItems = mutableListOf<MatchQuizGameItemModel>()
    private var firstSelectedItem: MatchQuizGameItemModel? = null

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
            _actualCards.value = UiState.Error("Quiz Complete")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                val cards = getCards(cardList, boardSize.getHeight())
                if (cards.isNullOrEmpty()) {
                    _actualCards.value = UiState.Error("Too Few Cards")
                } else {
                    getChoices(cardList)
                    _actualCards.value = UiState.Success(onBoarItems.shuffled())
                }
            }
        }
    }


    fun selectItem(item: MatchQuizGameItemModel): Boolean {
        var match = false
        if (firstSelectedItem == null) {
            restoreItems()
            firstSelectedItem = item
        } else {
            match = isMatching(firstSelectedItem!!, item)
            firstSelectedItem = null
        }
        activateItem(item)
        return match
    }

    private fun isMatching(item1: MatchQuizGameItemModel, item2: MatchQuizGameItemModel): Boolean {
        if (item1.text != item2.match) {
            return false
        }
        if (item2.text != item1.match) {
            return false
        }
        onBoarItems.forEach {
            if (it.text == item1.text) {
                it.isMatched = true
            }
            if (it.text == item2.text) {
                it.isMatched = true
            }
        }

        return true
    }

    private fun activateItem(item: MatchQuizGameItemModel) {
        onBoarItems.forEach {
            if (it.text == item.text) {
                it.isActive = !it.isActive
            }
        }
    }

    private fun restoreItems() {
        onBoarItems.forEach {
            if (!it.isMatched) {
                it.isActive = false
            }
        }
    }

    private fun getChoices(gameCards: List<ImmutableCard>) {
        if (onBoarItems.size < 10) {
            for (card in gameCards) {
                val items = toMatchQuizGameItem(card)
                items?.let {
                    onBoarItems.add(it[0])
                    onBoarItems.add(it[1])
                }

            }
        }
        onBoarItems
    }

    private fun toMatchQuizGameItem(card: ImmutableCard): List<MatchQuizGameItemModel>? {
        if (!card.cardContent.isNullOrEmpty() && !card.cardDefinition.isNullOrEmpty()) {
            val item1 = MatchQuizGameItemModel(card.cardContent, card.cardDefinition,)
            val item2 = MatchQuizGameItemModel(card.cardDefinition, card.cardContent,)
            return listOf(item1, item2)
        }
        return null
    }

    fun getCards(quizCardList: List<ImmutableCard>, borderHeight: Int): List<ImmutableCard>? {
        return when {
            quizCardList.size == borderHeight -> {
                quizCardList
            }

            quizCardList.size < borderHeight -> {
                null
            }

            quizCardList.size > borderHeight -> {
                var result = listOf<ImmutableCard>()
                if (passedCards <= quizCardList.size) {
                    val restCard = quizCardList.size - passedCards
                    if (restCard > borderHeight) {
                        result = quizCardList.slice(passedCards..borderHeight)
                    } else {
                        result = quizCardList.slice(passedCards..quizCardList.size.minus(1))
                            .toMutableList()
                        while (result.size != borderHeight) {
                            val randomCard = quizCardList.random()
                            if (randomCard !in result) {
                                result.add(randomCard)
                            }
                        }
                    }
                }
                result
            }

            else -> {
                null
            }
        }
    }
}