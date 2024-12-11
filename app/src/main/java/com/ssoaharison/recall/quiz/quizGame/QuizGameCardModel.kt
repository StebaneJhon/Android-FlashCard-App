package com.ssoaharison.recall.quiz.quizGame

import android.os.Parcelable
import com.ssoaharison.recall.backend.entities.CardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizGameCardModel(
    val cardId: String,
    val cardContent: CardContent?,
    val cardContentLanguage: String?,
    val cardDefinition: List<QuizGameCardDefinitionModel>,
    val cardDefinitionLanguage: String?,
    val cardType: String?,
    val cardStatus: String?,
    var isFlipped: Boolean = false,
    var attemptTime: Int = 0,
    var isCorrectlyAnswered: Boolean = false,
    var flipCount: Int = 0,
): Parcelable