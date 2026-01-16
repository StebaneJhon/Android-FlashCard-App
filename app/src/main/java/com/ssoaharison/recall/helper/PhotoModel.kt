package com.ssoaharison.recall.helper

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotoModel(
    val name: String,
    val bmp: Bitmap?
): Parcelable