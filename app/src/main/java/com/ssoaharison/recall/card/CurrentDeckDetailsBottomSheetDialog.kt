package com.ssoaharison.recall.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.databinding.BottomSheetDialogCurrentDeckDetailsBinding
import com.ssoaharison.recall.util.ColorModel
import com.ssoaharison.recall.helper.AppThemeHelper
import com.ssoaharison.recall.helper.DeckColorCategorySelector
import com.ssoaharison.recall.util.DeckColorPickerAdapter
import com.ssoaharison.recall.helper.LanguageUtil
import com.ssoaharison.recall.util.MAIN_DECK_ID
import com.ssoaharison.recall.helper.parcelable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class CurrentDeckDetailsBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetDialogCurrentDeckDetailsBinding

    private val currentDeck: ExternalDeck by lazy {
        requireArguments().parcelable<ExternalDeck>(ARG_CURRENT_DECK)!!
    }

    private val currentDeckDetailsViewModel: CurrentDeckDetailsViewModel by viewModels()

    private lateinit var deckColorPickerAdapter: DeckColorPickerAdapter

    private val supportedLanguages = LanguageUtil().getSupportedLang()

    private lateinit var contentLanguagePopupWindow: ListPopupWindow
    private lateinit var  definitionLanguagePopupWindow: ListPopupWindow

    companion object {
        const val TAG = "CurrentDeckDetailsBottomSheetDialog"
        const val ARG_CURRENT_DECK = "current_deck"
        const val CURRENT_DECK_DETAILS_REQUEST_CODE = "1100"
        const val CURRENT_DECK_DETAILS_BUNDLE_KEY = "11"
        fun newInstance(currentDeck: ExternalDeck) = CurrentDeckDetailsBottomSheetDialog().apply {
            arguments = bundleOf(ARG_CURRENT_DECK to currentDeck)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDialogCurrentDeckDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deckColorCategorySelector = DeckColorCategorySelector()

        contentLanguagePopupWindow = ListPopupWindow(requireContext(), null, androidx.appcompat.R.attr.popupWindowStyle)
        definitionLanguagePopupWindow = ListPopupWindow(requireContext(), null, androidx.appcompat.R.attr.popupWindowStyle)

        lifecycleScope.launch {
            delay(50)
            val colors =  when (AppThemeHelper.getSavedTheme(requireContext())) {
                1 -> {
                    deckColorCategorySelector.getColors()
                }
                2 -> {
                    deckColorCategorySelector.getDarkColors()
                }
                else -> {
                    if (AppThemeHelper.isSystemDarkTheme(requireContext())) {
                        deckColorCategorySelector.getDarkColors()
                    } else {
                        deckColorCategorySelector.getColors()
                    }
                }
            }
            currentDeckDetailsViewModel.initColorSelection(colors, currentDeck.deckColorCode)
            currentDeckDetailsViewModel.colorSelectionList.collect { colors ->
                displayColorPicker(colors, currentDeck.deckId != MAIN_DECK_ID)
            }
        }

        binding.btEditDeckName.apply {
            text = currentDeck.deckName
            setOnClickListener {
                if (currentDeck.deckId != MAIN_DECK_ID) {
                    EditDeckNameDialog.newInstance(currentDeck.deckName).show(childFragmentManager, EditDeckNameDialog.TAG)
                    childFragmentManager.setFragmentResultListener(
                        EditDeckNameDialog.CURRENT_DECK_NAME_REQUEST_CODE,
                        this@CurrentDeckDetailsBottomSheetDialog
                    ) {_, bundle ->
                        val data = bundle.getString(EditDeckNameDialog.CURRENT_DECK_NAME_BUNDLE_CODE)
                        data?.let { newDeckName ->
                            val updatedDeck = currentDeck.copy(deckName = newDeckName)
                            onUpdateCurrentDeck(updatedDeck)
                        }
                    }
                } else {
                    Toast.makeText(context, getString(R.string.error_message_cannot_edit_main_deck_name), Toast.LENGTH_SHORT).show()
                }
            }
        }

        val arrayAdapterSupportedLanguages = ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)
        contentLanguagePopupWindow.apply {
            anchorView = binding.llContentLanguageContainer
            setAdapter(arrayAdapterSupportedLanguages)
            setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val updatedDeck = currentDeck.copy( cardContentDefaultLanguage = supportedLanguages[position])
                onUpdateCurrentDeck(updatedDeck)
            }
        }

        definitionLanguagePopupWindow.apply {
            anchorView = binding.llDefinitionLanguageContainer
            setAdapter(arrayAdapterSupportedLanguages)
            setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val updatedDeck = currentDeck.copy( cardDefinitionDefaultLanguage = supportedLanguages[position])
                onUpdateCurrentDeck(updatedDeck)
            }
        }

        binding.inDeckContentLanguage.apply {
            tvHeader.text = getString(R.string.text_content)
            tvLanguage.text = currentDeck.cardContentDefaultLanguage ?: getString(R.string.text_definition_language)
        }

        binding.llContentLanguageContainer.setOnClickListener {
            if (contentLanguagePopupWindow.isShowing) {
                contentLanguagePopupWindow.dismiss()
            } else {
                contentLanguagePopupWindow.show()
            }
        }

        binding.inDeckDefinitionLanguage.apply {
            tvHeader.text = getString(R.string.text_definition)
            tvLanguage.text = currentDeck.cardDefinitionDefaultLanguage ?: getString(R.string.text_definition_language)
        }
        binding.llDefinitionLanguageContainer.setOnClickListener {
            if (definitionLanguagePopupWindow.isShowing) {
                definitionLanguagePopupWindow.dismiss()
            } else {
                definitionLanguagePopupWindow.show()

            }
        }



        binding.deckFirstLanguageET.apply {
            setAdapter(arrayAdapterSupportedLanguages)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.filter_spinner_dropdown_background,
                    requireActivity().theme
                )
            )
        }
        binding.deckSecondLanguageET.apply {
            setAdapter(arrayAdapterSupportedLanguages)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.filter_spinner_dropdown_background,
                    requireActivity().theme
                )
            )
        }

    }

    private fun displayColorPicker(
        colors: List<ColorModel>,
        isItemsClickable: Boolean = true,
    ) {
        deckColorPickerAdapter = DeckColorPickerAdapter(
            context = requireContext(),
            listOfColors = colors,
            isItemsClickable = isItemsClickable,
            onColorClicked = { color ->
                currentDeckDetailsViewModel.selectColor(color.id)
                val updatedDeck = currentDeck.copy(deckColorCode = color.id)
                onUpdateCurrentDeck(updatedDeck)
            }
        )
        binding.rvDeckColorPicker.apply {
            adapter = deckColorPickerAdapter
            layoutManager = GridLayoutManager(requireContext(), 6, GridLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun onUpdateCurrentDeck(updatedDeck: ExternalDeck) {
        parentFragmentManager.setFragmentResult(CURRENT_DECK_DETAILS_REQUEST_CODE, bundleOf(CURRENT_DECK_DETAILS_BUNDLE_KEY to updatedDeck))
        dismiss()
    }

}