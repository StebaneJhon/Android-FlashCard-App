package com.ssoaharison.recall.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Deck (
    @PrimaryKey(autoGenerate = false) val deckId: String,
    @ColumnInfo(name = "deck_name") val deckName: String?,
    @ColumnInfo(name = "deck_description") val deckDescription: String?,
    @ColumnInfo(name = "deck_first_language") val deckFirstLanguage: String?,
    @ColumnInfo(name = "deck_second_language") val deckSecondLanguage: String?,
    @ColumnInfo(name = "deck_color_code") val deckColorCode: String?,
    @ColumnInfo(name = "card_sum") var cardSum: Int?,
    @ColumnInfo(name = "deck_category") val deckCategory: String?,
    @ColumnInfo(name = "is_favorite") val isFavorite: Int?,
): Parcelable