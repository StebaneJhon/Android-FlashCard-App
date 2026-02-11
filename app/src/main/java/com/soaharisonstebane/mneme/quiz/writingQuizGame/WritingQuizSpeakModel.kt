package com.soaharisonstebane.mneme.quiz.writingQuizGame

import android.view.View
import android.widget.Button
import com.soaharisonstebane.mneme.util.TextWithLanguageModel

data class WritingQuizSpeakModel(
    val text: TextWithLanguageModel,
    val views: View,
    val speakButton: Button
)
