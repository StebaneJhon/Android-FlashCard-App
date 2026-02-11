package com.soaharisonstebane.mneme.helper

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioModel(
    val name: String,
//    val file: File,
    val duration: String
): Parcelable