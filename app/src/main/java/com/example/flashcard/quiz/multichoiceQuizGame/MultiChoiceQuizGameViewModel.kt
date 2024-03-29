package com.example.flashcard.quiz.multichoiceQuizGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.CardLevel
import com.example.flashcard.util.FlashCardMiniGameRef
import com.example.flashcard.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.UiState
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

    fun initCardList(gameCards: MutableList<ImmutableCard?>) {
        cardList = gameCards
    }
    fun initDeck(gameDeck: ImmutableDeck) {
        deck = gameDeck
    }

    fun initOriginalCardList(gameCards: List<ImmutableCard?>) {
        originalCardList = gameCards
    }

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
                cards.random()?.cardDefinition?.first()?.definition
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

    fun increaseAttemptTime() {
        attemptTime += 1
    }

    fun swipe(): Boolean {
        if (attemptTime == 0) {
            progress += 100/cardSum()
        } else {
            attemptTime = 0
        }
        currentCardPosition += 1
        return currentCardPosition != cardSum()
    }
    fun getCurrentCardPosition() = currentCardPosition
    fun cardSum() = cardList.size
    fun getMissedCardSum() = missedCards.size
    fun getKnownCardSum() = cardSum() - getMissedCardSum()
    fun getMissedCard(): MutableList<ImmutableCard?> {
        val newCards = arrayListOf<ImmutableCard?>()
        missedCards.forEach { immutableCard -> newCards.add(immutableCard) }
        return newCards
    }
    fun initTimedFlashCard() {
        missedCards.clear()
        progress = 0
        currentCardPosition = 0
    }

    fun onCardMissed() {
        val missedCard = cardList[currentCardPosition]
        if (missedCard !in missedCards) {
            missedCards.add(missedCard)
        }
        increaseAttemptTime()
    }

    fun isUserChoiceCorrect(userChoice: String, correctChoice: String): Boolean {
        val isCorrect = userChoice == correctChoice
        if (!isCorrect) {
            onCardMissed()
            onUserAnswered(isCorrect)
        }
        if (attemptTime == 0 && isCorrect) {
            onUserAnswered(isCorrect)
        }
        return isCorrect
    }

    private fun toListOfMultiChoiceQuizGameCardModel(cards: List<ImmutableCard?>, cardOrientation: String): List<MultiChoiceGameCardModel> {
        val temporaryList = mutableListOf<MultiChoiceGameCardModel>()
        cards.forEach {
            if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
                val alternatives = getWordAlternatives(originalCardList, it?.cardDefinition?.first()?.definition!!, 4, cardOrientation)
                temporaryList.add(
                    MultiChoiceGameCardModel(
                        it.cardContent?.content!!,
                        it.cardDefinition.first().definition!!,
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
                        it.cardDefinition?.first()?.definition!!,
                        it.cardContent.content,
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

    fun updateCard(cardOrientation: String) {
        if (cardList.size == 0) {
            _actualCards.value = UiState.Error("No Cards To Revise")
        } else {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _actualCards.value = UiState.Success(toListOfMultiChoiceQuizGameCardModel(cardList, cardOrientation))
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

    private fun onUserAnswered(isKnown: Boolean) {
        cardList.let {cards ->
            val card = cards[currentCardPosition]
            if (card != null) {
                val newStatus = spaceRepetitionHelper.status(card, isKnown)
                val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, isKnown, newStatus)
                val lastRevision = spaceRepetitionHelper.today()
                val nextForgettingDate = spaceRepetitionHelper.nextForgettingDate(card, isKnown, newStatus)
                val newCard = ImmutableCard(
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
                    nextForgettingDate,
                    nextRevision,
                    card.cardType,
                    card.creationDateTime
                )
                updateCard(newCard)
            }
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