package com.example.flashcard.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Card (
    @PrimaryKey(autoGenerate = true) val cardId: Int?,
    @ColumnInfo(name = "card_content") val cardContent: String?,
    @ColumnInfo(name = "card_definition") val cardDefinition: String?,
    @ColumnInfo(name = "deckId") val deckId: Int?,
)