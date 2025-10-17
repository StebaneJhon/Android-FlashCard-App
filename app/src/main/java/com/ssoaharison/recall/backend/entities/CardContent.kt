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
    val cardOwnerId: String,
    val deckOwnerId: String,
    val contentText: String?,
    val contentImageName: String?,
    val contentAudioName: String?
): Parcelable
