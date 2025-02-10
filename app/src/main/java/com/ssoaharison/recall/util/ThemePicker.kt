package com.ssoaharison.recall.util

import com.ssoaharison.recall.R
import com.ssoaharison.recall.util.DeckCategoryColorConst.BLACK
import com.ssoaharison.recall.util.DeckCategoryColorConst.BLUE
import com.ssoaharison.recall.util.DeckCategoryColorConst.CYAN
import com.ssoaharison.recall.util.DeckCategoryColorConst.EMERALD
import com.ssoaharison.recall.util.DeckCategoryColorConst.FUCHSIA
import com.ssoaharison.recall.util.DeckCategoryColorConst.GREEN
import com.ssoaharison.recall.util.DeckCategoryColorConst.GREY
import com.ssoaharison.recall.util.DeckCategoryColorConst.INDIGO
import com.ssoaharison.recall.util.DeckCategoryColorConst.LIME
import com.ssoaharison.recall.util.DeckCategoryColorConst.ORANGE
import com.ssoaharison.recall.util.DeckCategoryColorConst.PINK
import com.ssoaharison.recall.util.DeckCategoryColorConst.PURPLE
import com.ssoaharison.recall.util.DeckCategoryColorConst.RED
import com.ssoaharison.recall.util.DeckCategoryColorConst.ROSE
import com.ssoaharison.recall.util.DeckCategoryColorConst.SKY
import com.ssoaharison.recall.util.DeckCategoryColorConst.TEAL
import com.ssoaharison.recall.util.DeckCategoryColorConst.VIOLET
import com.ssoaharison.recall.util.DeckCategoryColorConst.WHITE
import com.ssoaharison.recall.util.DeckCategoryColorConst.YELLOW
import com.ssoaharison.recall.util.ThemeConst.AMBER_THEME
import com.ssoaharison.recall.util.ThemeConst.BLUE_THEME
import com.ssoaharison.recall.util.ThemeConst.BROWN_THEME
import com.ssoaharison.recall.util.ThemeConst.CYAN_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_AMBER_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_BLUE_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_BROWN_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_CYAN_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_DARK_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_EMERALD_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_FUCHSIA_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_GREEN_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_INDIGO_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_LIME_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_ORANGE_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_PINK_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_PURPLE_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_RED_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_ROSE_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_SKY_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_TEAL_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_VIOLET_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_WHITE_THEME
import com.ssoaharison.recall.util.ThemeConst.DARK_YELLOW_THEME
import com.ssoaharison.recall.util.ThemeConst.EMERALD_THEME
import com.ssoaharison.recall.util.ThemeConst.FUCHSIA_THEME
import com.ssoaharison.recall.util.ThemeConst.GREEN_THEME
import com.ssoaharison.recall.util.ThemeConst.INDIGO_THEME
import com.ssoaharison.recall.util.ThemeConst.LIME_THEME
import com.ssoaharison.recall.util.ThemeConst.ORANGE_THEME
import com.ssoaharison.recall.util.ThemeConst.PINK_THEME
import com.ssoaharison.recall.util.ThemeConst.PURPLE_THEME
import com.ssoaharison.recall.util.ThemeConst.RED_THEME
import com.ssoaharison.recall.util.ThemeConst.ROSE_THEME
import com.ssoaharison.recall.util.ThemeConst.SKY_THEME
import com.ssoaharison.recall.util.ThemeConst.TEAL_THEME
import com.ssoaharison.recall.util.ThemeConst.VIOLET_THEME
import com.ssoaharison.recall.util.ThemeConst.WHITE_THEME
import com.ssoaharison.recall.util.ThemeConst.YELLOW_THEME

class ThemePicker {

