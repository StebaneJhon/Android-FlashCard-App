package com.example.flashcard.quiz.testQuizGame

import com.example.flashcard.backend.entities.CardDefinition

class TrueOrFalseCardModel(val modelCard: ModelCard, val cardList: List<ModelCard?>) {

    fun isFlippable() = false
    private val card = modelCard.cardDetails

    fun getCardPosition() = cardList.indexOf(modelCard).plus(1)

    fun getCardSum() = cardList.size

    fun getCorrectAnswer() = card?.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == 1
    } ?: listOf<CardDefinition>()

    fun getWrongAnswer() = card?.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == 0
    } ?: listOf<CardDefinition>()

    fun getCorrectAnswerSum() = getCorrectAnswer().size

    fun isAnswerCorrect(userAnswer: CardDefinition) = getCorrectAnswer().contains(userAnswer)


    fun getCardAnswers() = (getWrongAnswer() + getCorrectAnswer()).shuffled()

    fun getCardContent() = card?.cardContent

}