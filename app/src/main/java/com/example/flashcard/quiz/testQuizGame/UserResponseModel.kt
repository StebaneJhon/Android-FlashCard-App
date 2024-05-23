package com.example.flashcard.quiz.testQuizGame

import android.view.View
import com.example.flashcard.backend.Model.ImmutableCard

data class UserResponseModel (
    val card: ImmutableCard,
    val view: View
)