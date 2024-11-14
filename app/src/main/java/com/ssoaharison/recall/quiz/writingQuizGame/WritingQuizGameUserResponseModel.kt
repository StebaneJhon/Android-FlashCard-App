package com.ssoaharison.recall.quiz.writingQuizGame

import com.google.android.material.card.MaterialCardView
import com.ssoaharison.recall.util.TextWithLanguageModel

data class WritingQuizGameUserResponseModel(
    val cardId: String,
    val userAnswer: String,
    val correctAnswer: List<TextWithLanguageModel>,
    val cvCardFront: MaterialCardView,
    val cvCardOnWrongAnswer: MaterialCardView,
)
