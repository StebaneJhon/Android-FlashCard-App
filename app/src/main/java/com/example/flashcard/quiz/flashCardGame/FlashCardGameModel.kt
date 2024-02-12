package com.example.flashcard.quiz.flashCardGame

import com.example.flashcard.backend.Model.ImmutableCard

data class FlashCardGameModel (
    val top: ImmutableCard,
    val bottom: ImmutableCard?
)