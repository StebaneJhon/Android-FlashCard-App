package com.ssoaharison.recall.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.Model.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.databinding.FragmentSettingsBinding
import com.ssoaharison.recall.util.ContactActions.CONTACT
import com.ssoaharison.recall.util.ContactActions.HELP
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.UiState
import com.ssoaharison.recall.util.ThemeConst.WHITE_THEME
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

    private var appTheme: String? = null
    private lateinit var themePickerAdapter: ThemePickerAdapter

    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

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
        appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            activity?.setTheme(themRef)
        }

        gridLayoutManager = GridLayoutManager(requireContext(), 6, GridLayoutManager.VERTICAL, false)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.settingsTopAppBar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_deckFragment)
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

        lifecycleScope.launch {
            delay(50)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsFragmentViewModel.initThemeSelection(ThemePicker().getThemes(), appTheme ?: WHITE_THEME)
                settingsFragmentViewModel.themSelectionList.collect {listOfTheme ->
                    displayThemes(listOfTheme)
                }
            }
        }

        binding.btPrivacyOthersSection.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
        }
        binding.btAboutOthersSection.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutRecallFragment)
        }
        binding.btHelpOthersSection.setOnClickListener {
            sendEmail(HELP)
        }
        binding.btContactOthersSection.setOnClickListener {
            sendEmail(CONTACT)
        }
        binding.btRateOthersSection.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.error_message_function_not_available), Toast.LENGTH_LONG).show()
        }

        binding.tvThemeSectionTitle.setOnClickListener { v ->
            if (binding.rvSettingsThemePicker.layoutManager == gridLayoutManager) {
                binding.rvSettingsThemePicker.layoutManager = linearLayoutManager
                (v as TextView).setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.icon_expand_more, 0
                )
            } else {
                binding.rvSettingsThemePicker.layoutManager = gridLayoutManager
                (v as TextView).setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.icon_expand_less, 0
                )
            }
        }

    }

    private fun displayThemes(listOfTheme: ArrayList<ThemeModel>) {
        themePickerAdapter = ThemePickerAdapter(
            requireContext(),
            listOfTheme
        ) {selectedTheme ->
            settingsFragmentViewModel.selectTheme(selectedTheme.themeId)
            setAppTheme(selectedTheme.themeId)
            updateAppTheme()
            themePickerAdapter.notifyDataSetChanged()
        }
        binding.rvSettingsThemePicker.apply {
            adapter = themePickerAdapter
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
        }
    }

    private fun sendEmail(subject: String) {
        val action = SettingsFragmentDirections.actionSettingsFragmentToEmailFragment(subject)
        findNavController().navigate(
            action,
            NavOptions.Builder().setPopUpTo(R.id.emailFragment, true).build()
        )
    }

    private fun bindSpaceRepetitionBox(boxLevels: List<ImmutableSpaceRepetitionBox>) {
        settingsFragmentSpaceRepetitionViewAdapter =
            SettingsFragmentSpaceRepetitionViewAdapter(appContext!!, boxLevels)
            { lv ->
                onBoxLevelCilcked(lv, boxLevels)
            }

        binding.rvSpaceRepetitionSection.apply {
            layoutManager = LinearLayoutManager(
                appContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = settingsFragmentSpaceRepetitionViewAdapter
        }
    }

    fun onBoxLevelCilcked(boxLevel: ImmutableSpaceRepetitionBox, boxLevelList: List<ImmutableSpaceRepetitionBox>) {
        val newDialog = SettingsFragmentEditBoxLevelDialog(boxLevel, boxLevelList)
        newDialog.show(parentFragmentManager, "Update Box Level Dialog")
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

    override fun getUpdatedBoxLevel(boxLevel: SpaceRepetitionBox) {
        settingsFragmentViewModel.updateBoxLevel(boxLevel)
    }

}