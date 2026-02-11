package com.ssoaharison.recall.backend.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Deck (
    @PrimaryKey(autoGenerate = false) val deckId: String,
    val parentDeckId: String?,
    val deckName: String,
    val deckDescription: String?,
    val cardContentDefaultLanguage: String?,
    val cardDefinitionDefaultLanguage: String?,
    val deckBackground: String?,
    val deckCategory: String?,
    val isFavorite: Int?,
    val deckCreationDate: String?
): Parcelable