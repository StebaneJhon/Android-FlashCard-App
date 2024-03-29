package com.example.flashcard.util

import com.example.flashcard.backend.Model.ImmutableCard

class TrueOrFalseCardModel(val card: ImmutableCard, val cardList: List<ImmutableCard>) {

    fun isFlippable() = false

    fun getCardPosition() = cardList.indexOf(card).plus(1)

    fun getCardSum() = cardList.size

    fun isAnswerCorrect(userAnswer: String): Boolean {
        var correctAnswer: String = ""
        card.cardDefinition?.forEach {
            if (it.isCorrectDefinition == true) {
                correctAnswer = it.definition.toString()
            }
        }
        return correctAnswer == userAnswer
    }

}