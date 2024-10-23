package com.ssoaharison.recall.backend.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardDefinition

data class CardWithDefinitions(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "cardId"
    )
    val definition: List<CardDefinition>
)