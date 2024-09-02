package com.example.flashcard.deck

import android.os.Parcelable
import com.example.flashcard.backend.entities.Deck
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnSaveDeckWithCationModel(
    val deck: Deck,
    val action: String
): Parcelable
