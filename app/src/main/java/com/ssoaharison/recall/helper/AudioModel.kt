package com.ssoaharison.recall.helper

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class AudioModel(
    val name: String,
//    val file: File,
    val duration: String
): Parcelable