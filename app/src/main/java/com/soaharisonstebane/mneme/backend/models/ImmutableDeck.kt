package com.soaharisonstebane.mneme.backend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImmutableDeck (
    val deckId: String = "",
    val deckName: String? = "",
    val deckDescription: String? = "",
    val cardContentDefaultLanguage: String? = "",
    val cardDefinitionDefaultLanguage: String? = "",
    val deckColorCode: String? = "",
    val cardSum: Int? = 0,
    val knownCardCount: Int? = 0,
    val unKnownCardCount: Int? = 0,
    val deckCategory: String? = "",
    val isFavorite: Boolean? = false,
): Parcelable