package com.ssoaharison.recall.quiz.flashCardGame

import com.ssoaharison.recall.backend.models.ImmutableCard

data class FlashCardGameModel (
    val top: ImmutableCard,
    val bottom: ImmutableCard?
)