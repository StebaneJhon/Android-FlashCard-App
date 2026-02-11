package com.soaharisonstebane.mneme.quiz.flashCardGame

import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions

data class FlashCardGameModel (
    val top: ExternalCardWithContentAndDefinitions,
    val bottom: ExternalCardWithContentAndDefinitions?
)