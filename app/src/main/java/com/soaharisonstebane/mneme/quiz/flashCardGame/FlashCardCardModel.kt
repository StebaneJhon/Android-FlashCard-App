package com.soaharisonstebane.mneme.quiz.flashCardGame

import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions

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
