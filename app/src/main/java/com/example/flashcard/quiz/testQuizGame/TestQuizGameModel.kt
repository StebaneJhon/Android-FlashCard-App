package com.example.flashcard.quiz.testQuizGame

import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition

data class TestQuizGameModel(
    val cardType: String?,
    val cardPosition: Int?,
    val content: CardContent?,
    val definition: List<CardDefinition>?,
    val correctDefinition: List<CardDefinition>?,
    val wrongDefinition: List<CardDefinition>?
)