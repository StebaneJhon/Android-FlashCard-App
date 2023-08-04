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
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.themeConst.BLUE_THEME
import com.example.flashcard.util.themeConst.BROWN_THEME
import com.example.flashcard.util.themeConst.DARK_THEME
import com.example.flashcard.util.themeConst.GREEN_THEME
import com.example.flashcard.util.themeConst.PINK_THEME
import com.example.flashcard.util.themeConst.PURPLE_THEME
import com.example.flashcard.util.themeConst.RED_THEME
import com.example.flashcard.util.themeConst.TEAL_THEME
import com.example.flashcard.util.themeConst.WHITE_THEME
import com.example.flashcard.util.themeConst.YELLOW_THEME

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
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            activity?.setTheme(themRef)
        }

        binding.blackThemeButton.setOnClickListener {
            setAppTheme(DARK_THEME)
            updateAppTheme()
        }

        binding.whiteThemeButton.setOnClickListener {
            setAppTheme(WHITE_THEME)
            updateAppTheme()
        }

        binding.purpleThemeButton.setOnClickListener {
            setAppTheme(PURPLE_THEME)
            updateAppTheme()
        }

        binding.blueThemeButton.setOnClickListener {
            setAppTheme(BLUE_THEME)
            updateAppTheme()
        }

        binding.pinkThemeButton.setOnClickListener {
            setAppTheme(PINK_THEME)
            updateAppTheme()
        }

        binding.redThemeButton.setOnClickListener {
            setAppTheme(RED_THEME)
            updateAppTheme()
        }

        binding.tealThemeButton.setOnClickListener {
            setAppTheme(TEAL_THEME)
            updateAppTheme()
        }

        binding.greenThemeButton.setOnClickListener {
            setAppTheme(GREEN_THEME)
            updateAppTheme()
        }

        binding.yellowThemeButton.setOnClickListener {
            setAppTheme(YELLOW_THEME)
            updateAppTheme()
        }

        binding.browneThemeButton.setOnClickListener {
            setAppTheme(BROWN_THEME)
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

}