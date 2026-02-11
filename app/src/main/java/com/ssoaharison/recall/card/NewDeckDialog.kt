package com.ssoaharison.recall.card

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.button.MaterialButton
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.databinding.AddDeckLayoutDialogBinding
import com.ssoaharison.recall.util.DeckAdditionAction.ADD
import com.ssoaharison.recall.helper.DeckColorCategorySelector
import com.ssoaharison.recall.helper.LanguageUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.helper.AppThemeHelper
import com.ssoaharison.recall.util.ColorModel
import com.ssoaharison.recall.util.DeckColorPickerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NewDeckDialog(
    val deckToEdit: ExternalDeck?,
    val parentDeckId: String?,
    val appTheme: String,
) : AppCompatDialogFragment() {

    private var _binding: AddDeckLayoutDialogBinding? = null
    private val binding get() = _binding!!

    private var deckCategoryColor: String? = null
    private val supportedLanguages = LanguageUtil().getSupportedLang()
    private lateinit var deckColorPickerAdapter: DeckColorPickerAdapter

    private val newDeckDialogViewModel: NewDeckDialogViewModel by viewModels()

    companion object {
        const val TAG = "NewDeckDialog"
        const val SAVE_DECK_BUNDLE_KEY = "1"
        const val EDIT_DECK_BUNDLE_KEY = "2"
        const val REQUEST_CODE = "0"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = AddDeckLayoutDialogBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        val arrayAdapterSupportedLanguages = ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)
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
        binding.btShowDetailOptions.setOnClickListener { bt ->
            showDetailOptions((bt as MaterialButton), !binding.deckFirstLanguageLY.isVisible)
        }

        if (deckToEdit != null) {
            binding.btAddCard.isVisible = false
            binding.tvTitle.text = getString(R.string.tv_update_deck)
            binding.deckNameET.setText(deckToEdit.deckName)
            binding.deckDescriptionET.setText(deckToEdit.deckDescription)
            binding.deckFirstLanguageET.setText(deckToEdit.cardContentDefaultLanguage)
            binding.deckSecondLanguageET.setText(deckToEdit.cardDefinitionDefaultLanguage)
            deckToEdit.deckBackground?.let { newDeckDialogViewModel.selectColor(it) }
            deckCategoryColor = deckToEdit.deckBackground

            builder.setView(binding.root)


            binding.btExit.setOnClickListener { dismiss() }
            binding.dialogPositiveBT.apply {
                text = getString(R.string.bt_text_update)
                setOnClickListener {
                    onUpdate(deckToEdit)
                }
                binding.btAddTop.setOnClickListener {
                    onUpdate(deckToEdit)
                }
            }
        } else {
            builder.setView(binding.root)
//            binding.btAddCard.isVisible = true
            binding.tvTitle.text = getString(R.string.tv_add_new_deck)
            binding.btExit.setOnClickListener { dismiss() }
            binding.dialogPositiveBT.apply {
                text = getString(R.string.bt_text_add)
                setOnClickListener {
                    onAdd(parentDeckId)
                }
            }
            binding.btAddTop.setOnClickListener {
                onAdd(parentDeckId)
            }
//            binding.btAddCard.setOnClickListener {
//                if (!checkError()) {
//                    val newDeck = Deck(
//                        deckId = UUID.randomUUID().toString(),
//                        parentDeckId = parentDeckId,
//                        deckName = binding.deckNameET.text.toString(),
//                        deckDescription = binding.deckDescriptionET.text.toString(),
//                        cardContentDefaultLanguage = binding.deckFirstLanguageET.text.toString(),
//                        cardDefinitionDefaultLanguage = binding.deckSecondLanguageET.text.toString(),
//                        deckColorCode = deckCategoryColor,
//                        deckCategory = null,
//                        isFavorite = 0,
//                        deckCreationDate = now()
//                    )
//                    sendDeckOnSave(REQUEST_CODE, ADD_DECK_FORWARD_TO_CARD_ADDITION, newDeck)
//                    dismiss()
//                }
//            }
        }

        lifecycleScope.launch {
            delay(50)
            val deckColorCategorySelector = DeckColorCategorySelector()
            val deckColors = when (AppThemeHelper.getSavedTheme(requireContext())) {
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

//            if (appTheme == DARK_THEME) {
//                deckColorCategorySelector.getDarkColors()
//            } else {
//                deckColorCategorySelector.getColors()
//            }
            newDeckDialogViewModel.initColorSelection(deckColors, deckCategoryColor)
            newDeckDialogViewModel.colorSelectionList.collect { listOfColors ->
                displayColorPicker(listOfColors)
                binding.rvDeckColorPicker.visibility = View.GONE
            }
        }

        builder.setView(binding.root)
        return builder.create()
    }

    private fun showDetailOptions(bt: MaterialButton, isShown: Boolean) {
        if (isShown) {
            bt.setIconResource(R.drawable.icon_expand_less)
        } else {
            bt.setIconResource(R.drawable.icon_expand_more)
        }
        binding.deckFirstLanguageLY.isVisible = isShown
        binding.deckSecondLanguageLY.isVisible = isShown
        binding.colorPickerTitle.isVisible = isShown
        binding.rvDeckColorPicker.isVisible = isShown

    }

    private fun onAdd(parentDeckId: String?) {
        if (!checkError()) {
            val deckFirstLanguage = binding.deckFirstLanguageET.text
            val deckSecondLanguage = binding.deckSecondLanguageET.text
            val newDeck = Deck(
                deckId = UUID.randomUUID().toString(),
                parentDeckId = parentDeckId,
                deckName = binding.deckNameET.text.toString(),
                deckDescription = binding.deckDescriptionET.text.toString(),
                cardContentDefaultLanguage = if (deckFirstLanguage.isBlank()) null else deckFirstLanguage.toString(),
                cardDefinitionDefaultLanguage = if (deckSecondLanguage.isBlank()) null else deckSecondLanguage.toString(),
                deckBackground = deckCategoryColor,
                deckCategory = null,
                isFavorite = 0,
                deckCreationDate = now()
            )

            sendDeckOnSave(REQUEST_CODE, ADD, newDeck)
            dismiss()
        }
    }

    private fun onUpdate(deck: ExternalDeck) {
        if (!checkError()) {
            val deckFirstLanguage = binding.deckFirstLanguageET.text
            val deckSecondLanguage = binding.deckSecondLanguageET.text
            val newDeck = Deck(
                deckId = deck.deckId,
                parentDeckId = deck.parentDeckId,
                deckName = binding.deckNameET.text.toString(),
                deckDescription = binding.deckDescriptionET.text.toString(),
                cardContentDefaultLanguage = if (deckFirstLanguage.isBlank()) null else deckFirstLanguage.toString(),
                cardDefinitionDefaultLanguage = if (deckSecondLanguage.isBlank()) null else deckSecondLanguage.toString(),
                deckBackground = deckCategoryColor,
                deckCategory = deck.deckCategory,
                isFavorite = deck.isFavorite,
                deckCreationDate = deck.deckCreationDate
            )

            sendDeckOnEdit(REQUEST_CODE, newDeck)
            dismiss()
        }
    }

    fun isCorrect(index: Int?) = index == 1
    fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

    private fun displayColorPicker(listOfColors: List<ColorModel>) {
        deckColorPickerAdapter = DeckColorPickerAdapter(
            requireContext(),
            listOfColors,
            true
        ) { selectedColor ->
            newDeckDialogViewModel.selectColor(selectedColor.id)
            deckCategoryColor = selectedColor.id
            deckColorPickerAdapter.notifyDataSetChanged()
        }
        binding.rvDeckColorPicker.apply {
            adapter = deckColorPickerAdapter
            layoutManager = GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun now(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        return LocalDateTime.now().format(formatter)
    }

    private fun checkError(): Boolean {
        var error = true
        val deckName = binding.deckNameET.text.toString()
//        val deckFirstLang = binding.deckFirstLanguageET.text.toString()
//        val deckSecondLang = binding.deckSecondLanguageET.text.toString()
        when {
            deckName.isBlank() -> {
                binding.deckNameLY.error = getString(R.string.error_message_on_missing_deck_name)
            }

//            deckFirstLang.isBlank() -> {
//                binding.deckFirstLanguageLY.error = getString(R.string.error_message_deck_missing_first_language)
//            }
//
//            deckSecondLang.isBlank() -> {
//                binding.deckSecondLanguageLY.error = getString(R.string.error_message_deck_missing_second_language)
//            }
//
//            deckFirstLang !in supportedLanguages -> {
//                binding.deckFirstLanguageLY.error = getString(R.string.error_message_deck_language_not_supported)
//            }
//
//            deckSecondLang !in supportedLanguages -> {
//                binding.deckSecondLanguageLY.error = getString(R.string.error_message_deck_language_not_supported)
//            }

            else -> {
                error = false
            }
        }
        return error
    }

    private fun sendDeckOnSave(requestCode: String, action: String, deck: Deck) {
        val result = OnSaveDeckWithCationModel(deck, action)
        parentFragmentManager.setFragmentResult(
            requestCode,
            bundleOf(SAVE_DECK_BUNDLE_KEY to result)
        )
    }

    private fun sendDeckOnEdit(requestCode: String, deck: Deck) {
        parentFragmentManager.setFragmentResult(requestCode, bundleOf(EDIT_DECK_BUNDLE_KEY to deck))
    }

}