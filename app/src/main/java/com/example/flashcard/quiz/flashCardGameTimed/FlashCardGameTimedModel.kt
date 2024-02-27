package com.example.flashcard.quiz.flashCardGameTimed

import com.example.flashcard.backend.Model.ImmutableCard

data class FlashCardGameTimedModel(
    val top: ImmutableCard,
    val bottom: ImmutableCard?
)
