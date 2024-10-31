package com.ssoaharison.recall.quiz.writingQuizGame

data class WritingQuizGameModel (
    val cardId: String,
    val onCardWord: String,
    val answer: List<String>
)