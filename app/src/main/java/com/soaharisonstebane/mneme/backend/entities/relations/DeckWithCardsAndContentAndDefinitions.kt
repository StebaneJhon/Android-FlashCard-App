package com.soaharisonstebane.mneme.backend.entities.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.Deck
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeckWithCardsAndContentAndDefinitions (
    @Embedded val deck: Deck,
    @Relation(
        entity = Card::class,
        parentColumn = "deckId",
        entityColumn = "deckOwnerId"
    )
    val cards: List<CardWithContentAndDefinitions>
): Parcelable