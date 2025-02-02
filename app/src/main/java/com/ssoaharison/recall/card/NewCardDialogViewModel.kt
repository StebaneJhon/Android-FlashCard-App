package com.ssoaharison.recall.card

import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.OpenTriviaQuestion
import com.ssoaharison.recall.backend.models.QuizQuestions
import com.ssoaharison.recall.backend.models.isCorrect
import com.ssoaharison.recall.backend.OpenTriviaRepository
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.util.CardType.MULTIPLE_CHOICE_CARD
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NewCardDialogViewModel(
    private val openTriviaRepository: OpenTriviaRepository,
    private val repository: FlashCardRepository,
) : ViewModel() {

    private var fetchOpenTriviaJob: Job? = null
    private var fetchDeckDeletionJob: Job? = null

    private var addedCardSum = 0

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

    suspend fun resultsToImmutableCards(
        deckId: String,
        results: List<OpenTriviaQuestion>?
    ): List<ImmutableCard> {
        val newCards = arrayListOf<ImmutableCard>()
        results?.forEach { result ->
            delay(10)
            val newCardId = now()
            delay(10)
            val contentId = now()
            val formatedQuestion = reformatText(result.question)
            val newCardContent = generateCardContent(formatedQuestion, newCardId, contentId, deckId)
            val newCardDefinitions = generateCardDefinitions(
                result.correctAnswer,
                result.incorrectAnswers,
                newCardId,
                contentId,
                deckId
            )

            val newCard = ImmutableCard(
                newCardId,
                newCardContent,
                newCardDefinitions,
                deckId,
                isCorrect(0),
                revisionTime = 0,
                0,
                today(),
                null,
                L1,
                null,
                null,
                MULTIPLE_CHOICE_CARD,
                "English (United Kingdom)",
                "English (United Kingdom)",
            )

            newCards.add(newCard)

        }
        return newCards
    }

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
            val reformatedText = reformatText(incorrectAnswer)
            newCardDefinitions.add(
                createDefinition(
                    reformatedText,
                    false,
                    cardId,
                    contentId,
                    deckId
                )
            )
        }
        return newCardDefinitions
    }

    fun createDefinition(
        text: String,
        isCorrect: Boolean,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardDefinition {
        return CardDefinition(
            null,
            cardId,
            deckId,
            contentId,
            text,
            isCorrectRevers(isCorrect)
        )
    }

    fun generateCardContent(
        text: String,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardContent {
        return CardContent(
            contentId,
            cardId,
            deckId,
            text
        )
    }

    fun insertCards(cards: List<ImmutableCard>) = viewModelScope.launch {
        val cardsToAdd = cards.reversed()
        repository.insertCards(cardsToAdd)
        addedCardSum += cardsToAdd.size
    }

    fun insertCard(card: ImmutableCard) = viewModelScope.launch {
        repository.insertCard(card)
        addedCardSum++
    }

    fun getAddedCardSum() = addedCardSum
}

fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

private fun setCategory(category: Int): String {
    if (category > 0) {
        return "$category"
    }
    return ""
}

fun now(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
    return LocalDateTime.now().format(formatter)
}

fun today(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return formatter.format(LocalDate.now())
}

private fun reformatText(text: String) =
    HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

class NewCardDialogViewModelFactory(
    private val openTriviaRepository: OpenTriviaRepository,
    private val repository: FlashCardRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewCardDialogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewCardDialogViewModel(openTriviaRepository, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}