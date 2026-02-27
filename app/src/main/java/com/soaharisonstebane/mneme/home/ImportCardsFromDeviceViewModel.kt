package com.soaharisonstebane.mneme.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soaharisonstebane.mneme.backend.entities.relations.CardWithContentAndDefinitions
import com.soaharisonstebane.mneme.helper.textToImmutableCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.InputStreamReader

data class ImportCardsFromDeviceUiState(
    val cards: List<CardWithContentAndDefinitions> = emptyList(),
    val separator: String = ":",
    val isLoading: Boolean = false,
)

class ImportCardsFromDeviceViewModel: ViewModel() {

    private var _cards = MutableStateFlow<List<CardWithContentAndDefinitions>>(emptyList())
    var _separator = MutableStateFlow(":")

    var _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<ImportCardsFromDeviceUiState> = combine(
        _cards, _separator, _isLoading
    ) { cards, separator, isLoading ->
        ImportCardsFromDeviceUiState(
            cards = cards,
            separator = separator,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ImportCardsFromDeviceUiState(
            cards = emptyList(),
            separator = ":",
            isLoading = false
        )
    )

    fun updateSeparator(separator: String) {
        _separator.update { separator }
    }

    fun onSeparatorChanged(separator: String, uri: Uri, deckId: String, context: Context) {
        _isLoading.update { true }
        _cards.update { textFromUriToImmutableCards(deckId, uri, separator, context) }
        _isLoading.update { false }
    }

    private fun textFromUriToImmutableCards(
        deckId: String,
        uri: Uri,
        separator: String,
        context: Context,
    ): List<CardWithContentAndDefinitions> {
        val result: MutableList<CardWithContentAndDefinitions> = mutableListOf()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    val card = textToImmutableCard(line, separator, deckId)
                    result.add(card)
                }
            }
        }
        return result
    }

}