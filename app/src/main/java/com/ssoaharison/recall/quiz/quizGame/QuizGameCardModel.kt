package com.ssoaharison.recall.quiz.quizGame

import android.os.Parcelable
import com.ssoaharison.recall.backend.entities.CardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizGameCardModel(
    val cardId: String,
    val cardContent: CardContent?,
    val cardDefinition: List<QuizGameCardDefinitionModel>,
    val cardType: String?,
    val cardStatus: String?,
    var isFlipped: Boolean = false
): Parcelable