package com.soaharisonstebane.mneme.card

import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel

data class ContentFieldModel (
    var contentId: String?,
    var contentText: String?,
    var contentImage: PhotoModel?,
    var contentAudio: AudioModel?,
    var hasFocus: Boolean,
)