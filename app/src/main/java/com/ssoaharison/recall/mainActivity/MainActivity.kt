package com.ssoaharison.recall.mainActivity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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

//        setSupportActionBar(binding.appBarMain.toolbar)
//
//        val navHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
//        val drawerLayout: DrawerLayout = binding.mainActivityRoot
//        val navView: NavigationView = binding.navView
//        val navController = navHostFragment.navController
//
//        appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.deckFragment,
//                R.id.settingsFragment
//            ), drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        drawerLayout = binding.mainActivityRoot
        binding.navView.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.deckFragment, R.id.settingsFragment, R.id.feedbackFragment, R.id.helpFragment),
            drawerLayout
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun getUpdatedBoxLevel(boxLevel: SpaceRepetitionBox) {
        settingsViewModel.updateBoxLevel(boxLevel)
    }

    override fun onSupportNavigateUp(): Boolean {
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
//        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}