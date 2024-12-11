package com.ssoaharison.recall.backend.models

import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.User
import com.ssoaharison.recall.backend.entities.relations.DeckWithCards

fun ImmutableDeck.toLocal() = Deck(
    deckId = deckId,
    deckName = deckName,
    deckDescription = deckDescription,
    cardContentDefaultLanguage = cardContentDefaultLanguage,
    cardDefinitionDefaultLanguage = cardDefinitionDefaultLanguage,
    deckColorCode = deckColorCode,
    deckCategory = deckCategory,
    isFavorite = isCorrectRevers(isFavorite),
)

fun List<ImmutableDeck>.toLocal() = map(ImmutableDeck::toLocal)

fun Deck.toExternal(cardCount: Int, knownCardCount: Int, unKnownCardCount: Int) = ImmutableDeck(
    deckId = deckId,
    deckName = deckName,
    deckDescription = deckDescription,
    cardContentDefaultLanguage = cardContentDefaultLanguage,
    cardDefinitionDefaultLanguage = cardDefinitionDefaultLanguage,
    deckColorCode = deckColorCode,
    cardSum = cardCount,
    knownCardCount = knownCardCount,
    unKnownCardCount = unKnownCardCount,
    deckCategory = deckCategory,
    isFavorite = isCorrect(isFavorite),
)

@JvmName("localToExternal")
fun List<Deck>.toExternal(cardCount: Int, knownCardCount: Int, unKnownCardCount: Int) = map { deck -> deck.toExternal(cardCount, knownCardCount, unKnownCardCount)}

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
    cardContentLanguage = cardContentLanguage,
    cardDefinitionLanguage = cardDefinitionLanguage
)

@JvmName("cardExternalToLocal")
fun List<ImmutableCard>.toLocal() = map(ImmutableCard::toLocal)

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
        cardContentLanguage = cardContentLanguage,
        cardDefinitionLanguage = cardDefinitionLanguage,
    )

@JvmName("cardLocalToExternal")
fun List<Card>.toExternal(cardContent: CardContent, cardDefinitions: List<CardDefinition>) =
    map { card -> card.toExternal(cardContent, cardDefinitions) }

@JvmName("userExternalToLocal")
fun List<ImmutableUser>.toLocal() = map(ImmutableUser::toLocal)

fun ImmutableUser.toLocal() = User(
    userId = userId,
    name = name,
    initial = initial,
    status = status,
    creation = creation
)

@JvmName("userLocalToExternal")
fun List<User>.toExternal() = map(User::toExternal)

fun User.toExternal() = ImmutableUser(
    userId = userId,
    name = name,
    initial = initial,
    status = status,
    creation = creation
)

@JvmName("spaceRepetitionBoxExternalToLocal")
fun List<ImmutableSpaceRepetitionBox>.toLocal() = map(ImmutableSpaceRepetitionBox::toLocal)

fun ImmutableSpaceRepetitionBox.toLocal() = SpaceRepetitionBox(
    levelId = levelId,
    levelName = levelName,
    levelColor = levelColor,
    levelRepeatIn = levelRepeatIn,
    levelRevisionMargin = levelRevisionMargin
)

@JvmName("spaceRepetitionBoxLocalToExternal")
fun List<SpaceRepetitionBox>.toExternal() = map(SpaceRepetitionBox::toExternal)

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

