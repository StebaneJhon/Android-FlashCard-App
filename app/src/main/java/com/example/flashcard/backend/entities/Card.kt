package com.example.flashcard.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Card (
    @PrimaryKey(autoGenerate = true) val cardId: Int?,
    @ColumnInfo(name = "card_content") val cardContent: String?,
    @ColumnInfo(name = "content_definition") val contentDescription: String?,
    @ColumnInfo(name = "card_value") val cardDefinition: String?,
    @ColumnInfo(name = "value_definition") val valueDefinition: String?,
    @ColumnInfo(name = "deckId") var deckId: Int?,
    @ColumnInfo(name = "background_img", defaultValue = "") val backgroundImg: String?,
    @ColumnInfo(name = "is_favorite", defaultValue = "false") val isFavorite: Boolean? = false,
    @ColumnInfo(name = "revision_time", defaultValue = "0") val revisionTime: Int? = 0,
    @ColumnInfo(name = "missed_time", defaultValue = "0") val missedTime: Int? = 0
): Parcelable