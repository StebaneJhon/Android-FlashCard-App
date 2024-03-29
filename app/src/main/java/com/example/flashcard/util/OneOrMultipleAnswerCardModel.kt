package com.example.flashcard.util

import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.CardDefinition

class OneOrMultipleAnswerCardModel(val card: ImmutableCard, val cardList: List<ImmutableCard>) {

    fun isFlippable() = false

    fun getCardPosition() = cardList.indexOf(card).plus(1)

    fun getCardSum() = cardList.size

    fun getCorrectAnswer() = card.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == true
    }

    fun getWrongAnswer() = card.cardDefinition?.filter {
            definition -> definition.isCorrectDefinition == false
    }

    fun isAnswerCorrect(userAnswer: CardDefinition) = getCorrectAnswer()?.contains(userAnswer) ?: false

}