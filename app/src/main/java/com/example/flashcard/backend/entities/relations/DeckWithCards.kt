package com.example.flashcard.backend.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck

data class DeckWithCards (
    @Embedded val deck: Deck,
    @Relation(
        parentColumn = "deckId",
        entityColumn = "deckId"
    )
    val cards: List<Card>
)