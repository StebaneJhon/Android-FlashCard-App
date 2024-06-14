package com.example.flashcard.quiz.multichoiceQuizGame

import android.content.res.ColorStateList
import android.view.View

data class SpeakModel(
    val text: List<String>,
    val views: List<View>,
    val originalTextColor: ColorStateList
)
