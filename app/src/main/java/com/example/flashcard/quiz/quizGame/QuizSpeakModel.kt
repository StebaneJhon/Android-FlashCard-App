package com.example.flashcard.quiz.quizGame

import android.view.View

data class QuizSpeakModel(
    val text: List<String>,
    val views: List<View>,
    val language: String
)
