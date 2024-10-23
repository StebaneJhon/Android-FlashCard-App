package com.ssoaharison.recall.backend.entities.relations

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.Deck
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