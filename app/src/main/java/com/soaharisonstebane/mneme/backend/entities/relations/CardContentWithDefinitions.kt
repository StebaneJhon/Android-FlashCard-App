package com.soaharisonstebane.mneme.backend.entities.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.soaharisonstebane.mneme.backend.entities.CardContent
import com.soaharisonstebane.mneme.backend.entities.CardDefinition
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardContentWithDefinitions(
    @Embedded val content: CardContent,
    @Relation(
        parentColumn = "contentId",
        entityColumn = "contentOwnerId"
    )
    val definitions: List<CardDefinition>
): Parcelable
