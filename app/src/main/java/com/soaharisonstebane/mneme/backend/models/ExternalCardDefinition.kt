package com.soaharisonstebane.mneme.backend.models

import android.os.Parcelable
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardDefinition (
    val definitionId: String,
    val cardOwnerId: String,
    val deckOwnerId: String?,
    val contentOwnerId: String,
    val isCorrectDefinition: Int,
    val definitionText: String?,
    val definitionImage: PhotoModel?,
    val definitionAudio: AudioModel?,
): Parcelable