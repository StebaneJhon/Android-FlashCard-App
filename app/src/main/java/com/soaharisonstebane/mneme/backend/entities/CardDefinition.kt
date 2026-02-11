package com.soaharisonstebane.mneme.backend.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class CardDefinition(
    @PrimaryKey(autoGenerate = false) val definitionId: String,
    val cardOwnerId: String,
    val deckOwnerId: String?,
    val contentOwnerId: String,
    val isCorrectDefinition: Int,
    val definitionText: String?,
    val definitionImageName: String?,
    val definitionAudioName: String?,
    val definitionAudioDuration: String?
): Parcelable
