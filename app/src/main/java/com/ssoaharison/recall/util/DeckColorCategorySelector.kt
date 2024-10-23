package com.ssoaharison.recall.util

import com.ssoaharison.recall.R
import com.ssoaharison.recall.util.DeckCategoryColorConst.BLACK
import com.ssoaharison.recall.util.DeckCategoryColorConst.BLUE
import com.ssoaharison.recall.util.DeckCategoryColorConst.CYAN
import com.ssoaharison.recall.util.DeckCategoryColorConst.EMERALD
import com.ssoaharison.recall.util.DeckCategoryColorConst.GREEN
import com.ssoaharison.recall.util.DeckCategoryColorConst.GREY
import com.ssoaharison.recall.util.DeckCategoryColorConst.INDIGO
import com.ssoaharison.recall.util.DeckCategoryColorConst.LIME
import com.ssoaharison.recall.util.DeckCategoryColorConst.PINK
import com.ssoaharison.recall.util.DeckCategoryColorConst.PURPLE
import com.ssoaharison.recall.util.DeckCategoryColorConst.RED
import com.ssoaharison.recall.util.DeckCategoryColorConst.ROSE
import com.ssoaharison.recall.util.DeckCategoryColorConst.SKY
import com.ssoaharison.recall.util.DeckCategoryColorConst.TEAL
import com.ssoaharison.recall.util.DeckCategoryColorConst.VIOLET
import com.ssoaharison.recall.util.DeckCategoryColorConst.WHITE
import com.ssoaharison.recall.util.DeckCategoryColorConst.YELLOW
import com.ssoaharison.recall.util.DeckCategoryColorConst.ORANGE
import com.ssoaharison.recall.util.DeckCategoryColorConst.FUCHSIA

class DeckColorCategorySelector {


    private val colors = mapOf(
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

    fun getColors() = colors

    fun getRandomColor() = colors.keys.random()

    fun selectColor(color: String): Int? {
        return if (color in colors.keys) {
            colors[color]
        } else {
            null
        }
    }
}

