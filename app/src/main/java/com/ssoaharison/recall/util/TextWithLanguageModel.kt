package com.ssoaharison.recall.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextWithLanguageModel (
    val cardId: String,
    val text: String,
    val textType: String,
    val language: String?,
): Parcelable