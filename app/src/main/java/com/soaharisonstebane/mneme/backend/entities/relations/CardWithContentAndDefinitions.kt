package com.soaharisonstebane.mneme.backend.entities.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.CardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardWithContentAndDefinitions(
    @Embedded val card: Card,
    @Relation(
        entity = CardContent::class,
        parentColumn = "cardId",
        entityColumn = "cardOwnerId"
    )
    val contentWithDefinitions: CardContentWithDefinitions
): Parcelable
