package com.soaharisonstebane.mneme.quiz.test

import android.os.Parcelable
import com.soaharisonstebane.mneme.backend.entities.CardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestCardModel (
    val cardId: String,
    val cardContent: CardContent,
    val cardContentLanguage: String?,
    val cardDefinition: List<TestCardDefinitionModel>,
    val cardDefinitionLanguage: String?,
    val cardType: String,
    var isActualOrPassed: Boolean = false,
//    var userAnswers: ArrayList<TestCardDefinitionModel>? = null
): Parcelable {
    fun setAsActualOrPassed() {
        isActualOrPassed = true
    }
    fun setAsNotActualOrNotPassed() {
        isActualOrPassed = false
    }
}