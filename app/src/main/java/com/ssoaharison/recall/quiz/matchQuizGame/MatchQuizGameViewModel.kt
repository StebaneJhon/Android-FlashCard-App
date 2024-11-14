package com.ssoaharison.recall.quiz.matchQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.Model.ImmutableCard
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.backend.Model.MatchQuizGameItemModel
import com.ssoaharison.recall.util.MatchQuizGameClickStatus
import com.ssoaharison.recall.util.UiState
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
    private lateinit var originalCardList: List<ImmutableCard?>
    private var passedCards: Int = 0
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

    fun initQuiz() {
        passedCards = 0
        restCard = cardSum()
    }

    fun getNumMove() = numMove
    fun getNumMiss() = numMiss

    fun getUserPerformance() = (100/numMove) * numMiss

    fun getOnBoardCardSum(boardSize: MatchQuizGameBorderSize) = boardSize.getCardCount()


    fun updateBoardCards(boardSize: MatchQuizGameBorderSize ) {
        if (passedCards == cardList.size) {
            _actualCards.value = UiState.Error("Quiz Complete")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                val cards = getCards(cardList, boardSize.getCardCount())
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
        return true
    }

    fun isQuizComplete(boardSize: MatchQuizGameBorderSize) = numMatch == boardSize.getCardCount()

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
            val item2 = MatchQuizGameItemModel(card.cardDefinition.first().definition, card.cardContent.content)
            return listOf(item1, item2)
        }
        return null
    }

    private fun getCards(quizCardList: List<ImmutableCard?>, borderHeight: Int): List<ImmutableCard?>? {
        return when {
            quizCardList.size == borderHeight -> {
                quizCardList
            }

            quizCardList.size < borderHeight -> {
                val cardAmountForBoard2 = MatchQuizGameBorderSize.BOARD_2.getCardCount()
                val cardAmountForBoard1 = MatchQuizGameBorderSize.BOARD_1.getCardCount()
                when {
                    quizCardList.size >= cardAmountForBoard2 -> {
                        getCardAmount(quizCardList, cardAmountForBoard2)
                    }
                    quizCardList.size >= cardAmountForBoard1 -> {
                        getCardAmount(quizCardList, cardAmountForBoard1)
                    }
                    else -> {
                        null
                    }
                }
            }
            else -> {
                getCardAmount(quizCardList, borderHeight)
            }
        }
    }

    private fun getCardAmount(quizCardList: List<ImmutableCard?>, amount: Int): List<ImmutableCard?> {
        var result = listOf<ImmutableCard?>()
        if (passedCards <= quizCardList.size) {
            if (restCard > amount) {
                result = quizCardList.slice(passedCards..passedCards.plus(amount).minus(1))
                passedCards += amount
            } else {
                result = quizCardList.slice(passedCards..quizCardList.size.minus(1))
                    .toMutableList()
                while (result.size != amount) {
                    val randomCard = quizCardList.random()
                    if (randomCard !in result) {
                        result.add(randomCard)
                    }
                }
                passedCards += quizCardList.size.plus(50)
            }
        }
        restCard = quizCardList.size - passedCards
        return result
    }
}