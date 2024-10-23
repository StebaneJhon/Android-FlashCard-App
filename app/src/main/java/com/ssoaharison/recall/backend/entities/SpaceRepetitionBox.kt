package com.ssoaharison.recall.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class SpaceRepetitionBox(
    @PrimaryKey(autoGenerate = true) val levelId: Int?,
    @ColumnInfo(name = "levelNme") val levelName: String?,
    @ColumnInfo(name = "levelColor") val levelColor: String?,
    @ColumnInfo(name = "levelRepeatIn") val levelRepeatIn: Int?,
    @ColumnInfo(name = "levelRevisionMargin") val levelRevisionMargin: Int?
): Parcelable