package com.example.flashcard.quiz.testQuizGame

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import com.google.android.material.button.MaterialButton

data class TestQuizSpeakModel(
    val text: List<String>,
    val views: List<View>,
    val language: String
)
