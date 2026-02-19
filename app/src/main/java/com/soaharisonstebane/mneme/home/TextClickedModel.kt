package com.soaharisonstebane.mneme.home

import android.widget.TextView
import com.soaharisonstebane.mneme.util.TextWithLanguageModel

data class TextClickedModel(
    val text: TextWithLanguageModel,
    val view: TextView,
    val textColor: Int,
    val type: String
)
