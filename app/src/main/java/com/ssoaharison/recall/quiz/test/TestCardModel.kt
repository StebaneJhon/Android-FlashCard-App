package com.ssoaharison.recall.quiz.test

import android.os.Parcelable
import com.ssoaharison.recall.backend.entities.CardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestCardModel (
    val cardId: String,
    val cardContent: CardContent,
    val cardContentLanguage: String?,
    val cardDefinition: List<TestCardDefinitionModel>,
    val cardDefinitionLanguage: String?,
    val cardType: String,
//    var userAnswers: ArrayList<TestCardDefinitionModel>? = null
): Parcelable