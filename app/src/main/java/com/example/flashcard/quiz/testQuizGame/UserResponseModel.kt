package com.example.flashcard.quiz.testQuizGame

import android.view.View
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.CardDefinition
import com.google.android.material.card.MaterialCardView

data class UserResponseModel (
    var userAnswer: CardDefinition?,
    val modelCard: ModelCard,
    val modelCardPosition: Int,
    val view: View,
    val containerFront: MaterialCardView,
    val containerBack: MaterialCardView
)