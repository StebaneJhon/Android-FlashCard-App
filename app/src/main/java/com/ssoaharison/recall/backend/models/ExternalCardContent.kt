package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.PhotoModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardContent(
    val contentId: String,
    val cardOwnerId: String,
    val deckOwnerId: String,
    val contentText: String?,
    val contentImage: PhotoModel?,
    val contentAudio: AudioModel?
): Parcelable