    private val themes = mapOf(
        DARK_THEME to R.style.DarkTheme_FlashCard,
        WHITE_THEME to R.style.StaleTheme_FlashCard,
        RED_THEME to R.style.RedTheme_Flashcard,
        ORANGE_THEME to R.style.OrangeTheme_Flashcard,
        AMBER_THEME to R.style.AmberTheme_Flashcard,
        YELLOW_THEME to R.style.YellowTheme_Flashcard,
        LIME_THEME to R.style.LimeTheme_Flashcard,
        GREEN_THEME to R.style.GreenTheme_Flashcard,
        EMERALD_THEME to R.style.EmeraldTheme_Flashcard,
        TEAL_THEME to R.style.TealTheme_Flashcard,
        CYAN_THEME to R.style.CyanTheme_Flashcard,
        SKY_THEME to R.style.SkyTheme_Flashcard,
        BLUE_THEME to R.style.BlueTheme_Flashcard,
        INDIGO_THEME to R.style.IndigoTheme_Flashcard,
        VIOLET_THEME to R.style.VioletTheme_Flashcard,
        PURPLE_THEME to R.style.PurpleTheme_Flashcard,
        FUCHSIA_THEME to R.style.FuchsiaTheme_Flashcard,
        PINK_THEME to R.style.PinkTheme_Flashcard,
        ROSE_THEME to R.style.RoseTheme_Flashcard,
        BROWN_THEME to R.style.BrownTheme_Flashcard,
    )

    private val darkThemes = mapOf(
        DARK_DARK_THEME to R.style.DarkTheme_FlashCard,
        DARK_WHITE_THEME to R.style.StaleTheme_FlashCard,
        DARK_RED_THEME to R.style.DarkRedTheme_Flashcard,
        DARK_ORANGE_THEME to R.style.DarkOrangeTheme_Flashcard,
        DARK_AMBER_THEME to R.style.DarkAmberTheme_Flashcard,
        DARK_YELLOW_THEME to R.style.DarkYellowTheme_Flashcard,
        DARK_LIME_THEME to R.style.DarkLimeTheme_Flashcard,
        DARK_GREEN_THEME to R.style.DarkGreenTheme_Flashcard,
        DARK_EMERALD_THEME to R.style.DarkEmeraldTheme_Flashcard,
        DARK_TEAL_THEME to R.style.DarkTealTheme_Flashcard,
        DARK_CYAN_THEME to R.style.DarkCyanTheme_Flashcard,
        DARK_SKY_THEME to R.style.DarkSkyTheme_Flashcard,
        DARK_BLUE_THEME to R.style.DarkBlueTheme_Flashcard,
        DARK_INDIGO_THEME to R.style.DarkIndigoTheme_Flashcard,
        DARK_VIOLET_THEME to R.style.DarkVioletTheme_Flashcard,
        DARK_PURPLE_THEME to R.style.DarkPurpleTheme_Flashcard,
        DARK_FUCHSIA_THEME to R.style.DarkFuchsiaTheme_Flashcard,
        DARK_PINK_THEME to R.style.DarkPinkTheme_Flashcard,
        DARK_ROSE_THEME to R.style.DarkRoseTheme_Flashcard,
        DARK_BROWN_THEME to R.style.DarkBrownTheme_Flashcard,
    )

    private val themeBaseColors = mapOf(
        WHITE_THEME to R.color.white,
        DARK_THEME to R.color.black,
        RED_THEME to R.color.red600,
        ORANGE_THEME to R.color.orange600,
        AMBER_THEME to R.color.amber600,
        YELLOW_THEME to R.color.yellow600,
        LIME_THEME to R.color.lime600,
        GREEN_THEME to R.color.green600,
        EMERALD_THEME to R.color.emerald600,
        TEAL_THEME to R.color.teal600,
        CYAN_THEME to R.color.cyan600,
        SKY_THEME to R.color.sky600,
        BLUE_THEME to R.color.blue600,
        INDIGO_THEME to R.color.indigo600,
        VIOLET_THEME to R.color.violet600,
        PURPLE_THEME to R.color.purple600,
        FUCHSIA_THEME to R.color.fuchsia600,
        PINK_THEME to R.color.pink600,
        ROSE_THEME to R.color.rose600,
        BROWN_THEME to R.color.stone700,
    )

    fun getThemeBaseColor(themeName: String): Int? {
        return if (themeName in themeBaseColors.keys) {
            themeBaseColors[themeName]
        } else {
            null
        }
    }

    fun selectTheme(themeName: String?): Int? {
        return if (themeName in themes.keys) {
            themes[themeName]
        } else {
            null
        }
    }

    fun getThemes() = themes

