package com.ssoaharison.recall.card

import android.content.Context
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.R
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
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.PhotoModel
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.util.CardType.MULTIPLE_CHOICE_CARD
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.update
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

    private var _contentField =
        MutableStateFlow<ContentFieldModel>(ContentFieldModel(null, null, null, null, false))
    val contentField: StateFlow<ContentFieldModel> = _contentField.asStateFlow()

    fun updateContentField(updatedContentField: ContentFieldModel) {
        _contentField.update { updatedContentField }
    }
    fun initContentField(content: ExternalCardContent?) {
        _contentField.update {
            if (content == null) {
                ContentFieldModel(null, null, null, null, false)
            } else {
                ContentFieldModel(content.contentId, content.contentText, content.contentImage, content.contentAudio, false)
            }
        }
    }

    fun deleteContentImageField() {
        _contentField.update { field ->
            field.contentImage = null
            field
        }
    }

    fun deleteContentAudioField() {
        _contentField.update { field ->
            field.contentAudio = null
            field
        }
    }




    private var _definitionFields = MutableStateFlow<MutableList<DefinitionFieldModel>>(mutableListOf())
    val definitionFields: StateFlow<MutableList<DefinitionFieldModel>> = _definitionFields.asStateFlow()

//    fun initDefinitionFields(cardDefinitions: List<ExternalCardDefinition>?) {
//        if (cardDefinitions != null) {
//            cardDefinitions.forEach { definition ->
//                addDefinitionField(definition)
//            }
//        } else {
//            addDefinitionField(null)
//        }
//
//    }


    fun initAddCardFields(card: ExternalCardWithContentAndDefinitions?) {
        if (card != null) {
            initContentField(card.contentWithDefinitions.content)
//            _contentField.update {
//                ContentFieldModel(
//                    contentId = card.contentWithDefinitions.content.contentId,
//                    contentText = card.contentWithDefinitions.content.contentText,
//                    contentImage = card.contentWithDefinitions.content.contentImage,
//                    contentAudio = card.contentWithDefinitions.content.contentAudio,
//                    hasFocus = false
//                )
//            }
            _definitionFields.update { mutableListOf() }
            card.contentWithDefinitions.definitions.forEach { definition ->
                addDefinitionField(definition)
            }
        } else {
            ContentFieldModel(null, null, null, null, false)
            addDefinitionField(null)
        }
    }

    fun addDefinitionField(definition: ExternalCardDefinition?) {
        val newDefinitionFieldModel = if (definition == null) {
            DefinitionFieldModel(
                definitionId = UUID.randomUUID().toString(),
                definitionText = null,
                definitionImage = null,
                definitionAudio = null,
                isCorrectDefinition = true,
                hasFocus = false
            )
        } else {
            DefinitionFieldModel(
                definitionId = definition.definitionId,
                definitionText = definition.definitionText,
                definitionImage = definition.definitionImage,
                definitionAudio = definition.definitionAudio,
                isCorrectDefinition = isCorrect(definition.isCorrectDefinition),
                hasFocus = false
            )
        }
        _definitionFields.update { fields ->
            (fields + newDefinitionFieldModel) as MutableList<DefinitionFieldModel>
        }
    }

    fun deleteDefinitionField(id: String) {
        _definitionFields.update { fields ->
            fields.filter { it.definitionId != id } as MutableList<DefinitionFieldModel>
        }
    }

    fun clearFields() {
        _contentField.update {
            ContentFieldModel(
                contentId = null,
                contentText = null,
                contentImage = null,
                contentAudio = null,
                hasFocus = false
            )
        }
        _definitionFields.update {
            mutableListOf(
                DefinitionFieldModel(
                    definitionId = UUID.randomUUID().toString(),
                    definitionText = null,
                    definitionImage = null,
                    definitionAudio = null,
                    isCorrectDefinition = true,
                    hasFocus = false
                )
            )
        }
    }

    fun updateDefinitionField(updatedDefinitionField: DefinitionFieldModel, id: String) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { index, field ->
                if (field.definitionId == id) {
                    fields[index] = updatedDefinitionField
                }
            }
            fields
        }
    }

    fun updateDefinitionStatus(id: String, status: Boolean) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { index, field ->
                if (field.definitionId == id) {
                    fields[index].isCorrectDefinition = status
                }
            }
            fields
        }
    }

    fun updateDefinitionImage(id: String, image: PhotoModel) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { index, field ->
                if (field.definitionId == id) {
                    fields[index].definitionImage = image
                }
            }
            fields
        }
    }

    fun updateDefinitionAudio(id: String, audio: AudioModel) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { index, field ->
                if (field.definitionId == id) {
                    fields[index].definitionAudio = audio
                }
            }
            fields
        }
    }

    fun deleteDefinitionImageField(id: String) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { index, field ->
                if (field.definitionId == id) {
                    fields[index].definitionImage = null
                }
            }
            fields
        }
    }

    fun deleteDefinitionAudioField(id: String) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { index, field ->
                if (field.definitionId == id) {
                    fields[index].definitionAudio = null
                }
            }
            fields
        }
    }

    fun updateDefinitionText(id: String, text: String) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { index, field ->
                if (field.definitionId == id) {
                    fields[index].definitionText = text
                }
            }
            fields
        }
    }

    fun getDefinitionStatusById(id: String): Boolean? {
        _definitionFields.value.forEach { fieldModel ->
            if (fieldModel.definitionId == id) {
                return fieldModel.isCorrectDefinition
            }
        }
        return null
    }

    fun getDefinitionFieldCount() = _definitionFields.value.size

    fun getDefinitionFieldAt(index: Int) = _definitionFields.value[index]

    fun getDefinitionTexts(): List<String>? {
        val result = mutableListOf<String>()
        definitionFields.value.forEach {
            if (it.definitionText != null) {
                result.add(it.definitionText!!)
            }
        }
        return if (result.isEmpty()) null else result
    }

    fun hasDefinitionText(): Boolean {
        definitionFields.value.forEach { field ->
            if (field.definitionText != null && field.definitionText!!.isNotBlank() && field.definitionText!!.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    fun hasContentText(): Boolean {
        return contentField.value.contentText != null && contentField.value.contentText!!.isNotBlank() && contentField.value.contentText!!.isNotEmpty()
    }

    fun definitionFieldToCardDefinition(
        field: DefinitionFieldModel,
        cardId: String,
        contentId: String,
        deckId: String
    ): CardDefinition {
        return CardDefinition(
            definitionId = field.definitionId,
            cardOwnerId = cardId,
            deckOwnerId = deckId,
            contentOwnerId = contentId,
            isCorrectDefinition = isCorrectRevers(field.isCorrectDefinition),
            definitionText = field.definitionText,
            definitionImageName = field.definitionImage?.name,
            definitionAudioName = field.definitionAudio?.file?.name,
        )
    }

    fun focusToContent() {
        _definitionFields.update { fields ->
            val newField = fields.toMutableList()
            newField.forEach { field ->
                field.hasFocus = false
            }
            newField
        }
        _contentField.update { field -> field.copy(hasFocus = true) }
    }

    fun changeFieldFocus(index: Int) {
        _definitionFields.update { fields ->
            fields.forEachIndexed { i, field ->
                fields[i].hasFocus = i == index
            }
            fields
        }
        _contentField.update { field -> field.copy(hasFocus = false) }
    }

    fun getActiveFieldIndex(): Int? {
        definitionFields.value.forEachIndexed { index, field ->
            if (field.hasFocus) {
                return index
            }
        }
        if (contentField.value.hasFocus) {
            return -1
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
        newCardDefinitions.add(
            createDefinition(
                correctAnswer,
                null,
                null,
                true,
                cardId,
                contentId,
                deckId
            )
        )
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
        text: String?,
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
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewCardDialogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewCardDialogViewModel(openTriviaRepository, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}