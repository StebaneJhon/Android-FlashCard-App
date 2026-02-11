package com.soaharisonstebane.mneme.card

import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel

data class DefinitionFieldModel (
    var definitionId: String,
    var definitionText: String?,
    var definitionImage: PhotoModel?,
    var definitionAudio: AudioModel?,
    var isCorrectDefinition: Boolean,
    var hasFocus: Boolean,
)