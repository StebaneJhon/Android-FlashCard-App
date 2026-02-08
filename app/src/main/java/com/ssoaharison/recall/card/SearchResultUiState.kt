package com.ssoaharison.recall.card

import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck

data class SearchResultUiState(
    val foundDecks: List<ExternalDeck> = emptyList(),
    val foundCards: List<ExternalCardWithContentAndDefinitions> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
) {
    val isDeck = foundDecks.isNotEmpty()
    val isCard = foundCards.isNotEmpty()
}