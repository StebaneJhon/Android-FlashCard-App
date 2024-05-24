package com.example.flashcard.quiz.testQuizGame

import com.example.flashcard.backend.Model.ImmutableCard

data class ModelCard (
    val cardDetails: ImmutableCard?,
    var isFlipped: Boolean = false,
    var correctAnswerSum: Int = 0,
    var isAnswered: Boolean = false,
)