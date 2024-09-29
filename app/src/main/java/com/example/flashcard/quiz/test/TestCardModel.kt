package com.example.flashcard.quiz.test

import android.os.Parcelable
import com.example.flashcard.backend.entities.CardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestCardModel (
    val cardId: String,
    val cardContent: CardContent,
    val cardDefinition: List<TestCardDefinitionModel>,
    val cardType: String,
//    var userAnswers: ArrayList<TestCardDefinitionModel>? = null
): Parcelable