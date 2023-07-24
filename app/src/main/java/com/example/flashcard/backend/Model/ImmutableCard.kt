package com.example.flashcard.backend.Model

data class ImmutableCard(
    val cardId: Int? = null,
    val cardContent: String? = "",
    val contentDescription: String? = "",
    val cardDefinition: String? = "",
    val valueDefinition: String? = "",
    val deckId: Int? = null,
    val backgroundImg: String? = "",
    val isFavorite: Boolean? = false,
    val revisionTime: Int? = 0,
    val missedTime: Int? = 0
)
