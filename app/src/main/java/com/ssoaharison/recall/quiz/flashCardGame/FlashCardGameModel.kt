package com.ssoaharison.recall.quiz.flashCardGame

import com.ssoaharison.recall.backend.Model.ImmutableCard

data class FlashCardGameModel (
    val top: ImmutableCard,
    val bottom: ImmutableCard?
)