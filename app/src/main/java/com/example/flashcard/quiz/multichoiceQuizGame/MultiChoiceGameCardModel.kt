package com.example.flashcard.quiz.multichoiceQuizGame

data class MultiChoiceGameCardModel(
    val onCardWord: String,
    val answer: String,
    val alternative1: String,
    val alternative2: String,
    val alternative3: String,
    val alternative4: String
)