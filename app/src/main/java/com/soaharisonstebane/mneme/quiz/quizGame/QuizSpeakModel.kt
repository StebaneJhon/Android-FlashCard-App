package com.soaharisonstebane.mneme.quiz.quizGame

import android.widget.TextView
import com.soaharisonstebane.mneme.util.TextWithLanguageModel

data class QuizSpeakModel(
    val text: List<TextWithLanguageModel>,
    val views: List<TextView>,
)
