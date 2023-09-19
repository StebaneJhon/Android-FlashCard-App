package com.example.flashcard.quiz.timedFlashCardGame

import com.example.flashcard.backend.Model.ImmutableCard

data class TimedFlashCardGameModel(
    val top: ImmutableCard,
    val bottom: ImmutableCard?
)