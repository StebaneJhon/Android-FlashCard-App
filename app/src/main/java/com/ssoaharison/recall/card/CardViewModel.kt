package com.ssoaharison.recall.card

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
    private var fetchJob: Job? = null
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()

    fun getDeckWithCards(deckId: String) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getExternalDeckWithCardsAndContentAndDefinitions(deckId).collect {
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
                        _subdecks.value = UiState.Success(it)
                    }
                }
            } catch (e: IOException) {
                _subdecks.value = UiState.Error(e.message.toString())
            }
        }
    }

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

    fun deleteCard(card: CardWithContentAndDefinitions) = viewModelScope.launch {
        repository.deleteCardWithContentAndDefinitions(card)
    }

    fun searchCard(searchQuery: String, deckId: String): LiveData<List<ExternalCardWithContentAndDefinitions>> {
        return repository.searchCard(searchQuery, deckId).asLiveData()
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