package com.ssoaharison.recall.quiz.test

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestCardDefinitionModel(
    val definitionId: Int?,
    val attachedCardId: String,
    val cardId: String,
    val definition: String,
    val cardType: String,
    val isCorrect: Int,
    var isSelected: Boolean = false
): Parcelable
