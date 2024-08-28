package com.example.flashcard.card

import com.example.flashcard.backend.Model.ImmutableCard

data class ModelCardWithPositionOnLocalEdit (
    val cardToEdit: ImmutableCard,
    val position: Int,
)