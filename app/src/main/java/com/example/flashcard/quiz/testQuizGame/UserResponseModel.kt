package com.example.flashcard.quiz.testQuizGame

import android.view.View
import com.example.flashcard.backend.Model.ImmutableCard
import com.google.android.material.card.MaterialCardView

data class UserResponseModel (
    val modelCard: ModelCard,
    val modelCardPosition: Int,
    val view: View,
    val containerFront: MaterialCardView,
    val containerBack: MaterialCardView
)