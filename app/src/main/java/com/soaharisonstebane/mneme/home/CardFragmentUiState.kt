package com.soaharisonstebane.mneme.home

import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalDeck
import com.soaharisonstebane.mneme.util.ItemLayoutManager.LINEAR

data class CardFragmentUiState(
    val decks: List<ExternalDeck> = emptyList(),
    val cards:  List<ExternalCardWithContentAndDefinitions> = emptyList(),
    val cardsViewMode: String = LINEAR,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
) {
    val isDeck = decks.isNotEmpty()
    val isCard = cards.isNotEmpty()
}
