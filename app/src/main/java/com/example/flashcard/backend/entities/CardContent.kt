package com.example.flashcard.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class CardContent(
    @PrimaryKey(autoGenerate = true) val contentId: Int?,
    @ColumnInfo(name = "cardId") val cardId: Int?,
    @ColumnInfo(name = "content") val content: String?,
): Parcelable
