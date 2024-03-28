package com.example.flashcard.backend.Model

data class ImmutableDeckWithCards(
    val deck: ImmutableDeck? = null,
    val cards: List<ImmutableCard>? = null
)
