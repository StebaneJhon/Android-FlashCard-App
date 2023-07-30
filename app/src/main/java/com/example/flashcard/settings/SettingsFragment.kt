package com.example.flashcard.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flashcard.R
import com.example.flashcard.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null

    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        appContext = container?.context
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = getThem(appTheme)
        activity?.setTheme(themRef)

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
        activity?.recreate()
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

    fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1.equals("color_option")) {
            activity?.recreate()
        }
    }

}