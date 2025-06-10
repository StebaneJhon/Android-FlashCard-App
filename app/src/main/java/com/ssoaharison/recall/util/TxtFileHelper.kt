package com.ssoaharison.recall.util

import com.ssoaharison.recall.backend.models.ImmutableCard

fun cardToText(card: ImmutableCard, separator: String): String {
    val content = card.cardContent?.content
    val definition = StringBuilder()
    card.cardDefinition?.forEach {
        definition.append("${it.definition},")
    }
    return "$content $separator ${definition.deleteAt(definition.length.minus(1))}\n"
}

