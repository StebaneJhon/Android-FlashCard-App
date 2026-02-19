package com.soaharisonstebane.mneme.helper

import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.CardContent
import com.soaharisonstebane.mneme.backend.entities.CardDefinition
import com.soaharisonstebane.mneme.backend.entities.relations.CardContentWithDefinitions
import com.soaharisonstebane.mneme.backend.entities.relations.CardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.home.isCorrectRevers
import com.soaharisonstebane.mneme.home.today
import com.soaharisonstebane.mneme.util.CardLevel.L1
import com.soaharisonstebane.mneme.util.CardType.SINGLE_ANSWER_CARD
import java.util.UUID

fun cardToText(card: ExternalCardWithContentAndDefinitions, separator: String): String {
    val spannableContent = Html.fromHtml(card.contentWithDefinitions.content.contentText, FROM_HTML_MODE_LEGACY).trim()
    val definition = StringBuilder()
    card.contentWithDefinitions.definitions.forEach {
        val spannableDefinition = Html.fromHtml(it.definitionText, FROM_HTML_MODE_LEGACY).trim()
        definition.append("${spannableDefinition},")
    }
    return "$spannableContent $separator ${definition.deleteAt(definition.length.minus(1))}\n"
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

