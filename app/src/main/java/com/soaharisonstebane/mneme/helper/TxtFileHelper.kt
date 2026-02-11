package com.soaharisonstebane.mneme.helper

import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.CardContent
import com.soaharisonstebane.mneme.backend.entities.CardDefinition
import com.soaharisonstebane.mneme.backend.entities.relations.CardContentWithDefinitions
import com.soaharisonstebane.mneme.backend.entities.relations.CardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.card.isCorrectRevers
import com.soaharisonstebane.mneme.card.today
import com.soaharisonstebane.mneme.util.CardLevel.L1
import com.soaharisonstebane.mneme.util.CardType.SINGLE_ANSWER_CARD
import java.util.UUID

fun cardToText(card: ExternalCardWithContentAndDefinitions, separator: String): String {
    val content = card.contentWithDefinitions.content.contentText
    val definition = StringBuilder()
    card.contentWithDefinitions.definitions.forEach {
        definition.append("${it.definitionText},")
    }
    return "$content $separator ${definition.deleteAt(definition.length.minus(1))}\n"
}

fun textToImmutableCard(text: String, separator: String, deckId: String): CardWithContentAndDefinitions {
    val textData = text.split(separator)
    val textContent = textData.first().trim()
    val textDefinition = textData.last().trim()
    val cardId = UUID.randomUUID().toString()
    val contentId = UUID.randomUUID().toString()
    val cardContent = CardContent(
        contentId = contentId,
        cardOwnerId = cardId,
        deckOwnerId = deckId,
        contentText = textContent,
        contentImageName = null,
        contentAudioName = null,
        contentAudioDuration = null,
    )

    val cardDefinition = listOf(
        CardDefinition(
            definitionId = UUID.randomUUID().toString(),
            cardOwnerId = cardId,
            deckOwnerId = deckId,
            contentOwnerId = contentId,
            isCorrectDefinition = isCorrectRevers(true),
            definitionText = textDefinition,
            definitionImageName = null,
            definitionAudioName = null,
            definitionAudioDuration = null,
        )
    )

    val card = Card(
        cardId = cardId,
        deckOwnerId = deckId,
        cardLevel = L1,
        cardType = SINGLE_ANSWER_CARD,
        revisionTime = 0,
        missedTime = 0,
        creationDate = today(),
        lastRevisionDate = null,
        nextMissMemorisationDate = null,
        nextRevisionDate = null,
        cardContentLanguage = null,
        cardDefinitionLanguage = null
    )
    return CardWithContentAndDefinitions(
        card = card,
        contentWithDefinitions = CardContentWithDefinitions(
            content = cardContent,
            definitions = cardDefinition
        )
    )
}

