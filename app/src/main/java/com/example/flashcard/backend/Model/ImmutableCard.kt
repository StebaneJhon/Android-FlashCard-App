package com.example.flashcard.backend.Model

import com.example.flashcard.backend.entities.CardDefinition

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
    val missedTime: Int? = 0,
    val creationDate: String? = null,
    val lastRevisionDate: String? = null,
    val cardStatus: String? = null,
    val nextMissMemorisationDate: String? = null,
    val nextRevisionDate: String? = null,
    val cardType: String? = null,
    val cardDefinitions: List<CardDefinition>? = null
)
