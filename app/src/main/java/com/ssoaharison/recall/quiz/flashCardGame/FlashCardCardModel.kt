package com.ssoaharison.recall.quiz.flashCardGame

import com.ssoaharison.recall.backend.models.ImmutableCard

data class FlashCardCardModel(
    val card: ImmutableCard,
    var isActualOrPassed: Boolean = false,
) {
    fun setAsActualOrPassed() {
        isActualOrPassed = true
    }
    fun setAsNotActualOrNotPassed() {
        isActualOrPassed = false
    }
}
