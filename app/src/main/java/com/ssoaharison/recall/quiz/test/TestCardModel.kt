package com.ssoaharison.recall.quiz.test

import android.os.Parcelable
import com.ssoaharison.recall.backend.entities.CardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestCardModel (
    val cardId: String,
    val cardContent: CardContent,
    val cardDefinition: List<TestCardDefinitionModel>,
    val cardType: String,
//    var userAnswers: ArrayList<TestCardDefinitionModel>? = null
): Parcelable