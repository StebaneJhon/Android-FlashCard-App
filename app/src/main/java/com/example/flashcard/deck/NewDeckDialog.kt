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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NewDeckDialog(val deck: ImmutableDeck?) : AppCompatDialogFragment() {

    private var deckNameET: EditText? = null
    private var deckDescriptionET: EditText? = null
    private var deckFirstLangET: AutoCompleteTextView? = null
    private var deckSecondLangET: AutoCompleteTextView? = null
    private var deckNameLY: TextInputLayout? = null
    private var deckFirstLanguageLY: TextInputLayout? = null
    private var deckSecondLanguageLY: TextInputLayout? = null
    private var buttonDialogueN: MaterialButton? = null
    private var buttonDialogueP: MaterialButton? = null
    private var btAddCard: MaterialButton? = null
    private var tvTitle: TextView? = null
    private var rvDeckColorPicker: RecyclerView? = null
    private var btAddTop: MaterialButton? = null

    private var deckCategoryColor: String? = null
    private val supportedLanguages = FirebaseTranslatorHelper().getSupportedLang()
    private lateinit var deckColorPickerAdapter: DeckColorPickerAdapter

    private val newDeckDialogViewModel: NewDeckDialogViewModel by viewModels()

    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

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
        buttonDialogueN = view?.findViewById(R.id.bt_exit)
        buttonDialogueP = view?.findViewById(R.id.dialogPositiveBT)
        btAddCard = view?.findViewById(R.id.bt_add_card)
        rvDeckColorPicker = view?.findViewById(R.id.rv_deck_color_picker)
        btAddTop = view?.findViewById(R.id.bt_add_top)

        gridLayoutManager = GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        tvTitle = view?.findViewById(R.id.tv_title)

        val arrayAdapterSupportedLanguages = ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)
        deckFirstLangET?.apply {
            setAdapter(arrayAdapterSupportedLanguages)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.filter_spinner_dropdown_background,
                    requireActivity().theme
                )
            )
        }
        deckSecondLangET?.apply {
            setAdapter(arrayAdapterSupportedLanguages)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.filter_spinner_dropdown_background,
                    requireActivity().theme
                )
            )
        }

        if (deck != null) {
            btAddCard?.isVisible =  false
            tvTitle?.text = getString(R.string.tv_update_deck)
            deckNameET?.setText(deck.deckName)
            deckDescriptionET?.setText(deck.deckDescription)
            deckFirstLangET?.setText(deck.deckFirstLanguage)
            deckSecondLangET?.setText(deck.deckSecondLanguage)
            deck.deckColorCode?.let { newDeckDialogViewModel.selectColor(it) }
            deckCategoryColor = deck.deckColorCode

            builder.setView(view)


            buttonDialogueN?.setOnClickListener { dismiss() }
            buttonDialogueP?.apply {
                text = "Update"
                setOnClickListener {
                    onUpdate(deck)
                }
                btAddTop?.setOnClickListener {
                    onUpdate(deck)
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
                    onAdd()
                }
            }
            btAddTop?.setOnClickListener {
                onAdd()
            }
            btAddCard?.setOnClickListener {
                if (!checkError()) {
                    val newDeck = Deck(
                        now(),
                        deckNameET?.text.toString(),
                        deckDescriptionET?.text.toString(),
                        deckFirstLangET?.text.toString(),
                        deckSecondLangET?.text.toString(),
                        deckCategoryColor,
                        0,
                        null,
                        0
                    )
                    sendDeckOnSave(REQUEST_CODE, ADD_DECK_FORWARD_TO_CARD_ADDITION, newDeck)
                    dismiss()
                }
            }
        }



        // Show Color Picker
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newDeckDialogViewModel.initColorSelection(DeckColorCategorySelector().getColors(), deckCategoryColor)
                newDeckDialogViewModel.colorSelectionList.collect { listOfColors ->
                    displayColorPicker(listOfColors)
                }
            }
        }

        view?.findViewById<TextView>(R.id.colorPickerTitle)?.setOnClickListener { v ->
            if (rvDeckColorPicker?.layoutManager == gridLayoutManager) {
                rvDeckColorPicker?.layoutManager = linearLayoutManager
                (v as TextView).setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.icon_expand_more,
                    0
                )
            } else {
                rvDeckColorPicker?.layoutManager = gridLayoutManager
                (v as TextView).setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.icon_expand_less,
                    0
                )
            }
        }

        builder.setView(view)

        return builder.create()
    }

    private fun onAdd() {
        if (!checkError()) {
            val newDeck = Deck(
                now(),
                deckNameET?.text.toString(),
                deckDescriptionET?.text.toString(),
                deckFirstLangET?.text.toString(),
                deckSecondLangET?.text.toString(),
                deckCategoryColor,
                0,
                null,
                0
            )

            sendDeckOnSave(REQUEST_CODE, ADD, newDeck)
            dismiss()
        }
    }

    private fun onUpdate(deck: ImmutableDeck) {
        if (!checkError()) {
            val newDeck = Deck(
                deck.deckId,
                deckNameET?.text.toString(),
                deckDescriptionET?.text.toString(),
                deckFirstLangET?.text.toString(),
                deckSecondLangET?.text.toString(),
                deckCategoryColor,
                deck.cardSum,
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
            layoutManager = linearLayoutManager
        }
    }

    private fun now(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        return LocalDateTime.now().format(formatter)
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