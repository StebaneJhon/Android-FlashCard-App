package com.ssoaharison.recall.card

import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.util.ItemLayoutManager.LINEAR

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
