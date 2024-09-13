package com.example.flashcard.mainActivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.databinding.ActivityMainBinding
import com.example.flashcard.backend.entities.SpaceRepetitionBox
import com.example.flashcard.settings.SettingsFragmentEditBoxLevelDialog
import com.example.flashcard.settings.SettingsFragmentViewModel
import com.example.flashcard.settings.SettingsFragmentViewModelFactory
import com.example.flashcard.util.ThemePicker

class MainActivity :
    AppCompatActivity(),
    SettingsFragmentEditBoxLevelDialog.SettingsFragmentEditBoxLevelDialogListener {

    private lateinit var binding: ActivityMainBinding

    private val settingsViewModel: SettingsFragmentViewModel by viewModels {
        SettingsFragmentViewModelFactory((application as FlashCardApplication).repository)
    }

    private val activityViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory((application as FlashCardApplication).repository)
    }
    lateinit var navController: NavController

    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private val SETTINGS_CODE = 12334

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                activityViewModel.isLoading.value
            }
        }

        setContentView(view)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        binding.nvvDrawer.setupWithNavController(navController)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CODE) {
            this.recreate()
        }
    }

    override fun getUpdatedBoxLevel(boxLevel: SpaceRepetitionBox) {
        settingsViewModel.updateBoxLevel(boxLevel)
    }
}