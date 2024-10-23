package com.ssoaharison.recall.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class CardDefinition(
    @PrimaryKey(autoGenerate = true) val definitionId: Int?,
    @ColumnInfo(name = "cardId") var cardId: String,
    @ColumnInfo(name = "deckId") var deckId: String?,
    @ColumnInfo(name = "contentId") val contentId: String,
    @ColumnInfo(name = "definition") val definition: String,
    @ColumnInfo(name = "isCorrectDefinition") val isCorrectDefinition: Int,
): Parcelable
