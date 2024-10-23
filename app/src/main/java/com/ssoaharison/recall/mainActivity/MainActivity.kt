package com.ssoaharison.recall.mainActivity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.databinding.ActivityMainBinding
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.settings.SettingsFragmentEditBoxLevelDialog
import com.ssoaharison.recall.settings.SettingsFragmentViewModel
import com.ssoaharison.recall.settings.SettingsFragmentViewModelFactory
import com.ssoaharison.recall.util.ThemePicker

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

    private var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

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
    }

    override fun getUpdatedBoxLevel(boxLevel: SpaceRepetitionBox) {
        settingsViewModel.updateBoxLevel(boxLevel)
    }
}