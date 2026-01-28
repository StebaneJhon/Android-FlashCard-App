package com.ssoaharison.recall.quiz.flashCardGame

import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions

data class FlashCardCardModel(
    val card: ExternalCardWithContentAndDefinitions,
    var isActualOrPassed: Boolean = false,
) {
    fun setAsActualOrPassed() {
        isActualOrPassed = true
    }
    fun setAsNotActualOrNotPassed() {
        isActualOrPassed = false
    }
}
