package com.example.flashcard.quiz.matchQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.MatchQuizGameItemModel
import com.example.flashcard.util.MatchQuizGameBorderSize
import com.example.flashcard.util.MatchQuizGameClickStatus
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchQuizGameViewModel : ViewModel() {

    var fetchJob: Job? = null
    private val _actualCards =
        MutableStateFlow<UiState<List<MatchQuizGameItemModel>>>(UiState.Loading)
    val actualCards: StateFlow<UiState<List<MatchQuizGameItemModel>>> = _actualCards.asStateFlow()
    private lateinit var cardList: List<ImmutableCard?>
    lateinit var deck: ImmutableDeck
    private var progression = 0
    private lateinit var originalCardList: List<ImmutableCard?>
    private var passedCards: Int = 0
    var boardSize = MatchQuizGameBorderSize.DEFAULT
    private var onBoarItems = mutableListOf<MatchQuizGameItemModel>()
    private var firstSelectedItem: MatchQuizGameItemModel? = null
    private var numMatch = 0
    private var restCard = 0
    private var numMove = 0
    private var numMiss = 0

    fun initCardList(gameCards: List<ImmutableCard?>) {
        cardList = gameCards
        restCard = cardSum()
    }

    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
        originalCardList = gameCards
    }

    fun cardLeft() = restCard

    fun initOnBoardItems(items: MutableList<MatchQuizGameItemModel>) {
        numMatch = 0
        numMiss = 0
        numMove = 0
        onBoarItems = items
        onBoarItems.forEach {
            it.isMatched = false
            it.isActive = false
        }
    }

    fun getNumMove() = numMove
    fun getNumMiss() = numMiss

    fun getUserPerformance() = (100/numMove) * numMiss

    fun getOnBoardCardSum() = boardSize.getHeight()


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
                    getChoices(cards)
                    _actualCards.value = UiState.Success(onBoarItems.shuffled())
                }
            }
        }
    }

    private fun cardSum() = cardList.size


    fun selectItem(item: MatchQuizGameItemModel): String {
        val result = if (firstSelectedItem == null) {
            restoreItems()
            firstSelectedItem = item
            MatchQuizGameClickStatus.FIRST_TRY
        } else {
            numMove ++
            if (isMatching(firstSelectedItem!!, item)) {
                firstSelectedItem = null
                MatchQuizGameClickStatus.MATCH
            } else {
                firstSelectedItem = null
                numMiss++
                MatchQuizGameClickStatus.MATCH_NOT
            }
        }
        activateItem(item)
        return result
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
        numMatch++
        updateProgression()
        return true
    }

    private fun updateProgression() {
        progression += 100/boardSize.getHeight()
    }

    fun getProgression() = progression

    fun isQuizComplete() = numMatch == boardSize.getNumCards()

    fun isItemActive(item: MatchQuizGameItemModel) = item.isActive

    private fun activateItem(item: MatchQuizGameItemModel) {
        onBoarItems.forEach {
            if (it == item) {
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

    private fun getChoices(gameCards: List<ImmutableCard?>) {
        onBoarItems = mutableListOf()
        for (card in gameCards) {
            val items = toMatchQuizGameItem(card)
            items?.let {
                onBoarItems.add(it[0])
                onBoarItems.add(it[1])
            }

        }
    }

    private fun toMatchQuizGameItem(card: ImmutableCard?): List<MatchQuizGameItemModel>? {
        if (!card?.cardContent?.content.isNullOrEmpty() && !card?.cardDefinition.isNullOrEmpty()) {
            val item1 = MatchQuizGameItemModel(card?.cardContent?.content!!, card.cardDefinition?.first()?.definition!!)
            val item2 = MatchQuizGameItemModel(card.cardDefinition.first().definition!!, card.cardContent.content)
            return listOf(item1, item2)
        }
        return null
    }

    fun getCards(quizCardList: List<ImmutableCard?>, borderHeight: Int): List<ImmutableCard?>? {
        return when {
            quizCardList.size == borderHeight -> {
                quizCardList
            }

            quizCardList.size < borderHeight -> {
                null
            }

            quizCardList.size > borderHeight -> {
                var result = listOf<ImmutableCard?>()
                if (passedCards <= quizCardList.size) {
                    if (restCard > borderHeight) {
                        result = quizCardList.slice(passedCards..borderHeight.minus(1))
                        passedCards += borderHeight
                    } else {
                        result = quizCardList.slice(passedCards..quizCardList.size.minus(1))
                            .toMutableList()
                        while (result.size != borderHeight) {
                            val randomCard = quizCardList.random()
                            if (randomCard !in result) {
                                result.add(randomCard)
                            }
                        }
                        passedCards += quizCardList.size.plus(50)
                    }
                }
                restCard = quizCardList.size - passedCards
                result
            }

            else -> {
                null
            }
        }
    }
}