package com.ssoaharison.recall.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextWithLanguageModel (
    val text: String,
    val language: String,
): Parcelable