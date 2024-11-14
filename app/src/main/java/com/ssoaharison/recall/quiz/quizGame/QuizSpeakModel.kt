package com.ssoaharison.recall.quiz.quizGame

import android.view.View
import com.ssoaharison.recall.util.TextWithLanguageModel

data class QuizSpeakModel(
    val text: List<TextWithLanguageModel>,
    val views: List<View>,
)
