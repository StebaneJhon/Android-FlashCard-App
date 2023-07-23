package com.example.flashcard.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.flashcard.R
import com.example.flashcard.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivitySettingsBinding

    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = getThem(appTheme)
        setTheme(themRef)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.blackThemeButton.setOnClickListener {
            setAppTheme("DARK THEME")
            updateAppTheme()
        }

        binding.whiteThemeButton.setOnClickListener {
            setAppTheme("WHITE THEME")
            updateAppTheme()
        }

        binding.purpleThemeButton.setOnClickListener {
            setAppTheme("PURPLE THEME")
            updateAppTheme()
        }

    }

    private fun updateAppTheme() {
        this.recreate()
    }

    private fun setAppTheme(themName: String) {
        editor?.apply {
            putString("themName", themName)
            apply()
        }
    }

    private fun getThem(themeName: String?): Int {
        return when (themeName) {
            "DARK THEME" -> R.style.DarkTheme_FlashCard
            "PURPLE THEME" -> R.style.PurpleTheme_Flashcard
            else -> R.style.Theme_FlashCard
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1.equals("color_option")) {
            this.recreate()
        }
    }
}