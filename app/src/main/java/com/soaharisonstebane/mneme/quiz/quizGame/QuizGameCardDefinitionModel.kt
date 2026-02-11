package com.soaharisonstebane.mneme.quiz.quizGame

import android.os.Parcelable
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizGameCardDefinitionModel (
    val definitionId: String,
    val cardId: String,
    val definition: String?,
    val definitionImage: PhotoModel?,
    val definitionAudio: AudioModel?,
    val cardType: String,
    val isCorrect: Int,
    var isSelected: Boolean = false,
    var position: Int? = null
): Parcelable {
    fun giveFeedbackOnSelected() = isSelected && isCorrect == 1
    fun ifCorrectIsSelected() = isCorrect == 1 && !isSelected
}