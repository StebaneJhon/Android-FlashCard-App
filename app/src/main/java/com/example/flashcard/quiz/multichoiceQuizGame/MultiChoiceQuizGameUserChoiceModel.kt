package com.example.flashcard.quiz.multichoiceQuizGame

import com.google.android.material.card.MaterialCardView

data class MultiChoiceQuizGameUserChoiceModel(
    val answer: List<String>,
    val userChoice: String,
    val cvCard: MaterialCardView,
    val cvCardOnWrongAnswer: MaterialCardView,
)
