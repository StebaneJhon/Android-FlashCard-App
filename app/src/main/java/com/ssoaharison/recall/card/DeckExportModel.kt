package com.ssoaharison.recall.card

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeckExportModel(
    val format: String,
    val separator: String
): Parcelable
