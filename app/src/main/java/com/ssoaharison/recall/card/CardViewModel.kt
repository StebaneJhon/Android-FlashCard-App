package com.ssoaharison.recall.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.models.toExternal
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

    private var _deckWithAllCards = MutableStateFlow<UiState<ImmutableDeckWithCards>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<ImmutableDeckWithCards>> = _deckWithAllCards.asStateFlow()
    private lateinit var actualDeck: ImmutableDeck
    private var fetchJob: Job? = null
    private val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()

    fun getDeckWithCards(deckId: String) {
        fetchJob?.cancel()
        _deckWithAllCards.value = UiState.Loading
        fetchJob = viewModelScope.launch {
            try {
                repository.getImmutableDeckWithCards(deckId).collect {
                    actualDeck = it.deck!!
                    if (it.cards?.isEmpty()!!) {
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

    suspend fun getCards(deckId: String) = repository.getCards(deckId)

    fun getBoxLevels(): List<ImmutableSpaceRepetitionBox>? {
       return spaceRepetitionHelper.getActualBoxLevels()
    }

    fun getActualDeck() = actualDeck

    fun insertCards(cards: List<ImmutableCard>, externalDeck: ImmutableDeck) = viewModelScope.launch {
        val cardsToAdd = cards.reversed()
        repository.insertCards(cardsToAdd)
    }

    fun insertCard(card: ImmutableCard) = viewModelScope.launch {
        repository.insertCard(card)
    }

    fun updateCard(card: ImmutableCard) = viewModelScope.launch {
        repository.updateCard(card)
    }

    fun deleteCard(card: ImmutableCard) = viewModelScope.launch {
        repository.deleteCard(card)
    }

    fun searchCard(searchQuery: String, deckId: String): LiveData<Set<ImmutableCard?>> {
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