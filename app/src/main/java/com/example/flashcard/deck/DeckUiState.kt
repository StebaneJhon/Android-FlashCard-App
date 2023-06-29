package com.example.flashcard.deck

import com.example.flashcard.backend.Model.ImmutableDeck

data class DeckUiState (
    val isLoading: Boolean = true,
    val errorMessage: String = "",
    val deckList: List<ImmutableDeck> = listOf()
)