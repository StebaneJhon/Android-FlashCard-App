package com.example.flashcard.backend.Model

import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck

// External to local
fun ImmutableDeck.toLocal() = Deck (
    deckId = deckId,
    deckName = deckName,
    deckDescription = deckDescription,
    deckFirstLanguage = deckFirstLanguage,
    deckSecondLanguage = deckSecondLanguage,
    deckColorCode = deckColorCode,
    cardSum = cardSum
)

fun List<ImmutableDeck>.toLocal() = map(ImmutableDeck::toLocal)

// Local to External
fun Deck.toExternal() = ImmutableDeck(
    deckId = deckId,
    deckName = deckName,
    deckDescription = deckDescription,
    deckFirstLanguage = deckFirstLanguage,
    deckSecondLanguage = deckSecondLanguage,
    deckColorCode = deckColorCode,
    cardSum = cardSum
)

@JvmName("localToExternal")
fun List<Deck>.toExternal() = map(Deck::toExternal)

// Cards Ex
fun ImmutableCard.toLocal() = Card (
    cardId = cardId,
    cardContent = cardContent,
    contentDescription = contentDescription,
    cardDefinition = cardDefinition,
    valueDefinition = valueDefinition,
    deckId = deckId
)

@JvmName("cardExternalToLocal")
fun List<ImmutableCard>.toLocal() = map(ImmutableCard::toLocal)

// Local to External
fun Card.toExternal() = ImmutableCard (
    cardId = cardId,
    cardContent = cardContent,
    contentDescription = contentDescription,
    cardDefinition = cardDefinition,
    valueDefinition = valueDefinition,
    deckId = deckId
)
@JvmName("cardLocalToExternal")
fun List<Card>.toExternal() = map(Card::toExternal)