package com.example.flashcard.deck

import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.flashcard.backend.FlashCardRepository
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.OpenTriviaQuestion
import com.example.flashcard.backend.Model.QuizQuestions
import com.example.flashcard.backend.Model.isCorrect
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.OpenTriviaRepository
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.util.CardLevel.L1
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DeckViewModel(
    private val repository: FlashCardRepository,
    private val openTriviaRepository: OpenTriviaRepository
) : ViewModel() {

    private var _allDecks = MutableStateFlow<UiState<List<ImmutableDeck>>>(UiState.Loading)
    val allDecks: StateFlow<UiState<List<ImmutableDeck>>> = _allDecks.asStateFlow()

    private var fetchJob: Job? = null
    private var fetchOpenTriviaJob: Job? = null
    private var fetchDeckDeletionJob: Job? = null

    fun getAllDecks() {
        fetchJob?.cancel()
        _allDecks.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.allDecks().collect {
                    if (it.isEmpty()) {
                        _allDecks.value = UiState.Error("No Deck")
                    } else {
                        _allDecks.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _allDecks.value = UiState.Error(e.toString())
            }
        }
    }

    fun searchDeck(searchQuery: String): LiveData<List<ImmutableDeck>> {
        return repository.searchDeck(searchQuery).asLiveData()
    }

    fun insertDeck(deck: Deck) = viewModelScope.launch {
        repository.insertDeck(deck)
    }

    fun deleteDeck(deck: ImmutableDeck) {
        fetchDeckDeletionJob?.cancel()
        fetchDeckDeletionJob = viewModelScope.launch {
            repository.deleteDeckWithCards(deck)
        }
    }

    fun updateDeck(deck: Deck) = viewModelScope.launch {
        repository.updateDeck(deck)
    }

    private var _deckWithAllCards =
        MutableStateFlow<UiState<ImmutableDeckWithCards>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<ImmutableDeckWithCards>> =
        _deckWithAllCards.asStateFlow()

    fun getDeckWithCards(deckId: String) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getImmutableDeckWithCards(deckId).collect {
                    _deckWithAllCards.value = UiState.Success(it)
                }
            } catch (e: IOException) {
                _deckWithAllCards.value = UiState.Error(e.toString())
            }
        }
    }

    private var _openTriviaResponse = MutableStateFlow<UiState<QuizQuestions>>(UiState.Loading)
    val openTriviaResponse: StateFlow<UiState<QuizQuestions>> =
        _openTriviaResponse.asStateFlow()

    fun getOpenTriviaQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String
    ) {
        fetchOpenTriviaJob?.cancel()
        _openTriviaResponse.value = UiState.Loading
        fetchDeckDeletionJob = viewModelScope.launch {
            val response = openTriviaRepository.getOpenTriviaQuestion(
                "$amount",
                setCategory(category),
                difficulty,
                type
            )
            if (response.isSuccessful) {
                _openTriviaResponse.value = UiState.Success(response.body()!!)
            } else {
                val message = response.code().toString()
                _openTriviaResponse.value = UiState.Error(message)
            }
        }
    }

    fun insertOpenTriviaQuestions(deckName: String, deckDescription: String, cards: List<OpenTriviaQuestion>) {
        fetchOpenTriviaJob?.cancel()
        fetchOpenTriviaJob = viewModelScope.launch {
            val newDeck = generateDeck(deckName, deckDescription)
            delay(100)
            repository.insertDeck(newDeck)
            delay(100)
            val newCards = resultsToImmutableCards(newDeck, cards)
            delay(200)
            repository.insertCards(newCards, newDeck.toExternal())
        }
    }

    private fun resultsToImmutableCards(
        deck: Deck,
        results: List<OpenTriviaQuestion>?
    ): List<ImmutableCard> {
        val newCards = arrayListOf<ImmutableCard>()
        results?.forEach { result ->
            val newCardId = now()
            val contentId = now()
            val newCardContent =
                generateCardContent(result.question, newCardId, contentId, deck.deckId)
            val newCardDefinitions = generateCardDefinitions(
                result.correctAnswer,
                result.incorrectAnswers,
                newCardId,
                contentId,
                deck.deckId
            )

            val newCard = ImmutableCard(
                newCardId,
                newCardContent,
                newCardDefinitions,
                deck.deckId,
                isCorrect(0),
                revisionTime = 0,
                0,
                today(),
                null,
                L1,
                null,
                null,
                result.type
            )

            newCards.add(newCard)

        }
        return newCards
    }

//    private fun toCardType(type: String) = if (type == "multiple") MULTIPLE_ANSWER_CARD else TRUE_OR_FALSE_CARD

    private fun generateCardDefinitions(
        correctAnswer: String,
        incorrectAnswers: List<String>,
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition> {
        val newCardDefinitions = arrayListOf<CardDefinition>()
        newCardDefinitions.add(createDefinition(correctAnswer, true, cardId, contentId, deckId))
        incorrectAnswers.forEach { incorrectAnswer ->
            newCardDefinitions.add(
                createDefinition(
                    incorrectAnswer,
                    false,
                    cardId,
                    contentId,
                    deckId
                )
            )
        }
        return newCardDefinitions
    }

    private fun createDefinition(
        text: String,
        isCorrect: Boolean,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardDefinition {
        val reformatedText = reformatText(text)
        return CardDefinition(
            null,
            cardId,
            deckId,
            contentId,
            reformatedText,
            isCorrectRevers(isCorrect)
        )
    }

    private fun generateCardContent(
        text: String,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardContent {
        val formatedText = reformatText(text)
        return CardContent(
            contentId,
            cardId,
            deckId,
            formatedText
        )
    }

    fun generateDeck(deckName: String, deckDescription: String): Deck {
        return Deck(
            now(),
            deckName,
            deckDescription,
            "English",
            "English",
            DeckColorCategorySelector().getRandomColor(),
            0,
            null,
            0
        )
    }

    private fun reformatText(text: String) =
        HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

    private fun setCategory(category: Int): String {
        if (category > 0) {
            return "$category"
        }
        return ""
    }

    private fun now(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        return LocalDateTime.now().format(formatter)
    }

    private fun today(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return formatter.format(LocalDate.now())
    }

}

class DeckViewModelFactory(
    private val repository: FlashCardRepository,
    private val openTriviaRepository: OpenTriviaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeckViewModel(repository, openTriviaRepository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}