    fun getDefaultTheme() = R.style.StaleTheme_FlashCard

    fun selectThemeByDeckColorCode(deckColorCode: String, defaultTheme: Int): Int {
        return when (deckColorCode) {
            WHITE -> themes.getOrDefault(WHITE_THEME, defaultTheme)
            BLACK -> themes.getOrDefault(DARK_THEME, defaultTheme)
            GREY -> themes.getOrDefault(WHITE_THEME, defaultTheme)
            RED -> themes.getOrDefault(RED_THEME, defaultTheme)
            ORANGE -> themes.getOrDefault(ORANGE_THEME, defaultTheme)
            YELLOW -> themes.getOrDefault(YELLOW_THEME, defaultTheme)
            LIME -> themes.getOrDefault(LIME_THEME, defaultTheme)
            GREEN -> themes.getOrDefault(GREEN_THEME, defaultTheme)
            EMERALD -> themes.getOrDefault(EMERALD_THEME, defaultTheme)
            TEAL -> themes.getOrDefault(TEAL_THEME, defaultTheme)
            CYAN -> themes.getOrDefault(CYAN_THEME, defaultTheme)
            SKY -> themes.getOrDefault(SKY_THEME, defaultTheme)
            BLUE -> themes.getOrDefault(BLUE_THEME, defaultTheme)
            INDIGO -> themes.getOrDefault(INDIGO_THEME, defaultTheme)
            VIOLET -> themes.getOrDefault(VIOLET_THEME, defaultTheme)
            PURPLE -> themes.getOrDefault(PURPLE_THEME, defaultTheme)
            FUCHSIA -> themes.getOrDefault(FUCHSIA_THEME, defaultTheme)
            PINK -> themes.getOrDefault(PINK_THEME, defaultTheme)
            ROSE -> themes.getOrDefault(ROSE_THEME, defaultTheme)
            else -> themes.getOrDefault(WHITE_THEME, defaultTheme)
        }
    }

    fun selectDarkThemeByDeckColorCode(deckColorCode: String, defaultTheme: Int): Int {
        return when (deckColorCode) {
            WHITE -> darkThemes.getOrDefault(DARK_WHITE_THEME, defaultTheme)
            BLACK -> darkThemes.getOrDefault(DARK_DARK_THEME, defaultTheme)
            GREY -> darkThemes.getOrDefault(DARK_WHITE_THEME, defaultTheme)
            RED -> darkThemes.getOrDefault(DARK_RED_THEME, defaultTheme)
            ORANGE -> darkThemes.getOrDefault(DARK_ORANGE_THEME, defaultTheme)
            YELLOW -> darkThemes.getOrDefault(DARK_YELLOW_THEME, defaultTheme)
            LIME -> darkThemes.getOrDefault(DARK_LIME_THEME, defaultTheme)
            GREEN -> darkThemes.getOrDefault(DARK_GREEN_THEME, defaultTheme)
            EMERALD -> darkThemes.getOrDefault(DARK_EMERALD_THEME, defaultTheme)
            TEAL -> darkThemes.getOrDefault(DARK_TEAL_THEME, defaultTheme)
            CYAN -> darkThemes.getOrDefault(DARK_CYAN_THEME, defaultTheme)
            SKY -> darkThemes.getOrDefault(DARK_SKY_THEME, defaultTheme)
            BLUE -> darkThemes.getOrDefault(DARK_BLUE_THEME, defaultTheme)
            INDIGO -> darkThemes.getOrDefault(DARK_INDIGO_THEME, defaultTheme)
            VIOLET -> darkThemes.getOrDefault(DARK_VIOLET_THEME, defaultTheme)
            PURPLE -> darkThemes.getOrDefault(DARK_PURPLE_THEME, defaultTheme)
            FUCHSIA -> darkThemes.getOrDefault(DARK_FUCHSIA_THEME, defaultTheme)
            PINK -> darkThemes.getOrDefault(DARK_PINK_THEME, defaultTheme)
            ROSE -> darkThemes.getOrDefault(DARK_ROSE_THEME, defaultTheme)
            else -> darkThemes.getOrDefault(DARK_WHITE_THEME, defaultTheme)
        }
    }

}