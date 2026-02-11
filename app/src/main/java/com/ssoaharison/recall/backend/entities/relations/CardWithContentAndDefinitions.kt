package com.ssoaharison.recall.backend.entities.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardContent
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
