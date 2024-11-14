package com.ssoaharison.recall.quiz.multichoiceQuizGame

import android.view.View
import android.widget.Button
import com.ssoaharison.recall.util.TextWithLanguageModel

data class SpeakModel(
    val text: List<TextWithLanguageModel>,
    val views: List<View>,
    val speakButton: Button
)
