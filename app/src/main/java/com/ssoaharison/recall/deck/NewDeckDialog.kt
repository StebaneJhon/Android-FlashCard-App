package com.ssoaharison.recall.deck

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.button.MaterialButton
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.databinding.AddDeckLayoutDialogBinding
import com.ssoaharison.recall.util.DeckAdditionAction.ADD
import com.ssoaharison.recall.util.DeckAdditionAction.ADD_DECK_FORWARD_TO_CARD_ADDITION
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.ssoaharison.recall.util.LanguageUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssoaharison.recall.util.DeckColorPickerAdapter
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NewDeckDialog(
    val deck: ImmutableDeck?,
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

        val arrayAdapterSupportedLanguages =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)
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

        if (deck != null) {
            binding.btAddCard.isVisible = false
            binding.tvTitle.text = getString(R.string.tv_update_deck)
            binding.deckNameET.setText(deck.deckName)
            binding.deckDescriptionET.setText(deck.deckDescription)
            binding.deckFirstLanguageET.setText(deck.cardContentDefaultLanguage)
            binding.deckSecondLanguageET.setText(deck.cardDefinitionDefaultLanguage)
            deck.deckColorCode?.let { newDeckDialogViewModel.selectColor(it) }
            deckCategoryColor = deck.deckColorCode

            builder.setView(binding.root)


            binding.btExit.setOnClickListener { dismiss() }
            binding.dialogPositiveBT.apply {
                text = getString(R.string.bt_text_update)
                setOnClickListener {
                    onUpdate(deck)
                }
                binding.btAddTop.setOnClickListener {
                    onUpdate(deck)
                }
            }
        } else {
            builder.setView(binding.root)
            binding.btAddCard.isVisible = true
            binding.tvTitle.text = getString(R.string.tv_add_new_deck)
            binding.btExit.setOnClickListener { dismiss() }
            binding.dialogPositiveBT.apply {
                text = getString(R.string.bt_text_add)
                setOnClickListener {
                    onAdd()
                }
            }
            binding.btAddTop.setOnClickListener {
                onAdd()
            }
            binding.btAddCard.setOnClickListener {
                if (!checkError()) {
                    val newDeck = Deck(
                        now(),
                        binding.deckNameET.text.toString(),
                        binding.deckDescriptionET.text.toString(),
                        binding.deckFirstLanguageET.text.toString(),
                        binding.deckSecondLanguageET.text.toString(),
                        deckCategoryColor,
                        null,
                        0
                    )
                    sendDeckOnSave(REQUEST_CODE, ADD_DECK_FORWARD_TO_CARD_ADDITION, newDeck)
                    dismiss()
                }
            }
        }

        lifecycleScope.launch {
            delay(50)
            val deckColorCategorySelector = DeckColorCategorySelector()
            val deckColors = if (appTheme == DARK_THEME) {
                deckColorCategorySelector.getDarkColors()
            } else {
                deckColorCategorySelector.getColors()
            }
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

    private fun onAdd() {
        if (!checkError()) {
            val deckFirstLanguage = binding.deckFirstLanguageET.text
            val deckSecondLanguage = binding.deckSecondLanguageET.text
            val newDeck = Deck(
                now(),
                binding.deckNameET.text.toString(),
                binding.deckDescriptionET.text.toString(),
                if (deckFirstLanguage.isBlank()) null else deckFirstLanguage.toString(),
                if (deckSecondLanguage.isBlank()) null else deckSecondLanguage.toString(),
                deckCategoryColor,
                null,
                0
            )

            sendDeckOnSave(REQUEST_CODE, ADD, newDeck)
            dismiss()
        }
    }

    private fun onUpdate(deck: ImmutableDeck) {
        if (!checkError()) {
            val deckFirstLanguage = binding.deckFirstLanguageET.text
            val deckSecondLanguage = binding.deckSecondLanguageET.text
            val newDeck = Deck(
                deck.deckId,
                binding.deckNameET.text.toString(),
                binding.deckDescriptionET.text.toString(),
                if (deckFirstLanguage.isBlank()) null else deckFirstLanguage.toString(),
                if (deckSecondLanguage.isBlank()) null else deckSecondLanguage.toString(),
                deckCategoryColor,
                deck.deckCategory,
                isCorrectRevers(deck.isFavorite)
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
            listOfColors
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