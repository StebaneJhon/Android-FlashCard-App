package com.ssoaharison.recall.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class CardContent(
    @PrimaryKey(autoGenerate = false) val contentId: String,
    @ColumnInfo(name = "cardId") var cardId: String,
    @ColumnInfo(name = "deckId") var deckId: String?,
    @ColumnInfo(name = "content") val content: String,
): Parcelable
