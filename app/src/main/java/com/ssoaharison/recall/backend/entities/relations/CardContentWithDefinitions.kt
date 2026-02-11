package com.ssoaharison.recall.backend.entities.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
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
