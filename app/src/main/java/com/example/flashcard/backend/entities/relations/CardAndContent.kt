package com.example.flashcard.backend.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.CardContent

data class CardAndContent(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "cardId"
    )
    val cardContent: CardContent
)