package com.soaharisonstebane.mneme.card

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardImportFromDeviceModel(
    val format: String,
    val separator: String
): Parcelable
