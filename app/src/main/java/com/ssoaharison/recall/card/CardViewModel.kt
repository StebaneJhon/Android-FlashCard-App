package com.ssoaharison.recall.card

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCard
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.deck.ColorModel
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class CardViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _deckWithAllCards = MutableStateFlow<UiState<ExternalDeckWithCardsAndContentAndDefinitions>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<ExternalDeckWithCardsAndContentAndDefinitions>> = _deckWithAllCards.asStateFlow()
    private lateinit var actualDeck: ExternalDeck
    private lateinit var actualDeckSubdecks: List<ExternalDeck>
    private var fetchJob: Job? = null
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()

    private lateinit var deckPath: List<ExternalDeck>

    fun getDeckWithCards(deckId: String, context: Context) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getExternalDeckWithCardsAndContentAndDefinitions(deckId, context).collect {
                    actualDeck = it.deck
                    if (it.cards.isEmpty()) {
                        _deckWithAllCards.value = UiState.Error("Empty deck")
                    } else {
                        _deckWithAllCards.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _deckWithAllCards.value = UiState.Error(e.toString())
            }
        }
    }

    private var _subdecks = MutableStateFlow<UiState<List<ExternalDeck>>>(UiState.Loading)
    val subdecks: StateFlow<UiState<List<ExternalDeck>>> = _subdecks.asStateFlow()
    var fetchSubdeckJob: Job? = null
    fun getSubdecks(deckId: String) {
        fetchSubdeckJob?.cancel()
        _subdecks.value = UiState.Loading
        fetchSubdeckJob = viewModelScope.launch {
            try {
                repository.getSubdecks(deckId).collect {
                    if (it.isEmpty()) {
                        _subdecks.value = UiState.Error("No subdeck found")
                    } else {
                        actualDeckSubdecks = it
                        _subdecks.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _subdecks.value = UiState.Error(e.message.toString())
            }
        }
    }

    suspend fun getDeckPath(deck: ExternalDeck, result: (List<ExternalDeck>) -> Unit) {
        val path = repository.getDeckPath(deck)
        deckPath = path
        result(path)
    }

    fun deckPath() = deckPath

    fun insertSubdeck(subdeck: Deck) = viewModelScope.launch {
        repository.insertDeck(subdeck)
    }

    fun deleteSubdeck(subdeck: ExternalDeck) = viewModelScope.launch {
        repository.deleteDeckWithCards(subdeck.deckId)
    }

    fun updateSubdeck(deck: Deck) = viewModelScope.launch {
        repository.updateDeck(deck)
    }

    suspend fun getCards(deckId: String) = repository.getCards(deckId)

    fun getBoxLevels(): List<ImmutableSpaceRepetitionBox>? {
       return spaceRepetitionHelper.getActualBoxLevels()
    }

    fun getActualDeck() = actualDeck
    fun getActualDeckSubdecks() = actualDeckSubdecks


    suspend fun getMainDeck() = repository.getMainDeck()

    fun insertCards(cards: List<CardWithContentAndDefinitions>, externalDeck: ExternalDeck) = viewModelScope.launch {
        val cardsToAdd = cards.reversed()
//        repository.insertCards(cardsToAdd)
        repository.insertCardsWithContentAndDefinition(cards)
    }

    fun insertCard(card: CardWithContentAndDefinitions) = viewModelScope.launch {
        repository.insertCardWithContentAndDefinition(card)
    }

    fun updateCard(card: CardWithContentAndDefinitions) = viewModelScope.launch {
        repository.updateCardWithContentAndDefinition(card)
    }

    fun deleteCard(card: CardWithContentAndDefinitions, context: Context) = viewModelScope.launch {
        repository.deleteCardWithContentAndDefinitions(card, context)
    }

    private var _foundCards = MutableStateFlow<UiState<List<ExternalCardWithContentAndDefinitions>>>(UiState.Loading)
    val foundCards: StateFlow<UiState<List<ExternalCardWithContentAndDefinitions>>> = _foundCards.asStateFlow()
    var searchCardJob: Job? = null
    fun searchCard(searchQuery: String, context: Context){
        searchCardJob?.cancel()
        _foundCards.value = UiState.Loading
        searchCardJob = viewModelScope.launch {
            try {
                repository.searchCard(searchQuery, context).collect {
                    if (it.isEmpty()) {
                        _foundCards.value = UiState.Error("No cards found")
                    } else {
                        _foundCards.value = UiState.Success(it)
                    }
                }
            } catch (e: okio.IOException) {
                _foundCards.value = UiState.Error(e.message.toString())
            }
        }
    }

    private var _foundDecks = MutableStateFlow<UiState<List<ExternalDeck>>>(UiState.Loading)
    val foundDecks: StateFlow<UiState<List<ExternalDeck>>> = _foundDecks.asStateFlow()
    var searchDeckJob: Job? = null
    fun searchDeck(searchQuery: String) {
        searchDeckJob?.cancel()
        _foundDecks.value = UiState.Loading
        searchDeckJob = viewModelScope.launch {
            try {
                repository.searchDeck(searchQuery).collect {
                    if (it.isEmpty()) {
                        _foundDecks.value = UiState.Error("No decks found")
                    } else {
                        _foundDecks.value = UiState.Success(it)
                    }
                }
            } catch (e: okio.IOException) {
                _foundDecks.value = UiState.Error(e.message.toString())
            }
        }
    }



    fun updateCardContentLanguage(cardId: String, language: String) = viewModelScope.launch {
        repository.updateCardContentLanguage(cardId, language)
    }

    fun updateCardDefinitionLanguage(cardId: String, language: String) = viewModelScope.launch {
        repository.updateCardDefinitionLanguage(cardId, language)
    }

    fun updateDefaultCardContentLanguage(deckId: String, language: String) = viewModelScope.launch {
        repository.updateDefaultCardContentLanguage(deckId, language)
    }

    fun updateDefaultCardDefinitionLanguage(deckId: String, language: String) = viewModelScope.launch {
        repository.updateDefaultCardDefinitionLanguage(deckId, language)
    }

    private var _colorSelectionList = MutableStateFlow<ArrayList<ColorModel>>(arrayListOf())
    val colorSelectionList: StateFlow<ArrayList<ColorModel>> = _colorSelectionList.asStateFlow()

    fun initColorSelection(colors: Map<String, Int>, actualColorId: String?) {
        colors.forEach { (id, color) ->
            if (actualColorId == id) {
                _colorSelectionList.value.add(
                    ColorModel(color, id, true)
                )
            } else {
                _colorSelectionList.value.add(
                    ColorModel(color, id)
                )
            }

        }
    }

    fun selectColor(id: String) {
        _colorSelectionList.value.forEachIndexed { index, color ->
            if (color.id == id) {
                _colorSelectionList.value[index].isSelected = true
            } else {
                _colorSelectionList.value[index].isSelected = false
            }
        }
    }

    fun updateDeck(deck: Deck) = viewModelScope.launch {
        repository.updateDeck(deck)
    }

    suspend fun getDeckById(deckId: String, deck: (ExternalDeck) -> Unit) {
        deck(repository.getDeckById(deckId))
    }

}

class CardViewModelFactory(private val repository: FlashCardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}