package com.example.flashcard.quiz.quizGame

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizGameCardDefinitionModel (
    val definitionId: Int?,
    val cardId: String,
    val definition: String,
    val cardType: String,
    val isCorrect: Int,
    var isSelected: Boolean = false
): Parcelable