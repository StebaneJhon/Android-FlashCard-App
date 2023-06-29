package com.example.flashcard.backend.Model

data class ImmutableCard(
    val cardId: Int? = null,
    val cardContent: String? = "",
    val contentDescription: String? = "",
    val cardDefinition: String? = "",
    val valueDefinition: String? = "",
    val deckId: Int? = null
)
