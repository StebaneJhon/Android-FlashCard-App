package com.soaharisonstebane.mneme.quiz.multichoiceQuizGame

import android.view.View
import android.widget.Button
import com.soaharisonstebane.mneme.util.TextWithLanguageModel

data class SpeakModel(
    val text: List<TextWithLanguageModel>,
    val views: List<View>,
    val speakButton: Button
)
