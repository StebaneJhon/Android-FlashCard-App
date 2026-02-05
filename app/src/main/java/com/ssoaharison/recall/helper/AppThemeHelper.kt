package com.ssoaharison.recall.helper

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object AppThemeHelper {

    private const val PREFS_NAME = "settingsPref"
    private const val KEY_THEME = "selected_theme"

    fun applyTheme(context: Context) {
        val theme = getSavedTheme(context)
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    fun saveTheme(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putInt(KEY_THEME, mode)
            }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getSavedTheme(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun isSystemDarkTheme(context: Context): Boolean {
        val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
    }

}