package com.soaharisonstebane.mneme.backend.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Card (
    @PrimaryKey(autoGenerate = false) val cardId: String,
    val deckOwnerId: String,
    val cardLevel: String?,
    val cardType: String?,
    val revisionTime: Int?,
    val missedTime: Int?,
    val creationDate: String?,
    val lastRevisionDate: String?,
    val nextMissMemorisationDate: String?,
    val nextRevisionDate: String?,
    val cardContentLanguage: String?,
    val cardDefinitionLanguage: String?,
): Parcelable