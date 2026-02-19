package com.soaharisonstebane.mneme.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeckExportModel(
    val format: String,
    val separator: String,
    val includeSubdecks: Boolean = false
): Parcelable
