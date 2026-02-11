package com.soaharisonstebane.mneme.backend.models

import android.os.Parcelable
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardContent(
    val contentId: String,
    val cardOwnerId: String,
    val deckOwnerId: String,
    val contentText: String?,
    val contentImage: PhotoModel?,
    val contentAudio: AudioModel?,
): Parcelable
