package com.ssoaharison.recall.card

import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.PhotoModel

data class DefinitionFieldModel (
    var definitionId: String,
    var definitionText: String?,
    var definitionImage: PhotoModel?,
    var definitionAudio: AudioModel?,
    var isCorrectDefinition: Boolean,
    var hasFocus: Boolean,
)