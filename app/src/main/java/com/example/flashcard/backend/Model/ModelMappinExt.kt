package com.example.flashcard.backend.Model

import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.backend.entities.User
import com.example.flashcard.backend.entities.relations.DeckWithCards
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// External to local
fun ImmutableDeck.toLocal() = Deck(
    deckId = deckId,
    deckName = deckName,
    deckDescription = deckDescription,
    deckFirstLanguage = deckFirstLanguage,
    deckSecondLanguage = deckSecondLanguage,
    deckColorCode = deckColorCode,
    cardSum = cardSum,
    deckCategory = deckCategory,
    isFavorite = isCorrectRevers(isFavorite),
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
    cardSum = cardSum,
    deckCategory = deckCategory,
    isFavorite = isCorrect(isFavorite),
)

@JvmName("localToExternal")
fun List<Deck>.toExternal() = map(Deck::toExternal)

// Cards Ex
fun ImmutableCard.toLocal() = Card(
    cardId = cardId,
    deckId = deckId,
    isFavorite = isCorrectRevers(isFavorite),
    revisionTime = revisionTime,
    missedTime = missedTime,
    creationDate = creationDate,
    lastRevisionDate = lastRevisionDate,
    cardStatus = cardStatus,
    nextMissMemorisationDate = nextMissMemorisationDate,
    nextRevisionDate = nextRevisionDate,
    cardType = cardType,
)

@JvmName("cardExternalToLocal")
fun List<ImmutableCard>.toLocal() = map(ImmutableCard::toLocal)

// Local to External
fun Card.toExternal(cardContent: CardContent, cardDefinitions: List<CardDefinition>) =
    ImmutableCard(
        cardId = cardId,
        cardContent = cardContent,
        cardDefinition = cardDefinitions,
        deckId = deckId,
        isFavorite = isCorrect(isFavorite),
        revisionTime = revisionTime,
        missedTime = missedTime,
        creationDate = creationDate,
        lastRevisionDate = lastRevisionDate,
        cardStatus = cardStatus,
        nextMissMemorisationDate = nextMissMemorisationDate,
        nextRevisionDate = nextRevisionDate,
        cardType = cardType,
    )

@JvmName("cardLocalToExternal")
fun List<Card>.toExternal(cardContent: CardContent, cardDefinitions: List<CardDefinition>) =
    map { card -> card.toExternal(cardContent, cardDefinitions) }

@JvmName("userExternalToLocal")
fun List<ImmutableUser>.toLocal() = map(ImmutableUser::toLocal)

// User Ex
fun ImmutableUser.toLocal() = User(
    userId = userId,
    name = name,
    initial = initial,
    status = status,
    creation = creation
)

@JvmName("userLocalToExternal")
fun List<User>.toExternal() = map(User::toExternal)

// User Loc
fun User.toExternal() = ImmutableUser(
    userId = userId,
    name = name,
    initial = initial,
    status = status,
    creation = creation
)

@JvmName("spaceRepetitionBoxExternalToLocal")
fun List<ImmutableSpaceRepetitionBox>.toLocal() = map(ImmutableSpaceRepetitionBox::toLocal)

// SpaceRepetitionBox Ex
fun ImmutableSpaceRepetitionBox.toLocal() = SpaceRepetitionBox(
    levelId = levelId,
    levelName = levelName,
    levelColor = levelColor,
    levelRepeatIn = levelRepeatIn,
    levelRevisionMargin = levelRevisionMargin
)

@JvmName("spaceRepetitionBoxLocalToExternal")
fun List<SpaceRepetitionBox>.toExternal() = map(SpaceRepetitionBox::toExternal)

// SpaceRepetitionBox Local
fun SpaceRepetitionBox.toExternal() = ImmutableSpaceRepetitionBox(
    levelId = levelId,
    levelName = levelName,
    levelColor = levelColor,
    levelRepeatIn = levelRepeatIn,
    levelRevisionMargin = levelRevisionMargin
)

@JvmName("deckWithCardsLocalToExternal")
fun DeckWithCards.toExternal(deck: ImmutableDeck, cards: List<ImmutableCard?>) =
    ImmutableDeckWithCards(
        deck = deck,
        cards = cards
    )

fun isCorrect(index: Int?) = index == 1
fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

private fun now(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
    return LocalDateTime.now().format(formatter)
}

