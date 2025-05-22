package com.ssoaharison.recall.helper

class RoteLearningAlgorithmHelper {

    fun onRepeatCardPosition(
        cards: List<Any?>,
        currentCardPosition: Int
    ): Int {
        val cardsLeftCount = countCardsLeft(cards, currentCardPosition)
        val repeatedCardPosition = when {
            cardsLeftCount <= 4 -> {cards.lastIndex + 1}
            cardsLeftCount <= 5 -> {currentCardPosition + listOf(4, 5).random()}
            cardsLeftCount <= 6 -> {currentCardPosition + listOf(4, 5, 6).random()}
            else -> {currentCardPosition + listOf(4, 5, 6, 7).random()}
        }
        return repeatedCardPosition
    }

    private fun countCardsLeft(
        cards: List<Any?>,
        currentCardPosition: Int
    ): Int {
        val cardsLeft = cards.subList(currentCardPosition, cards.lastIndex)
        return cardsLeft.size
    }

}