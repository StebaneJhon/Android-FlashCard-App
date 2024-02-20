package com.example.flashcard.quiz.writingQuizGame

import com.google.android.material.card.MaterialCardView

data class WritingQuizGameUserResponseModel(
    val userAnswer: String,
    val correctAnswer: String,
    val cvCardFront: MaterialCardView,
    val cvCardOnWrongAnswer: MaterialCardView,
)
