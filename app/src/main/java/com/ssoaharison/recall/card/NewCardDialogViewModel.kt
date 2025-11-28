package com.ssoaharison.recall.card

import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.models.OpenTriviaQuestion
import com.ssoaharison.recall.backend.models.QuizQuestions
import com.ssoaharison.recall.backend.models.isCorrect
import com.ssoaharison.recall.backend.OpenTriviaRepository
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.entities.relations.CardContentWithDefinitions
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCardContent
import com.ssoaharison.recall.backend.models.ExternalCardDefinition
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.util.CardType.MULTIPLE_CHOICE_CARD
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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

    private var _contentField = MutableStateFlow<ContentFieldModel>(ContentFieldModel(null, null, null, false))
    val contentField: StateFlow<ContentFieldModel> = _contentField.asStateFlow()

    fun initContentField(content: ExternalCardContent?) {
        _contentField.value = if (content == null) {
            ContentFieldModel(null, null, null, false)
        } else {
            ContentFieldModel(content.contentId, content.contentText, content.contentImage, false)
        }
    }

    fun updateContentField(updatedContentField: ContentFieldModel) {
        _contentField.value = updatedContentField
    }

    fun clearContentField() {
        initContentField(null)
    }


    private var _definitionFields = MutableStateFlow<ArrayList<DefinitionFieldModel>>(arrayListOf())
    val definitionFields: StateFlow<ArrayList<DefinitionFieldModel>> = _definitionFields.asStateFlow()

    fun initDefinitionFields(cardDefinitions: List<ExternalCardDefinition>?) {
        if (cardDefinitions != null) {
            cardDefinitions.forEach { definition ->
                addDefinitionField(definition)
            }
        } else {
            addDefinitionField(null)
        }

    }

    fun addDefinitionField(definition: ExternalCardDefinition?) {
        val newDefinitionFieldModel = if (definition == null) {
            DefinitionFieldModel(
                definitionId = null,
                definitionText = null,
                definitionImage = null,
                isCorrectDefinition = true,
                hasFocus = false
            )
        } else {
            DefinitionFieldModel(
                definitionId = definition.definitionId,
                definitionText = definition.definitionText,
                definitionImage = definition.definitionImage,
                isCorrectDefinition = isCorrect(definition.isCorrectDefinition),
                hasFocus = false
            )
        }
        _definitionFields.value.add(newDefinitionFieldModel)
    }

    fun deleteDefinitionField(definitionFieldModel: DefinitionFieldModel) {
        _definitionFields.value.remove(definitionFieldModel)
    }

    fun clearDefinitionFields() {
        _definitionFields.value = arrayListOf()
        initDefinitionFields(null)
    }

    fun updateDefinitionField(updatedDefinitionField: DefinitionFieldModel, index: Int) {
        _definitionFields.value[index].definitionImage = updatedDefinitionField.definitionImage
        _definitionFields.value[index].definitionText = updatedDefinitionField.definitionText
    }

    fun getDefinitionFieldCount() = _definitionFields.value.size

    fun definitionFieldToCardDefinition(
        field: DefinitionFieldModel,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardDefinition {
        return CardDefinition(
            definitionId = field.definitionId ?: UUID.randomUUID().toString(),
            cardOwnerId = cardId,
            deckOwnerId = deckId,
            contentOwnerId = contentId,
            isCorrectDefinition = isCorrectRevers(field.isCorrectDefinition),
            definitionText = field.definitionText,
            definitionImageName = field.definitionImage?.name,
            definitionAudioName = null
        )
    }

    fun changeFieldFocus(index: Int) {
        if (index < 0) {
            _definitionFields.value.forEach { field ->
                field.hasFocus = false
            }
            _contentField.value.hasFocus = true
        } else {
            _contentField.value.hasFocus = false
            _definitionFields.value.forEachIndexed { i,  field ->
                field.hasFocus = i == index
            }
        }
    }

    fun getActiveFieldIndex(): Int? {
        if (_contentField.value.hasFocus) {
            return -1
        } else {
            _definitionFields.value.forEachIndexed { index, field ->
                if (field.hasFocus) {
                    return index
                }
            }
        }
        return null
    }

    suspend fun resultsToImmutableCards(
        deckId: String,
        results: List<OpenTriviaQuestion>?
    ): List<CardWithContentAndDefinitions> {
        val newCards = arrayListOf<CardWithContentAndDefinitions>()
        results?.forEach { result ->
            val newCardId = UUID.randomUUID().toString()
            val contentId = UUID.randomUUID().toString()
            val formatedQuestion = reformatText(result.question)
            val newCardContent = generateCardContent(formatedQuestion, null, null, newCardId, contentId, deckId)
            val newCardDefinitions = generateCardDefinitions(
                result.correctAnswer,
                result.incorrectAnswers,
                newCardId,
                contentId,
                deckId
            )

            val newCard = Card(
                cardId = newCardId,
                deckOwnerId = deckId,
                cardLevel = L1,
                cardType = MULTIPLE_CHOICE_CARD,
                revisionTime = 0,
                missedTime = 0,
                creationDate = today(),
                lastRevisionDate = null,
                nextMissMemorisationDate = null,
                nextRevisionDate = null,
                cardContentLanguage = "English (United Kingdom)",
                cardDefinitionLanguage = "English (United Kingdom)"
            )

            val newCardWithContentAndDefinitions = CardWithContentAndDefinitions(
                card = newCard,
                contentWithDefinitions = CardContentWithDefinitions(
                    content = newCardContent,
                    definitions = newCardDefinitions
                )
            )

            newCards.add(newCardWithContentAndDefinitions)

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
        newCardDefinitions.add(createDefinition(correctAnswer, null, null, true, cardId, contentId, deckId))
        incorrectAnswers.forEach { incorrectAnswer ->
            val reformatedText = reformatText(incorrectAnswer)
            newCardDefinitions.add(
                createDefinition(
                    reformatedText,
                    null,
                    null,
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
        imageName: String?,
        audioName: String?,
        isCorrect: Boolean,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardDefinition {
        return CardDefinition(
            definitionId = UUID.randomUUID().toString(),
            cardOwnerId = cardId,
            deckOwnerId = deckId,
            contentOwnerId = contentId,
            isCorrectDefinition = isCorrectRevers(isCorrect),
            definitionText = text,
            definitionImageName = imageName,
            definitionAudioName = audioName,
        )
    }

    fun generateCardContent(
        text: String,
        imageName: String?,
        audioName: String?,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardContent {
        return CardContent(
            contentId = contentId,
            cardOwnerId = cardId,
            deckOwnerId = deckId,
            contentText = text,
            contentImageName = imageName,
            contentAudioName = audioName
        )
    }

    fun insertCards(cards: List<CardWithContentAndDefinitions>) = viewModelScope.launch {
        val cardsToAdd = cards.reversed()
        repository.insertCardsWithContentAndDefinition(cardsToAdd)
        addedCardSum += cardsToAdd.size
    }

    fun insertCard(card: CardWithContentAndDefinitions) = viewModelScope.launch {
        repository.insertCardWithContentAndDefinition(card)
        addedCardSum++
    }

    fun getAddedCardSum() = addedCardSum
}

fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

fun isCorrect(index: Int?) = index == 1

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