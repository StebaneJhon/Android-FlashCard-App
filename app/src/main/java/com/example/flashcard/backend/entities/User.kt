package com.example.flashcard.backend.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "initial") val initial: String?,
    @ColumnInfo(name = "status") val status: String?
): Parcelable
