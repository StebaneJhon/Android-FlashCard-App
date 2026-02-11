package com.ssoaharison.recall.mainActivity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
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

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var binding: ActivityMainBinding

    private val settingsViewModel: SettingsFragmentViewModel by viewModels {
        SettingsFragmentViewModelFactory((application as FlashCardApplication).repository)
    }
    private val activityViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory((application as FlashCardApplication).repository)
    }

    val deckPathViewModel: DeckPathViewModel by viewModels {
        DeckPathViewModelFactory((application as FlashCardApplication).repository)
    }

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

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                activityViewModel.isLoading.value
            }
        }

        setContentView(binding.root)

        WindowCompat.enableEdgeToEdge(window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.vwSpaceTop) { v, windowInserts ->
            val insets = windowInserts.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        drawerLayout = binding.mainActivityRoot
        binding.navView.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.settingsFragment, R.id.feedbackFragment, R.id.helpFragment, R.id.privacyPolicyFragment),
            drawerLayout
        )
    }

    override fun getUpdatedBoxLevel(boxLevel: SpaceRepetitionBox) {
        settingsViewModel.updateBoxLevel(boxLevel)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}