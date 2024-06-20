package com.example.flashcard.quiz.testQuizGame

import android.content.res.ColorStateList
import android.view.View

data class TestQuizSpeakModel(
    val text: List<String>,
    val views: List<View>,
    val originalTextColor: ColorStateList,
    val language: String,
)
