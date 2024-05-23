package com.example.flashcard.quiz.testQuizGame

import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.CardDefinition

class FlashCardModel(val card: ImmutableCard, val cardList: List<ImmutableCard?>) {

    private var isFlipped = true

    fun getCardPosition() = cardList.indexOf(card).plus(1)
    fun getCardSum() = cardList.size
    fun isFlippable() = true
    fun isFlipped() = isFlipped
    fun flip() = !isFlipped
    fun getCorrectAnswer() = card.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == true
    } ?: listOf<CardDefinition>()

    fun getCardAnswers() = getCorrectAnswer()

    fun getCardContent() = card.cardContent

}