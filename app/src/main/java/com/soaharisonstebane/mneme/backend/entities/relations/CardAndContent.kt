package com.soaharisonstebane.mneme.backend.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.CardContent

data class CardAndContent(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "cardOwnerId"
    )
    val cardContent: CardContent
)
