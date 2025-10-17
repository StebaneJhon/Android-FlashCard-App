package com.ssoaharison.recall.backend.models

import android.os.Parcelable
import com.ssoaharison.recall.help.AudioModel
import com.ssoaharison.recall.help.ImageModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCardDefinition (
    val definitionId: String,
    val cardOwnerId: String,
    val deckOwnerId: String?,
    val contentOwnerId: String,
    val isCorrectDefinition: Int,
    val definitionText: String?,
    val definitionImage: ImageModel?,
    val definitionAudio: AudioModel?,
): Parcelable