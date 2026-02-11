package com.soaharisonstebane.mneme.quiz.test

import android.os.Parcelable
import com.soaharisonstebane.mneme.util.TextWithLanguageModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestCardDefinitionModel(
    val definitionId: Int?,
    val attachedCardId: String,
    val cardId: String,
    val definition: TextWithLanguageModel,
    val cardType: String,
    val isCorrect: Int,
    var isSelected: Boolean = false
): Parcelable
