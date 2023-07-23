package com.example.flashcard.util

import com.example.flashcard.R

class DeckColorCategorySelector {

    private val colors = mapOf(
        "red" to R.color.red700,
        "purple" to R.color.purple700,
        "black" to R.color.black
    )

    fun selectColor(color: String) = colors[color]
}