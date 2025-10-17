package com.ssoaharison.recall.backend.models

import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.User
import com.ssoaharison.recall.backend.entities.relations.CardContentWithDefinitions
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.entities.relations.DeckWithCardsAndContentAndDefinitions
import com.ssoaharison.recall.help.AudioModel
import com.ssoaharison.recall.help.ImageModel

//fun ImmutableDeck.toLocal() = Deck(
//    deckId = deckId,
//    deckName = deckName,
//    deckDescription = deckDescription,
//    cardContentDefaultLanguage = cardContentDefaultLanguage,
//    cardDefinitionDefaultLanguage = cardDefinitionDefaultLanguage,
//    deckColorCode = deckColorCode,
//    deckCategory = deckCategory,
//    isFavorite = isCorrectRevers(isFavorite),
//)

fun ExternalDeck.toLocal() = Deck(
    deckId = deckId,
    parentDeckId = parentDeckId,
    deckName = deckName,
    deckDescription = deckDescription,
    cardContentDefaultLanguage = cardContentDefaultLanguage,
    cardDefinitionDefaultLanguage = cardDefinitionDefaultLanguage,
    deckColorCode = deckColorCode,
    deckCategory = deckCategory,
    isFavorite = isFavorite,
    deckCreationDate = deckCreationDate,
)

//fun List<ImmutableDeck>.toLocal() = map(ImmutableDeck::toLocal)

@JvmName("externalDeckToLocal")
fun List<ExternalDeck>.toLocal() = map(ExternalDeck::toLocal)

//fun Deck.toExternal(cardCount: Int, knownCardCount: Int, unKnownCardCount: Int) = ImmutableDeck(
//    deckId = deckId,
//    deckName = deckName,
//    deckDescription = deckDescription,
//    cardContentDefaultLanguage = cardContentDefaultLanguage,
//    cardDefinitionDefaultLanguage = cardDefinitionDefaultLanguage,
//    deckColorCode = deckColorCode,
//    cardSum = cardCount,
//    knownCardCount = knownCardCount,
//    unKnownCardCount = unKnownCardCount,
//    deckCategory = deckCategory,
//    isFavorite = isCorrect(isFavorite),
//)

fun Deck.toExternal(cardCount: Int, knownCardCount: Int, unKnownCardCount: Int) = ExternalDeck(
    deckId = deckId,
    parentDeckId = parentDeckId,
    deckName = deckName,
    deckDescription = deckDescription,
    cardContentDefaultLanguage = cardContentDefaultLanguage,
    cardDefinitionDefaultLanguage = cardDefinitionDefaultLanguage,
    deckColorCode = deckColorCode,
    deckCategory = deckCategory,
    isFavorite = isFavorite,
    deckCreationDate = deckCreationDate,
    cardCount = cardCount,
    knownCardCount = knownCardCount,
    unKnownCardCount = unKnownCardCount
)

@JvmName("localToExternal")
fun List<Deck>.toExternal(cardCount: Int, knownCardCount: Int, unKnownCardCount: Int) = map { deck -> deck.toExternal(cardCount, knownCardCount, unKnownCardCount)}


//fun ImmutableCard.toLocal() = Card(
//    cardId = cardId,
//    deckId = deckId,
//    isFavorite = isCorrectRevers(isFavorite),
//    revisionTime = revisionTime,
//    missedTime = missedTime,
//    creationDate = creationDate,
//    lastRevisionDate = lastRevisionDate,
//    cardStatus = cardStatus,
//    nextMissMemorisationDate = nextMissMemorisationDate,
//    nextRevisionDate = nextRevisionDate,
//    cardType = cardType,
//    cardContentLanguage = cardContentLanguage,
//    cardDefinitionLanguage = cardDefinitionLanguage
//)

fun ExternalCard.toLocal() = Card(
    cardId = cardId,
    deckOwnerId = deckOwnerId,
    cardLevel = cardLevel,
    cardType = cardType,
    revisionTime = revisionTime,
    missedTime = missedTime,
    creationDate = creationDate,
    lastRevisionDate = lastRevisionDate,
    nextMissMemorisationDate = nextMissMemorisationDate,
    nextRevisionDate = nextRevisionDate,
    cardContentLanguage = cardContentLanguage,
    cardDefinitionLanguage = cardDefinitionLanguage
)

//@JvmName("cardExternalToLocal")
//fun List<ImmutableCard>.toLocal() = map(ImmutableCard::toLocal)

@JvmName("cardExternalToLocal")
fun List<ExternalCard>.toLocal() = map(ExternalCard::toLocal)

