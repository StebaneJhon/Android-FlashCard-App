package com.ssoaharison.recall.quiz.writingQuizGame

import com.ssoaharison.recall.util.TextWithLanguageModel

data class WritingQuizGameModel (
    val cardId: String,
    val onCardWord: TextWithLanguageModel,
    val answer: List<TextWithLanguageModel>
)