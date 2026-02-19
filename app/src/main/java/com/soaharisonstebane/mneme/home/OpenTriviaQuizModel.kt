package com.soaharisonstebane.mneme.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpenTriviaQuizModel(
    var number: Int = 10,
    var category: Int = 0,
    var difficulty: String = "",
    var type: String = "",
): Parcelable