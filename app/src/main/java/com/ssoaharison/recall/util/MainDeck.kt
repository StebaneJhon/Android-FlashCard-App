package com.ssoaharison.recall.util

import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.models.toExternal
import com.ssoaharison.recall.card.today

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
