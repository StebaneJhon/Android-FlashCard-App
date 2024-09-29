package com.example.flashcard.quiz.quizGame

import com.example.flashcard.backend.entities.CardDefinition

class FlashCardModel(val modelCard: ModelCard, val cardList: List<ModelCard?>) {

    private var isFlipped = true
    private val card = modelCard.cardDetails

    fun getCardPosition() = cardList.indexOf(modelCard).plus(1)
    fun getCardSum() = cardList.size
    fun isFlippable() = true
    fun isFlipped() = isFlipped
    fun flip() = !isFlipped
    fun getCorrectAnswer() = card?.cardDefinition?.filter {
        definition -> definition.isCorrectDefinition == 1
    } ?: listOf<CardDefinition>()

    fun getCardAnswers() = getCorrectAnswer()

    fun getCardContent() = card?.cardContent

}