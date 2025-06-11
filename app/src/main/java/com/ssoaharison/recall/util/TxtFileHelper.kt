package com.ssoaharison.recall.util

import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.isCorrect
import com.ssoaharison.recall.card.isCorrectRevers
import com.ssoaharison.recall.card.now
import com.ssoaharison.recall.card.today
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import kotlinx.coroutines.delay
import java.util.UUID

fun cardToText(card: ImmutableCard, separator: String): String {
    val content = card.cardContent?.content
    val definition = StringBuilder()
    card.cardDefinition?.forEach {
        definition.append("${it.definition},")
    }
    return "$content $separator ${definition.deleteAt(definition.length.minus(1))}\n"
}

fun textToImmutableCard(text: String, separator: String, deckId: String): ImmutableCard {
    val textData = text.split(separator)
    val textContent = textData.first()
    val textDefinition = textData.last()
    val cardId = UUID.randomUUID().toString()
    val contentId = UUID.randomUUID().toString()
    val cardContent = CardContent(contentId, cardId, deckId, textContent)
    val cardDefinition = listOf(
        CardDefinition(
            null,
            cardId,
            deckId,
            contentId,
            textDefinition,
            isCorrectRevers(true)
        )
    )
    return ImmutableCard(
        cardId,
        cardContent,
        cardDefinition,
        deckId,
        isCorrect(0),
        0,
        0,
        today(),
        null,
        L1,
        null,
        null,
        SINGLE_ANSWER_CARD,
        null,
        null
    )
}

