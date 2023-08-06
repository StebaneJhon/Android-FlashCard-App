package com.example.flashcard.util

import com.example.flashcard.R
import com.example.flashcard.util.deckCategoryColorConst.BLACK
import com.example.flashcard.util.deckCategoryColorConst.BLUE
import com.example.flashcard.util.deckCategoryColorConst.BROWN
import com.example.flashcard.util.deckCategoryColorConst.GREEN
import com.example.flashcard.util.deckCategoryColorConst.PINK
import com.example.flashcard.util.deckCategoryColorConst.PURPLE
import com.example.flashcard.util.deckCategoryColorConst.RED
import com.example.flashcard.util.deckCategoryColorConst.TEAL
import com.example.flashcard.util.deckCategoryColorConst.WHITE
import com.example.flashcard.util.deckCategoryColorConst.YELLOW

class DeckColorCategorySelector {

    private val colors = mapOf(
        WHITE to R.color.white,
        RED to R.color.red700,
        PINK to R.color.pink700,
        PURPLE to R.color.purple700,
        BLUE to R.color.blue700,
        TEAL to R.color.teal700,
        GREEN to R.color.green700,
        YELLOW to R.color.yellow700,
        BROWN to R.color.brown700,
        BLACK to R.color.black
    )

    fun selectColor(color: String): Int? {
        return if (color in colors.keys) {
            colors[color]
        } else {
            null
        }
    }
}