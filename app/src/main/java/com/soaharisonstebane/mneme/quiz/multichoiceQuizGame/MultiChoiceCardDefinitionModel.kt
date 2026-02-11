package com.soaharisonstebane.mneme.quiz.multichoiceQuizGame

import com.soaharisonstebane.mneme.util.TextWithLanguageModel

data class MultiChoiceCardDefinitionModel(
    val cardId: String,
    val definition: TextWithLanguageModel,
    var position: Int? = null,
    val isCorrect: Boolean,
    var isSelected: Boolean = false
)
