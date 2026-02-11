package com.ssoaharison.recall.card

import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.PhotoModel

data class ContentFieldModel (
    var contentId: String?,
    var contentText: String?,
    var contentImage: PhotoModel?,
    var contentAudio: AudioModel?,
    var hasFocus: Boolean,
)