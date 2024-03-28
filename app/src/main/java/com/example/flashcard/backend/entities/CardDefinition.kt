package com.example.flashcard.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class CardDefinition(
    @PrimaryKey(autoGenerate = true) val definitionId: Int?,
    @ColumnInfo(name = "cardId") val cardId: Int?,
    @ColumnInfo(name = "contentId") val contentId: Int?,
    @ColumnInfo(name = "definition") val definition: String?,
    @ColumnInfo(name = "isCorrectDefinition") val isCorrectDefinition: Boolean?,
): Parcelable
