package com.ssoaharison.recall.util

import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.entities.relations.CardContentWithDefinitions
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.card.isCorrectRevers
import com.ssoaharison.recall.card.today
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
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

