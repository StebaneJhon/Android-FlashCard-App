package com.soaharisonstebane.mneme.card

import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalDeck

data class SearchResultUiState(
    val foundDecks: List<ExternalDeck> = emptyList(),
    val foundCards: List<ExternalCardWithContentAndDefinitions> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
) {
    val isDeck = foundDecks.isNotEmpty()
    val isCard = foundCards.isNotEmpty()
}