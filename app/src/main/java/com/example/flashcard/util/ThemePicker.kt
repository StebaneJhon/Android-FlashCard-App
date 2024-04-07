package com.example.flashcard.util

import com.example.flashcard.R

class ThemePicker {

    private val themes = mapOf(
        themeConst.DARK_THEME to R.style.DarkTheme_FlashCard,
        themeConst.PURPLE_THEME to R.style.PurpleTheme_Flashcard,
        themeConst.BLUE_THEME to R.style.BlueTheme_Flashcard,
        themeConst.PINK_THEME to R.style.PinkTheme_Flashcard,
        themeConst.RED_THEME to R.style.RedTheme_Flashcard,
        themeConst.TEAL_THEME to R.style.TealTheme_Flashcard,
        themeConst.GREEN_THEME to R.style.GreenTheme_Flashcard,
        themeConst.YELLOW_THEME to R.style.YellowTheme_Flashcard,
        themeConst.BROWN_THEME to R.style.BrownTheme_Flashcard,
        themeConst.WHITE_THEME to R.style.StaleTheme_FlashCard,
    )

    fun selectTheme(themeName: String): Int? {
        return if (themeName in themes.keys) {
            themes[themeName]
        } else {
            null
        }
    }

}