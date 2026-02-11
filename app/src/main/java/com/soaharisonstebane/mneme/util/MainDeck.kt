package com.soaharisonstebane.mneme.util

import com.soaharisonstebane.mneme.backend.entities.Deck
import com.soaharisonstebane.mneme.backend.models.toExternal
import com.soaharisonstebane.mneme.card.today

class MainDeck {
    private val mainDeck = Deck(
        deckId = MAIN_DECK_ID,
        parentDeckId = null,
        deckName = MAIN_DECK_NAME,
        deckDescription = null,
        cardContentDefaultLanguage = null,
        cardDefinitionDefaultLanguage = null,
        deckBackground = null,
        deckCategory = null,
        isFavorite = null,
        deckCreationDate = today()
    )
    fun getMainDeck() = mainDeck
    fun getExternalMainDeck() = mainDeck.toExternal(0, 0, 0)
}
