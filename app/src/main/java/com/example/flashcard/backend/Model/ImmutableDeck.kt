package com.example.flashcard.backend.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.sql.Date

@Parcelize
data class ImmutableDeck (
    val deckId: String = "",
    val deckName: String? = "",
    val deckDescription: String? = "",
    val deckFirstLanguage: String? = "",
    val deckSecondLanguage: String? = "",
    val deckColorCode: String? = "",
    val cardSum: Int? = 0,
    val deckCategory: String? = "",
    val isFavorite: Boolean? = false,
): Parcelable