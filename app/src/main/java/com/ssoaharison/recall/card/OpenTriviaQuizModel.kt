package com.ssoaharison.recall.card

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpenTriviaQuizModel(
    var number: Int = 10,
    var category: Int = 0,
    var difficulty: String = "",
    var type: String = "",
): Parcelable