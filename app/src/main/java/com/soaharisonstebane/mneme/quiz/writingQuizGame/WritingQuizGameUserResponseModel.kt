package com.soaharisonstebane.mneme.quiz.writingQuizGame

import com.soaharisonstebane.mneme.util.TextWithLanguageModel

data class WritingQuizGameUserResponseModel(
    val cardId: String,
    val userAnswer: String,
    val correctAnswer: List<TextWithLanguageModel>,
)
