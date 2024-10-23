package com.ssoaharison.recall.util

import com.ssoaharison.recall.R

class ThemePicker {

    private val themes = mapOf(
        ThemeConst.DARK_THEME to R.style.DarkTheme_FlashCard,
        ThemeConst.WHITE_THEME to R.style.StaleTheme_FlashCard,
        ThemeConst.RED_THEME to R.style.RedTheme_Flashcard,
        ThemeConst.ORANGE_THEME to R.style.OrangeTheme_Flashcard,
        ThemeConst.AMBER_THEME to R.style.AmberTheme_Flashcard,
        ThemeConst.YELLOW_THEME to R.style.YellowTheme_Flashcard,
        ThemeConst.LIME_THEME to R.style.LimeTheme_Flashcard,
        ThemeConst.GREEN_THEME to R.style.GreenTheme_Flashcard,
        ThemeConst.EMERALD_THEME to R.style.EmeraldTheme_Flashcard,
        ThemeConst.TEAL_THEME to R.style.TealTheme_Flashcard,
        ThemeConst.CYAN_THEME to R.style.CyanTheme_Flashcard,
        ThemeConst.SKY_THEME to R.style.SkyTheme_Flashcard,
        ThemeConst.BLUE_THEME to R.style.BlueTheme_Flashcard,
        ThemeConst.INDIGO_THEME to R.style.IndigoTheme_Flashcard,
        ThemeConst.VIOLET_THEME to R.style.VioletTheme_Flashcard,
        ThemeConst.PURPLE_THEME to R.style.PurpleTheme_Flashcard,
        ThemeConst.FUCHSIA_THEME to R.style.FuchsiaTheme_Flashcard,
        ThemeConst.PINK_THEME to R.style.PinkTheme_Flashcard,
        ThemeConst.ROSE_THEME to R.style.RoseTheme_Flashcard,
        ThemeConst.BROWN_THEME to R.style.BrownTheme_Flashcard,
    )

    private val themeBaseColors = mapOf(
        ThemeConst.WHITE_THEME to R.color.white,
        ThemeConst.DARK_THEME to R.color.black,
        ThemeConst.RED_THEME to R.color.red600,
        ThemeConst.ORANGE_THEME to R.color.orange600,
        ThemeConst.AMBER_THEME to R.color.amber600,
        ThemeConst.YELLOW_THEME to R.color.yellow600,
        ThemeConst.LIME_THEME to R.color.lime600,
        ThemeConst.GREEN_THEME to R.color.green600,
        ThemeConst.EMERALD_THEME to R.color.emerald600,
        ThemeConst.TEAL_THEME to R.color.teal600,
        ThemeConst.CYAN_THEME to R.color.cyan600,
        ThemeConst.SKY_THEME to R.color.sky600,
        ThemeConst.BLUE_THEME to R.color.blue600,
        ThemeConst.INDIGO_THEME to R.color.indigo600,
        ThemeConst.VIOLET_THEME to R.color.violet600,
        ThemeConst.PURPLE_THEME to R.color.purple600,
        ThemeConst.FUCHSIA_THEME to R.color.fuchsia600,
        ThemeConst.PINK_THEME to R.color.pink600,
        ThemeConst.ROSE_THEME to R.color.rose600,
        ThemeConst.BROWN_THEME to R.color.brown700,
    )

    fun getThemeBaseColor(themeName: String): Int? {
        return if (themeName in themeBaseColors.keys) {
            themeBaseColors[themeName]
        } else {
            null
        }
    }

    fun selectTheme(themeName: String): Int? {
        return if (themeName in themes.keys) {
            themes[themeName]
        } else {
            null
        }
    }

    fun getThemes() = themes

}