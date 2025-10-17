package com.ssoaharison.recall.quiz.quizGame

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizGameCardDefinitionModel (
    val definitionId: String,
    val cardId: String,
    val definition: String,
    val cardType: String,
    val isCorrect: Int,
    var isSelected: Boolean = false,
    var position: Int? = null
): Parcelable {
    fun giveFeedbackOnSelected() = isSelected && isCorrect == 1
    fun ifCorrectIsSelected() = isCorrect == 1 && !isSelected
}