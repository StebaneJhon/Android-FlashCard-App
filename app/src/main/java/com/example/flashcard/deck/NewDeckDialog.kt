package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.card.AddedCardRecyclerViewAdapter
import com.example.flashcard.card.NewCardDialog
import com.example.flashcard.util.DeckAdditionAction.ADD
import com.example.flashcard.util.DeckAdditionAction.ADD_DECK_FORWARD_TO_CARD_ADDITION
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.FirebaseTranslatorHelper
import com.example.flashcard.util.deckCategoryColorConst.BLACK
import com.example.flashcard.util.deckCategoryColorConst.BLUE
import com.example.flashcard.util.deckCategoryColorConst.BROWN
import com.example.flashcard.util.deckCategoryColorConst.GREEN
import com.example.flashcard.util.deckCategoryColorConst.PINK
import com.example.flashcard.util.deckCategoryColorConst.PURPLE
import com.example.flashcard.util.deckCategoryColorConst.RED
import com.example.flashcard.util.deckCategoryColorConst.TEAL
import com.example.flashcard.util.deckCategoryColorConst.WHITE
import com.example.flashcard.util.deckCategoryColorConst.YELLOW
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class NewDeckDialog(val deck: ImmutableDeck?) : AppCompatDialogFragment() {

    private var deckNameET: EditText? = null
    private var deckDescriptionET: EditText? = null
    private var deckFirstLangET: MaterialAutoCompleteTextView? = null
    private var deckSecondLangET: MaterialAutoCompleteTextView? = null
    private var deckNameLY: TextInputLayout? = null
    private var deckFirstLanguageLY: TextInputLayout? = null
    private var deckSecondLanguageLY: TextInputLayout? = null
    private var buttonDialogueN: MaterialButton? = null
    private var buttonDialogueP: MaterialButton? = null
    private var btAddCard: MaterialButton? = null
    private var tvTitle: TextView? = null
    private var rvDeckColorPicker: RecyclerView? = null

    private var deckCategoryColor: String? = null
    private val supportedLanguages = FirebaseTranslatorHelper().getSupportedLang()
    private lateinit var deckColorPickerAdapter: DeckColorPickerAdapter

    private val newDeckDialogViewModel: NewDeckDialogViewModel by viewModels()

    companion object {
        const val TAG = "NewDeckDialog"
        const val SAVE_DECK_BUNDLE_KEY = "1"
        const val EDIT_DECK_BUNDLE_KEY = "2"
        const val REQUEST_CODE = "0"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_deck_layout_dialog, null)

        deckNameET = view?.findViewById(R.id.deckNameET)
        deckSecondLangET = view?.findViewById(R.id.deckSecondLanguageET)
        deckFirstLangET = view?.findViewById(R.id.deckFirstLanguageET)
        deckDescriptionET = view?.findViewById(R.id.deckDescriptionET)

        deckNameLY = view?.findViewById(R.id.deckNameLY)
        deckFirstLanguageLY = view?.findViewById(R.id.deckFirstLanguageLY)
        deckSecondLanguageLY = view?.findViewById(R.id.deckSecondLanguageLY)
        buttonDialogueN = view?.findViewById(R.id.dialogueNegativeBT)
        buttonDialogueP = view?.findViewById(R.id.dialogPositiveBT)
        btAddCard = view?.findViewById(R.id.bt_add_card)
        rvDeckColorPicker = view?.findViewById(R.id.rv_deck_color_picker)

        tvTitle = view?.findViewById(R.id.tv_title)

        deckFirstLangET?.setSimpleItems(supportedLanguages)
        deckSecondLangET?.setSimpleItems(supportedLanguages)

        // Show Color Picker
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newDeckDialogViewModel.initColorSelection(DeckColorCategorySelector().getColors())
                newDeckDialogViewModel.colorSelectionList.collect { listOfColors ->
                    displayColorPicker(listOfColors)
                }
            }
        }

        if (deck != null) {
            btAddCard?.isVisible =  false
            tvTitle?.text = getString(R.string.tv_update_deck)
            deckNameET?.setText(deck.deckName)
            deckDescriptionET?.setText(deck.deckDescription)
            deckFirstLangET?.setText(deck.deckFirstLanguage)
            deckSecondLangET?.setText(deck.deckSecondLanguage)
            deck.deckColorCode?.let { newDeckDialogViewModel.selectColor(it) }

            builder.setView(view)


            buttonDialogueN?.setOnClickListener { dismiss() }
            buttonDialogueP?.apply {
                text = "Update"
                setOnClickListener {
                    if (!checkError()) {
                        val newDeck = Deck(
                            deck.deckId,
                            deckNameET?.text.toString(),
                            deckDescriptionET?.text.toString(),
                            deckFirstLangET?.text.toString(),
                            deckSecondLangET?.text.toString(),
                            deckCategoryColor,
                            deck.cardSum,
                            deck.category,
                            deck.isFavorite
                        )

                        sendDeckOnEdit(REQUEST_CODE, newDeck)
                        dismiss()
                    }
                }
            }
        } else {
            builder.setView(view)
            btAddCard?.isVisible = true
            tvTitle?.text = getString(R.string.tv_add_new_deck)
            buttonDialogueN?.setOnClickListener { dismiss() }
            buttonDialogueP?.apply {
                text = "Add"
                setOnClickListener {
                    if (!checkError()) {
                        val newDeck = Deck(
                            null,
                            deckNameET?.text.toString(),
                            deckDescriptionET?.text.toString(),
                            deckFirstLangET?.text.toString(),
                            deckSecondLangET?.text.toString(),
                            deckCategoryColor,
                            0,
                            null,
                            false
                        )

                        sendDeckOnSave(REQUEST_CODE, ADD, newDeck)
                        dismiss()
                    }
                }
            }
            btAddCard?.setOnClickListener {
                if (!checkError()) {
                    val newDeck = Deck(
                        null,
                        deckNameET?.text.toString(),
                        deckDescriptionET?.text.toString(),
                        deckFirstLangET?.text.toString(),
                        deckSecondLangET?.text.toString(),
                        deckCategoryColor,
                        0,
                        null,
                        false
                    )
                    sendDeckOnSave(REQUEST_CODE, ADD_DECK_FORWARD_TO_CARD_ADDITION, newDeck)
                    dismiss()
                }
            }
        }

        builder.setView(view)

        return builder.create()
    }

    private fun displayColorPicker(listOfColors: List<ColorModel>) {
        deckColorPickerAdapter = context?.let { ctx ->
            DeckColorPickerAdapter(
                ctx,
                listOfColors
            ) { selectedColor ->
                newDeckDialogViewModel.selectColor(selectedColor.id)
                deckCategoryColor = selectedColor.id
                deckColorPickerAdapter.notifyDataSetChanged()
            }
        }!!
        rvDeckColorPicker?.apply {
            adapter = deckColorPickerAdapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun checkError(): Boolean {
        var error = true
        val deckName = deckNameET?.text.toString()
        val deckFirstLang = deckFirstLangET?.text.toString()
        val deckSecondLang = deckSecondLangET?.text.toString()
        when {
            deckName.isBlank() -> {
                deckNameLY?.error = "Deck name required"
            }

            deckFirstLang.isBlank() -> {
                deckFirstLanguageLY?.error = "Deck first language required"
            }

            deckSecondLang.isBlank() -> {
                deckSecondLanguageLY?.error = "Deck second language required"
            }

            deckFirstLang !in supportedLanguages -> {
                deckFirstLanguageLY?.error = "Language not supported"
            }

            deckSecondLang !in supportedLanguages -> {
                deckSecondLanguageLY?.error = "Language not supported"
            }

            else -> {
                error = false
            }
        }
        return error
    }

    private fun sendDeckOnSave(requestCode: String, action: String, deck: Deck) {
        val result = OnSaveDeckWithCationModel(deck, action)
        parentFragmentManager.setFragmentResult(requestCode, bundleOf(SAVE_DECK_BUNDLE_KEY to result))
    }

    private fun sendDeckOnEdit(requestCode: String, deck: Deck) {
        parentFragmentManager.setFragmentResult(requestCode, bundleOf(EDIT_DECK_BUNDLE_KEY to deck))
    }

}