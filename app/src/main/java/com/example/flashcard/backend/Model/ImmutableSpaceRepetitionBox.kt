package com.example.flashcard.backend.Model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class ImmutableSpaceRepetitionBox(
    val levelId: Int? = null,
    val levelName: String?,
    val levelColor: String?,
    val levelRepeatIn: Int?,
    val levelRevisionMargin: Int?
)
