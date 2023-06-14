package com.example.flashcard.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.flashcard.entities.Card
import com.example.flashcard.entities.Deck

data class DeckWithCards (
    @Embedded val deck: Deck,
    @Relation(
        parentColumn = "deckId",
        entityColumn = "deckId"
    )
    val cards: List<Card>
)