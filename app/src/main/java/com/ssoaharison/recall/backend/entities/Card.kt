package com.ssoaharison.recall.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Card (
    @PrimaryKey(autoGenerate = false) val cardId: String,
    @ColumnInfo(name = "deckId") val deckId: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Int?,
    @ColumnInfo(name = "revision_time") val revisionTime: Int?,
    @ColumnInfo(name = "missed_time") val missedTime: Int?,
    @ColumnInfo(name = "creation_date") val creationDate: String?,
    @ColumnInfo(name = "last_revision_date") val lastRevisionDate: String?,
    @ColumnInfo(name = "card_status") val cardStatus: String?,
    @ColumnInfo(name = "next_miss_memorisation_date") val nextMissMemorisationDate: String?,
    @ColumnInfo(name = "next_revision_date") val nextRevisionDate: String?,
    @ColumnInfo(name = "card_type") val cardType: String?,
    @ColumnInfo(name = "card_content_language", defaultValue = "null") val cardContentLanguage: String?,
    @ColumnInfo(name = "card_definition_language", defaultValue = "null") val cardDefinitionLanguage: String?,
): Parcelable