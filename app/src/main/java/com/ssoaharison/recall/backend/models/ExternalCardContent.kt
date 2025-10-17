package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import com.ssoaharison.recall.help.AudioModel
import com.ssoaharison.recall.help.ImageModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardContent(
    val contentId: String,
    val cardOwnerId: String,
    val deckOwnerId: String,
    val contentText: String?,
    val contentImage: ImageModel?,
    val contentAudio: AudioModel?
): Parcelable
