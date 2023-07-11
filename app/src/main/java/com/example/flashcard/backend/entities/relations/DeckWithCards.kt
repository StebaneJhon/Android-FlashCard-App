package com.example.flashcard.backend.entities.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeckWithCards (
    @Embedded val deck: Deck,
    @Relation(
        parentColumn = "deckId",
        entityColumn = "deckId"
    )
    val cards: List<Card>
): Parcelable