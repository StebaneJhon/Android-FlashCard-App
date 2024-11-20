package com.ssoaharison.recall.quiz.multichoiceQuizGame

import com.ssoaharison.recall.util.TextWithLanguageModel

data class MultiChoiceGameCardModel(
    val cardId: String,
    val onCardWord: TextWithLanguageModel,
    val alternatives: List<MultiChoiceCardDefinitionModel>,
    var attemptTime: Int = 0,
    var isCorrectlyAnswered: Boolean = false,
)