package com.example.flashcard.quiz.quizGame

import android.os.Parcelable
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizGameCardModel(
    val cardId: String,
    val cardContent: CardContent?,
    val cardDefinition: List<QuizGameCardDefinitionModel>,
    val cardType: String?,
    val cardStatus: String?,
    var isFlipped: Boolean = false
//    val cardPosition: Int?,
): Parcelable