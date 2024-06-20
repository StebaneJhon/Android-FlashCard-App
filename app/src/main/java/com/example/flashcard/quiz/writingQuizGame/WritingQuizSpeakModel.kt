package com.example.flashcard.quiz.writingQuizGame

import android.content.res.ColorStateList
import android.view.View

data class WritingQuizSpeakModel(
    val text: String,
    val views: View,
    val originalTextColor: ColorStateList
)
