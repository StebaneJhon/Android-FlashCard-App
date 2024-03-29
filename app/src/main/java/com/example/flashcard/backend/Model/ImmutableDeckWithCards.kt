package com.example.flashcard.backend.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImmutableDeckWithCards(
    val deck: ImmutableDeck? = null,
    val cards: List<ImmutableCard?>? = null
): Parcelable
