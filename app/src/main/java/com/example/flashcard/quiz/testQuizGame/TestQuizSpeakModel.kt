package com.example.flashcard.quiz.testQuizGame

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button

data class TestQuizSpeakModel(
    val text: List<String>,
    val views: List<View>,
    val language: String,
    val speakButton: Button,
)
