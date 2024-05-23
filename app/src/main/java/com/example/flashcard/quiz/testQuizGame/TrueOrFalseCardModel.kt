package com.example.flashcard.quiz.testQuizGame

import com.example.flashcard.backend.entities.CardDefinition

class TrueOrFalseCardModel(val modelCard: ModelCard, val cardList: List<ModelCard?>) {

    fun isFlippable() = false
    private val card = modelCard.cardDetails

    fun getCardPosition() = cardList.indexOf(modelCard).plus(1)

    fun getCardSum() = cardList.size

    fun getCorrectAnswer() = card?.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == true
    } ?: listOf<CardDefinition>()

    fun getWrongAnswer() = card?.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == false
    } ?: listOf<CardDefinition>()

    fun isAnswerCorrect(userAnswer: CardDefinition) = getCorrectAnswer().contains(userAnswer) ?: false


    fun getCardAnswers() = (getWrongAnswer() + getCorrectAnswer()).shuffled()

    fun getCardContent() = card?.cardContent

}