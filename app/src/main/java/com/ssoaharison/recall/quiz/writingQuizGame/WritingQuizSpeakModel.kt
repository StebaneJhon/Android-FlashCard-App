package com.ssoaharison.recall.quiz.writingQuizGame

import android.view.View
import android.widget.Button
import com.ssoaharison.recall.util.TextWithLanguageModel

data class WritingQuizSpeakModel(
    val text: TextWithLanguageModel,
    val views: View,
    val speakButton: Button
)
