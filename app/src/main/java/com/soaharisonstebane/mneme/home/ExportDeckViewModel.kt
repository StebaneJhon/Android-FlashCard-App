package com.soaharisonstebane.mneme.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soaharisonstebane.mneme.backend.FlashCardRepository
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.helper.cardToText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExportDeckUiState(
    val preview: String = "",
    val separator: String = ":",
    val includeSubdecks: Boolean = false
)



class ExportDeckViewModel(private val repository: FlashCardRepository): ViewModel() {

    private var _preview = MutableStateFlow<String>("")
    private var _separator = MutableStateFlow<String>(":")

    private var _includeSubdecks = MutableStateFlow<Boolean>(false)

    val uiState: StateFlow<ExportDeckUiState> = combine(
        _preview, _separator, _includeSubdecks
    ) { preview, separator, includeSubdecks ->
        ExportDeckUiState(
            preview = preview,
            separator = separator,
            includeSubdecks = includeSubdecks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ExportDeckUiState(
            preview = "",
            separator = ":",
            includeSubdecks = false
        )
    )

    fun initCardsToExport(context: Context, deckId: String) {
        updateCardsToExport(context, deckId, false)
    }

    fun onIncludeSubdecksChanged(context: Context, deckId: String, includeSubdecks: Boolean) {
        _includeSubdecks.update { includeSubdecks }
        updateCardsToExport(context, deckId, includeSubdecks)
    }


    fun setPreviewText(cards: List<ExternalCardWithContentAndDefinitions>, separator: String) {
        _preview.update { "" }
        _preview.update { text ->
            var result = text
            cards.forEach { card ->
                result += cardToText(card, separator)
            }
            if (cards.size >= 5) {
                "$result...\n"
            } else {
                result
            }
        }
    }

    fun onSeparatorChanged(separator: String) {
        _separator.update { separator }

        setPreviewText(getPreviewCards(), separator)
    }

    val _cards = MutableStateFlow<List<ExternalCardWithContentAndDefinitions>>(emptyList())
    val cards: StateFlow<List<ExternalCardWithContentAndDefinitions>> = _cards.asStateFlow()

    suspend fun getDeckAndSubdecksCards(deckId: String) = repository.getDeckAndSubdecksCards(deckId)

    suspend fun getDeckCards(deckId: String, context: Context) = repository.getCards(deckId, context)

    fun updateCardsToExport(context: Context, deckId: String, includerSubdecks: Boolean) {
        viewModelScope.launch {
            if (includerSubdecks) {
                _cards.update { getDeckAndSubdecksCards(deckId) }
            } else {
                _cards.update { getDeckCards(deckId, context) }
            }
            setPreviewText(getPreviewCards(), _separator.value)
        }
    }

    private fun getPreviewCards() =  if (cards.value.size > 5) {
        cards.value.subList(0, 5)
    } else {
        cards.value
    }

}

class ExportDeckViewModelFactory(private val repository: FlashCardRepository): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportDeckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportDeckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}