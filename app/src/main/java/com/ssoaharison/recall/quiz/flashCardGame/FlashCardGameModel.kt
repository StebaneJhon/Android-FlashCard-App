package com.ssoaharison.recall.quiz.flashCardGame

import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ImmutableCard

data class FlashCardGameModel (
    val top: ExternalCardWithContentAndDefinitions,
    val bottom: ExternalCardWithContentAndDefinitions?
)