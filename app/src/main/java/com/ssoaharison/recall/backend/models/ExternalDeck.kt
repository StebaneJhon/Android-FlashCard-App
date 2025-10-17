package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalDeck (
    val deckId: String,
    val parentDeckId: String?,
    val deckName: String,
    val deckDescription: String?,
    val cardContentDefaultLanguage: String?,
    val cardDefinitionDefaultLanguage: String?,
    val deckColorCode: String?,
    val deckCategory: String?,
    val isFavorite: Int?,
    val deckCreationDate: String?,
    val cardCount: Int = 0,
    val knownCardCount: Int = 0,
    val unKnownCardCount: Int = 0
): Parcelable