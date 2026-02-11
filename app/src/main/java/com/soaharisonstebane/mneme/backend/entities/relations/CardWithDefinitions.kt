package com.soaharisonstebane.mneme.backend.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.CardDefinition

data class CardWithDefinitions(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "cardOwnerId"
    )
    val definition: List<CardDefinition>
)