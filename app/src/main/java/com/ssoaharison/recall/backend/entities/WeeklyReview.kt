package com.ssoaharison.recall.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class WeeklyReview(
    @PrimaryKey(autoGenerate = true) val dayId: Int?,
    @ColumnInfo(name = "dayName") val dayName: String?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "revisedCardSum") val revisedCardSum: Int?,
    @ColumnInfo(name = "colorGrade") val colorGrade: Int?,
    ): Parcelable
