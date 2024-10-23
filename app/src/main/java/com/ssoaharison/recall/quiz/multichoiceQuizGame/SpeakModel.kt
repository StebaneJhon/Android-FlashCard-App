package com.ssoaharison.recall.quiz.multichoiceQuizGame

import android.view.View
import android.widget.Button

data class SpeakModel(
    val text: List<String>,
    val views: List<View>,
    val speakButton: Button
)
