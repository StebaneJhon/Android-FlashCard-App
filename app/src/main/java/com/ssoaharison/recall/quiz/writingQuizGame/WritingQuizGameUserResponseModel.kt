package com.ssoaharison.recall.quiz.writingQuizGame

import com.google.android.material.card.MaterialCardView

data class WritingQuizGameUserResponseModel(
    val cardId: String,
    val userAnswer: String,
    val correctAnswer: List<String>,
    val cvCardFront: MaterialCardView,
    val cvCardOnWrongAnswer: MaterialCardView,
)
