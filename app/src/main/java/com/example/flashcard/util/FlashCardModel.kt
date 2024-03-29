package com.example.flashcard.util

import com.example.flashcard.backend.Model.ImmutableCard

class FlashCardModel(val card: ImmutableCard, val cardList: List<ImmutableCard>) {

    private var isFlipped = false

    fun getCardPosition() = cardList.indexOf(card).plus(1)
    fun getCardSum() = cardList.size
    fun isFlippable() = true

    fun isFlipped() = isFlipped

    fun flip() = !isFlipped

}