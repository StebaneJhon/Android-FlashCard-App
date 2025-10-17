package com.ssoaharison.recall.help

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageModel(
    val name: String,
    val bmp: Bitmap
): Parcelable
