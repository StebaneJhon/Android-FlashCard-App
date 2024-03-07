package com.example.flashcard.backend.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.sql.Date

@Parcelize
data class ImmutableUser(
    val name: String = "Anonymous",
    val initial: Char = name[1],
    val status: String,
    val creation: String?,
): Parcelable
