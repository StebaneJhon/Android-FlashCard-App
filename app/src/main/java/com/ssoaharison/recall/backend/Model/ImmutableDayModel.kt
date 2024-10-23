package com.ssoaharison.recall.backend.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImmutableDayModel(
    val dayName: String = "",
    val date: String = "",
    val revisedCardSum: Int = 0,
    var colorGrade: Int = 50
): Parcelable
