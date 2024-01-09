package com.example.flashcard.util

enum class MatchQuizGameBorderSize(val numChoice: Int) {
    DEFAULT(10);

    fun getWidth() = 2
    fun getHeight() = numChoice/getWidth()
    fun getNumCards() = numChoice/getWidth()
}