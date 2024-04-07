package com.example.flashcard.quiz.testQuizGame

import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.entities.CardDefinition

class OneOrMultipleAnswerCardModel(val card: ImmutableCard, val cardList: List<ImmutableCard?>) {

    fun isFlippable() = false

    fun getCardPosition() = cardList.indexOf(card).plus(1)

    fun getCardSum() = cardList.size

    fun getCorrectAnswer() = card.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == true
    } ?: listOf<CardDefinition>()

    fun getWrongAnswer() = card.cardDefinition?.filter {
            definition -> definition.isCorrectDefinition == false
    } ?: listOf<CardDefinition>()

    fun isAnswerCorrect(userAnswer: CardDefinition) = getCorrectAnswer().contains(userAnswer) ?: false

    fun getCardAnswers() = (getCorrectAnswer() + getWrongAnswer()).shuffled()

    fun getCardContent() = card.cardContent

}