package com.example.flashcard.deck

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpenTriviaQuizModel(
    var deckName: String,
    var number: Int = 10,
    var category: Int = 0,
    var difficulty: String = "",
    var type: String = "",
): Parcelable