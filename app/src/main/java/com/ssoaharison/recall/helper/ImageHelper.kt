package com.ssoaharison.recall.helper

import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun loadPhotoFromInternalStorage(context: Context, filename: String): PhotoModel {
    return withContext(Dispatchers.IO) {
        val file = File(context.filesDir, filename)
        val bytes = file.readBytes()
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        PhotoModel(file.name, bmp)
    }
}