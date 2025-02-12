package com.ssoaharison.recall.card

import android.content.res.ColorStateList
import android.widget.TextView
import com.ssoaharison.recall.util.TextWithLanguageModel

data class TextClickedModel(
    val text: TextWithLanguageModel,
    val view: TextView,
    val textColor: Int,
    val type: String
)
