package com.soaharisonstebane.mneme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.helper.cardToText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update

data class ExportDeckUiState(
    val preview: String = "",
    val separator: String = ":"
)

class ExportDeckViewModel: ViewModel() {

    private var _preview = MutableStateFlow<String>("")
    private var _separator = MutableStateFlow<String>(":")

    val uiState: StateFlow<ExportDeckUiState> = combine(
        _preview, _separator
    ) { preview, separator ->
        ExportDeckUiState(
            preview = preview,
            separator = separator
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ExportDeckUiState(
            preview = "",
            separator = ":"
        )
    )

    fun setPreviewText(cards: List<ExternalCardWithContentAndDefinitions>, separator: String) {
        _preview.update { "" }
        _preview.update { text ->
            var result = text
            cards.forEach { card ->
                result += cardToText(card, separator)
            }
            result
        }
    }

    fun onSeparatorChanged(separator: String, cards: List<ExternalCardWithContentAndDefinitions>) {
        _separator.update { separator }
        setPreviewText(cards, separator)
    }


}