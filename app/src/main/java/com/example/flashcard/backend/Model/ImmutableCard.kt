package com.example.flashcard.backend.Model

import android.os.Parcelable
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImmutableCard(
    val cardId: Int? = null,
    val cardContent: CardContent? = null,
    val contentDescription: String? = "",
    val cardDefinition: List<CardDefinition>? = null,
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
    val creationDateTime: String? = null
): Parcelable
