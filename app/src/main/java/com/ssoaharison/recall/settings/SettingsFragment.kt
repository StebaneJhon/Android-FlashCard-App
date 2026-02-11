package com.ssoaharison.recall.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.models.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.databinding.FragmentSettingsBinding
import com.ssoaharison.recall.helper.AppThemeHelper
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(), SettingsFragmentEditBoxLevelDialog.SettingsFragmentEditBoxLevelDialogListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private val settingsFragmentViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, SettingsFragmentViewModelFactory(repository))[SettingsFragmentViewModel::class.java]
    }


    private var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    private lateinit var settingsFragmentSpaceRepetitionViewAdapter: SettingsFragmentSpaceRepetitionViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        appContext = container?.context
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()

        binding.settingsTopAppBar.setNavigationOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()
        }

        lifecycleScope.launch {

            settingsFragmentViewModel.getCardCount { sum ->
                binding.tvCardNumber.text = sum.toString()
            }
             settingsFragmentViewModel.getDeckCount { sum ->
                 binding.tvDeckNumber.text = sum.toString()
            }
            settingsFragmentViewModel.getKnownCardCount { sum ->
                binding.tvKnownCardNumber.text = sum.toString()
            }
        }

        lifecycleScope.launch {
            settingsFragmentViewModel.getBox()
            settingsFragmentViewModel.boxLevels
                .collect {
                    when(it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Error -> {
                            Toast.makeText(appContext, it.errorMessage, Toast.LENGTH_LONG).show()
                        }
                        is UiState.Success -> {
                            bindSpaceRepetitionBox(it.data)
                        }
                    }
                }
        }

        when (AppThemeHelper.getSavedTheme(requireContext())) {
            1 -> binding.rgAppThemes.check(R.id.rb_light)
            2 -> binding.rgAppThemes.check(R.id.rb_dark)
            else -> binding.rgAppThemes.check(R.id.rb_system)
        }

        binding.rgAppThemes.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_light -> {
                    AppThemeHelper.saveTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_NO)
                }
                R.id.rb_dark -> {
                    AppThemeHelper.saveTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_YES)
                }
                R.id.rb_system -> {
                    AppThemeHelper.saveTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
         }

        binding.btCreditsAndAttributions.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_creditsAndAttributionsFragment)
        }

    }

    private fun bindSpaceRepetitionBox(boxLevels: List<ImmutableSpaceRepetitionBox>) {
        settingsFragmentSpaceRepetitionViewAdapter =
            SettingsFragmentSpaceRepetitionViewAdapter(appContext!!, boxLevels)
            { lv ->
                onBoxLevelCilcked(lv, boxLevels)
            }

        binding.rvSpaceRepetitionSection.apply {
            layoutManager = GridLayoutManager(requireContext(), 4, GridLayoutManager.VERTICAL, false)
            adapter = settingsFragmentSpaceRepetitionViewAdapter
        }
    }

    fun onBoxLevelCilcked(boxLevel: ImmutableSpaceRepetitionBox, boxLevelList: List<ImmutableSpaceRepetitionBox>) {
        val newDialog = SettingsFragmentEditBoxLevelDialog(boxLevel, boxLevelList)
        newDialog.show(parentFragmentManager, "Update Box Level Dialog")
    }

    override fun getUpdatedBoxLevel(boxLevel: SpaceRepetitionBox) {
        settingsFragmentViewModel.updateBoxLevel(boxLevel)
    }

}