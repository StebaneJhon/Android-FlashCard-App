package com.ssoaharison.recall.card

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ssoaharison.recall.backend.FlashCardRepository
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.util.ColorModel
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.ssoaharison.recall.util.Async
import com.ssoaharison.recall.util.CardSortOptions.SORT_CARD_ALPHABETICALLY
import com.ssoaharison.recall.util.CardSortOptions.SORT_CARD_BY_CREATION_DATE
import com.ssoaharison.recall.util.CardSortOptions.SORT_CARD_BY_LEVEL
import com.ssoaharison.recall.util.DeckRef.DECK_SORT_ALPHABETICALLY
import com.ssoaharison.recall.util.DeckRef.DECK_SORT_BY_CARD_SUM
import com.ssoaharison.recall.util.DeckRef.DECK_SORT_BY_CREATION_DATE
import com.ssoaharison.recall.util.ItemLayoutManager.LINEAR
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class CardViewModel(private val repository: FlashCardRepository) : ViewModel() {

    private var _cards =
        MutableStateFlow<Async<List<ExternalCardWithContentAndDefinitions>>>(Async.Loading)
    private var _decks = MutableStateFlow<Async<List<ExternalDeck>>>(Async.Loading)
    private var _cardViewMode = MutableStateFlow<String>(LINEAR)
    private val _isLoading = MutableStateFlow(false)
    private val _isError = MutableStateFlow(false)

    val uiState: StateFlow<CardFragmentUiState> = combine(
        _cards, _decks, _cardViewMode, _isLoading, _isError
    ) { cards, decks, cardViewMode, isLoading, isError ->
        var isFetchingError = cards is Async.Error && decks is Async.Error
        var isFetchingLoading = cards is Async.Loading || decks is Async.Loading
        var fetchedCards = (cards as? Async.Success)?.data ?: emptyList()
        var fetchedDecks = (decks as? Async.Success)?.data ?: emptyList()
        CardFragmentUiState(
            cards = fetchedCards,
            decks = fetchedDecks,
            cardsViewMode = cardViewMode,
            isLoading = isFetchingLoading,
            isError = isFetchingError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = CardFragmentUiState()
    )

    fun fetchCards(deckId: String, context: Context, sortPref: String) {
        viewModelScope.launch {
            _cards.value = Async.Loading
            repository.getExternalDeckWithCardsAndContentAndDefinitions(deckId, context)
                .collect { (deck, cards) ->
                    actualDeck = deck
                    _cards.value = if (cards.isEmpty()) {
                        Async.Error(true)
                    } else {
                        Async.Success(applyCardsSortPref(sortPref, cards))
                    }
                }
        }
    }

    fun fetchDecks(deckId: String, sortPref: String) {
        viewModelScope.launch {
            _decks.value = Async.Loading
            repository.getSubdecks(deckId).collect { decks ->
                _decks.value = if (decks.isEmpty()) {
                    Async.Error(true)
                } else {
                    actualDeckSubdecks = decks
                    val sortedSupport = applyDeckSortPref(sortPref, decks)
                    Async.Success(sortedSupport)
                }
            }
        }
    }

    fun updateCardViewMode(viewMode: String) {
        _cardViewMode.update {
            viewMode
        }
    }

    private var _searchDeckResult = MutableStateFlow<Async<List<ExternalDeck>>>(Async.Loading)
    private var _searchCardResult =
        MutableStateFlow<Async<List<ExternalCardWithContentAndDefinitions>>>(Async.Loading)
    private var _searchLoading = MutableStateFlow(false)
    private var _searchError = MutableStateFlow(false)

    val searchResultUiState: StateFlow<SearchResultUiState> = combine(
        _searchDeckResult, _searchCardResult, _searchLoading, _searchError
    ) { searchDeckResult, searchCardResult, searchLoading, searchError ->
        var isSearchingError = searchDeckResult is Async.Error && searchCardResult is Async.Error
        var isSearchingLoading =
            searchDeckResult is Async.Loading || searchCardResult is Async.Loading
        var foundDesks = (searchDeckResult as? Async.Success)?.data ?: emptyList()
        var foundCards = (searchCardResult as? Async.Success)?.data ?: emptyList()
        SearchResultUiState(
            foundDecks = foundDesks,
            foundCards = foundCards,
            isLoading = isSearchingLoading,
            isError = isSearchingError
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SearchResultUiState()
    )


    private var _deckWithAllCards =
        MutableStateFlow<UiState<ExternalDeckWithCardsAndContentAndDefinitions>>(UiState.Loading)
    val deckWithAllCards: StateFlow<UiState<ExternalDeckWithCardsAndContentAndDefinitions>> =
        _deckWithAllCards.asStateFlow()
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
                repository.getExternalDeckWithCardsAndContentAndDefinitions(deckId, context)
                    .collect {
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
    fun getSubdecks(deckId: String, sortPref: String) {
        fetchSubdeckJob?.cancel()
        _subdecks.value = UiState.Loading
        fetchSubdeckJob = viewModelScope.launch {
            try {
                repository.getSubdecks(deckId).collect {
                    if (it.isEmpty()) {
                        _subdecks.value = UiState.Error("No subdeck found")
                    } else {
                        actualDeckSubdecks = it
                        val sortedSupport = applyDeckSortPref(sortPref, it)
                        _subdecks.value = UiState.Success(sortedSupport)
                    }
                }
            } catch (e: IOException) {
                _subdecks.value = UiState.Error(e.message.toString())
            }
        }
    }

//    var _data = MutableStateFlow<UiState<CardFragmentUiState>>(UiState.Loading)
//    val data: StateFlow<UiState<CardFragmentUiState>> = _data.asStateFlow()
//
//    fun getDeckWithDecksAndCards(deckId: String, context: Context, deckSortPref: String) {
//        fetchJob?.cancel()
//        _subdecks.value = UiState.Loading
//        _deckWithAllCards.value = UiState.Loading
//        fetchJob = viewModelScope.launch {
//            try {
//                repository.getSubdecks(deckId).collect { decks ->
//                    _subdecks.value = UiState.Success(decks)
//                    repository.getExternalDeckWithCardsAndContentAndDefinitions(deckId, context)
//                        .collect { cards ->
//                            _deckWithAllCards.value = UiState.Success(cards)
//                            _data.value =
//                                UiState.Success(CardFragmentUiState(subdecks, deckWithAllCards))
//                        }
//                }
//
//            } catch (e: okio.IOException) {
//
//            }
//        }
//    }

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

    suspend fun getCards(deckId: String, context: Context) = repository.getCards(deckId, context)

    suspend fun getDeckAndSubdecksCards(deckId: String) = repository.getDeckAndSubdecksCards(deckId)


    fun getBoxLevels(): List<ImmutableSpaceRepetitionBox> {
        return spaceRepetitionHelper.getActualBoxLevels()
    }

    fun getActualDeck() = actualDeck
    fun getActualDeckSubdecks() = actualDeckSubdecks


    suspend fun getMainDeck() = repository.getMainDeck()

    fun insertCards(cards: List<CardWithContentAndDefinitions>, externalDeck: ExternalDeck) =
        viewModelScope.launch {
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

//    private var _foundCards =
//        MutableStateFlow<UiState<List<ExternalCardWithContentAndDefinitions>>>(UiState.Loading)
//    val foundCards: StateFlow<UiState<List<ExternalCardWithContentAndDefinitions>>> =
//        _foundCards.asStateFlow()
//    var searchCardJob: Job? = null
//    fun searchCard(searchQuery: String, context: Context) {
//        searchCardJob?.cancel()
//        _foundCards.value = UiState.Loading
//        searchCardJob = viewModelScope.launch {
//            try {
//                repository.searchCard(searchQuery, context).collect {
//                    if (it.isEmpty()) {
//                        _foundCards.value = UiState.Error("No cards found")
//                    } else {
//                        _foundCards.value = UiState.Success(it)
//                    }
//                }
//            } catch (e: okio.IOException) {
//                _foundCards.value = UiState.Error(e.message.toString())
//            }
//        }
//    }

    fun searchCard(searchQuery: String, context: Context) {
        viewModelScope.launch {
            _searchCardResult.value = Async.Loading
            repository.searchCard(searchQuery, context).collect {
                _searchCardResult.value = if (it.isEmpty()) {
                    Async.Error(true)
                } else {
                    Async.Success(it)
                }
            }
        }
    }

//    private var _foundDecks = MutableStateFlow<UiState<List<ExternalDeck>>>(UiState.Loading)
//    val foundDecks: StateFlow<UiState<List<ExternalDeck>>> = _foundDecks.asStateFlow()
//    var searchDeckJob: Job? = null
//    fun searchDeck(searchQuery: String) {
//        searchDeckJob?.cancel()
//        _foundDecks.value = UiState.Loading
//        searchDeckJob = viewModelScope.launch {
//            try {
//                repository.searchDeck(searchQuery).collect {
//                    if (it.isEmpty()) {
//                        _foundDecks.value = UiState.Error("No decks found")
//                    } else {
//                        _foundDecks.value = UiState.Success(it)
//                    }
//                }
//            } catch (e: okio.IOException) {
//                _foundDecks.value = UiState.Error(e.message.toString())
//            }
//        }
//    }

    fun searchDeck(searchQuery: String) {
        viewModelScope.launch {
            _searchDeckResult.value = Async.Loading
            repository.searchDeck(searchQuery).collect {
                _searchDeckResult.value = if (it.isEmpty()) {
                    Async.Error(true)
                } else {
                    Async.Success(it)
                }
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

    fun updateDefaultCardDefinitionLanguage(deckId: String, language: String) =
        viewModelScope.launch {
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

    fun sortCardsAlphabetically(cards: List<ExternalCardWithContentAndDefinitions>): List<ExternalCardWithContentAndDefinitions> {
        return cards.sortedBy { it.contentWithDefinitions.content.contentText }
    }

    fun sortCardsByCreationDate(cards: List<ExternalCardWithContentAndDefinitions>) = cards

    fun sortCardsByLevel(cards: List<ExternalCardWithContentAndDefinitions>): List<ExternalCardWithContentAndDefinitions> {
        return cards.sortedBy { it.card.cardLevel }
    }

    fun applyCardsSortPref(
        sortPref: String,
        cards: List<ExternalCardWithContentAndDefinitions>
    ): List<ExternalCardWithContentAndDefinitions> {
        return when (sortPref) {
            SORT_CARD_ALPHABETICALLY -> sortCardsAlphabetically(cards)
            SORT_CARD_BY_CREATION_DATE -> sortCardsByCreationDate(cards)
            SORT_CARD_BY_LEVEL -> sortCardsByLevel(cards)
            else -> cards
        }
    }

    fun sortDeckByCreationDate(decks: List<ExternalDeck>) = decks
    fun sortDeckByCardSum(decks: List<ExternalDeck>): List<ExternalDeck> {
        return decks.sortedBy { it.cardCount }
    }

    fun sortDeckAlphabetically(decks: List<ExternalDeck>): List<ExternalDeck> {
        return decks.sortedBy { it.deckName.lowercase() }
    }

    fun applyDeckSortPref(sortPref: String, decks: List<ExternalDeck>): List<ExternalDeck> {
        return when (sortPref) {
            DECK_SORT_ALPHABETICALLY -> sortDeckAlphabetically(decks)
            DECK_SORT_BY_CARD_SUM -> sortDeckByCardSum(decks)
            DECK_SORT_BY_CREATION_DATE -> sortDeckByCreationDate(decks)
            else -> decks
        }
    }

    suspend fun getDeckWithCardsOnStartQuiz(deckId: String) =
        repository.getDeckWithCardsOnStartQuiz(deckId)

}

class CardViewModelFactory(private val repository: FlashCardRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(repository) as T
        }
        throw throw IllegalArgumentException("Unknown ViewModel class")
    }
}