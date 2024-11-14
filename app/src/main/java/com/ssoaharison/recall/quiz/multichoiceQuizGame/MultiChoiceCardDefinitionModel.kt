package com.ssoaharison.recall.quiz.multichoiceQuizGame

import com.ssoaharison.recall.util.TextWithLanguageModel

data class MultiChoiceCardDefinitionModel(
    val cardId: String,
    val definition: TextWithLanguageModel,
    val isCorrect: Boolean,
    var isSelected: Boolean = false
)