//fun Card.toExternal(cardContent: CardContent, cardDefinitions: List<CardDefinition>) =
//    ImmutableCard(
//        cardId = cardId,
//        cardContent = cardContent,
//        cardDefinition = cardDefinitions,
//        deckId = deckId,
//        isFavorite = isCorrect(isFavorite),
//        revisionTime = revisionTime,
//        missedTime = missedTime,
//        creationDate = creationDate,
//        lastRevisionDate = lastRevisionDate,
//        cardStatus = cardStatus,
//        nextMissMemorisationDate = nextMissMemorisationDate,
//        nextRevisionDate = nextRevisionDate,
//        cardType = cardType,
//        cardContentLanguage = cardContentLanguage,
//        cardDefinitionLanguage = cardDefinitionLanguage,
//    )

fun Card.toExternal() = ExternalCard(
    cardId = cardId,
    deckOwnerId = deckOwnerId,
    cardLevel = cardLevel,
    cardType =  cardType,
    revisionTime = revisionTime,
    missedTime = missedTime,
    creationDate = creationDate,
    lastRevisionDate = lastRevisionDate,
    nextMissMemorisationDate = nextMissMemorisationDate,
    nextRevisionDate = nextRevisionDate,
    cardContentLanguage = cardContentLanguage,
    cardDefinitionLanguage = cardDefinitionLanguage
)


//@JvmName("cardLocalToExternal")
//fun List<Card>.toExternal(cardContent: CardContent, cardDefinitions: List<CardDefinition>) =
//    map { card -> card.toExternal(cardContent, cardDefinitions) }

@JvmName("cardLocalToExternal")
fun List<Card>.toExternal() = map( Card::toExternal )

fun ExternalCardDefinition.toLocal() = CardDefinition(
    definitionId = definitionId,
    cardOwnerId = cardOwnerId,
    deckOwnerId = deckOwnerId,
    contentOwnerId = contentOwnerId,
    isCorrectDefinition = isCorrectDefinition,
    definitionText = definitionText,
    definitionImageName = definitionImage?.name,
    definitionAudioName = definitionAudio?.name
)

@JvmName("externalCardDefinitionToLocal")
fun List<ExternalCardDefinition>.toLocal() = map(ExternalCardDefinition::toLocal)

fun CardDefinition.toExternal(imageModel: ImageModel?, audioModel: AudioModel?) = ExternalCardDefinition(
    definitionId = definitionId,
    cardOwnerId = cardOwnerId,
    deckOwnerId = deckOwnerId,
    contentOwnerId = contentOwnerId,
    isCorrectDefinition = isCorrectDefinition,
    definitionText = definitionText,
    definitionImage = imageModel,
    definitionAudio = audioModel
)

@JvmName("localCardDefinitionToExternal")
fun List<CardDefinition>.toExternal(imageModel: ImageModel?, audioModel: AudioModel?) = map { definition -> definition.toExternal(imageModel, audioModel)}

fun ExternalCardContent.toLocal() = CardContent(
    contentId = contentId,
    cardOwnerId = cardOwnerId,
    deckOwnerId = deckOwnerId,
    contentText = contentText,
    contentImageName = contentImage?.name,
    contentAudioName = contentAudio?.name
)

@JvmName("externalCardContentToLocal")
fun List<ExternalCardContent>.toLocal() = map(ExternalCardContent::toLocal)

fun CardContent.toExternal(imageModel: ImageModel?, audioModel: AudioModel?) = ExternalCardContent(
    contentId = contentId,
    cardOwnerId = cardOwnerId,
    deckOwnerId = deckOwnerId,
    contentText = contentText,
    contentImage = imageModel,
    contentAudio = audioModel
)

@JvmName("localCardContentToExternal")
fun List<CardContent>.toExternal(imageModel: ImageModel?, audioModel: AudioModel?) = map { content -> content.toExternal(imageModel, audioModel)}

@JvmName("externalCardContentWithDefinitionsToLocal")
fun ExternalCardContentWithDefinitions.toLocal() = CardContentWithDefinitions(
    content = content.toLocal(),
    definitions = definitions.toLocal()
)

@JvmName("externalCardWithContentAndDefinitionsToLocal")
fun ExternalCardWithContentAndDefinitions.toLocal() = CardWithContentAndDefinitions(
    card = card.toLocal(),
    contentWithDefinitions = contentWithDefinitions.toLocal()
)


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
fun DeckWithCardsAndContentAndDefinitions.toExternal(deck: ImmutableDeck, cards: List<ImmutableCard?>) =
    ImmutableDeckWithCards(
        deck = deck,
        cards = cards
    )

fun isCorrect(index: Int?) = index == 1
fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

