package com.ssoaharison.recall.quiz.multichoiceQuizGame

import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

data class MultiChoiceQuizGameUserChoiceModel(
    val answer: List<String>,
    val userChoice: String,
    val cvCard: MaterialCardView,
    val cvCardOnWrongAnswer: MaterialCardView,
    val selectedButton: MaterialButton,
    val selectedButtonOnWrong: MaterialButton
)
