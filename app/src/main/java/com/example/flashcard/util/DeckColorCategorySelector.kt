package com.example.flashcard.util

import com.example.flashcard.R
import com.example.flashcard.util.deckCategoryColorConst.BLACK
import com.example.flashcard.util.deckCategoryColorConst.BLUE
import com.example.flashcard.util.deckCategoryColorConst.BROWN
import com.example.flashcard.util.deckCategoryColorConst.CYAN
import com.example.flashcard.util.deckCategoryColorConst.EMERALD
import com.example.flashcard.util.deckCategoryColorConst.GREEN
import com.example.flashcard.util.deckCategoryColorConst.GREY
import com.example.flashcard.util.deckCategoryColorConst.INDIGO
import com.example.flashcard.util.deckCategoryColorConst.LIME
import com.example.flashcard.util.deckCategoryColorConst.PINK
import com.example.flashcard.util.deckCategoryColorConst.PURPLE
import com.example.flashcard.util.deckCategoryColorConst.RED
import com.example.flashcard.util.deckCategoryColorConst.ROSE
import com.example.flashcard.util.deckCategoryColorConst.SKY
import com.example.flashcard.util.deckCategoryColorConst.TEAL
import com.example.flashcard.util.deckCategoryColorConst.VIOLET
import com.example.flashcard.util.deckCategoryColorConst.WHITE
import com.example.flashcard.util.deckCategoryColorConst.YELLOW
import com.example.flashcard.util.deckCategoryColorConst.ORANGE
import com.example.flashcard.util.deckCategoryColorConst.FUCHSIA

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

    private val colors2 = mapOf(
        WHITE to R.color.neutral50,
        GREY to R.color.neutral400,
        BLACK to R.color.neutral950,
        RED to R.color.red900,
        ORANGE to R.color.orange900,
        YELLOW to R.color.yellow900,
        LIME to R.color.lime900,
        GREEN to R.color.green900,
        EMERALD to R.color.emerald900,
        TEAL to R.color.teal900,
        CYAN to R.color.cyan900,
        SKY to R.color.sky900,
        BLUE to R.color.blue900,
        INDIGO to R.color.indigo900,
        VIOLET to R.color.violet900,
        PURPLE to R.color.purple900,
        FUCHSIA to R.color.fuchsia900,
        PINK to R.color.pink900,
        ROSE to R.color.rose900,
    )

    fun getColors() = colors2

    fun selectColor(color: String): Int? {
        return if (color in colors2.keys) {
            colors2[color]
        } else {
            null
        }
    }
}

