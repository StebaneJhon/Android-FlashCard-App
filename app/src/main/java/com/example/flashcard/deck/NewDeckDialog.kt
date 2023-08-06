package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.card.NewCardDialog
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

class NewDeckDialog(val deck: ImmutableDeck?): AppCompatDialogFragment() {

    private var deckNameET: EditText? = null
    private var deckDescriptionET: EditText? = null
    private var deckFirstLangET: MaterialAutoCompleteTextView? = null
    private var deckSecondLangET: MaterialAutoCompleteTextView? = null
    private var categoryWhiteBT: LinearLayout? = null
    private var categoryColorRedBT: LinearLayout? = null
    private var categoryColorPinkBT: LinearLayout? = null
    private var categoryColorPurpleBT: LinearLayout? = null
    private var categoryColorBlueBT: LinearLayout? = null
    private var categoryColorTealBT: LinearLayout? = null
    private var categoryColorGreenBT: LinearLayout? = null
    private var categoryColorYellowBT: LinearLayout? = null
    private var categoryColorBrownBT: LinearLayout? = null
    private var categoryColorBlackBT: LinearLayout? = null
    private var whiteCheck: View? = null
    private var redCheck: View? = null
    private var pinkCheck: View? = null
    private var purpleCheck: View? = null
    private var blueCheck: View? = null
    private var tealCheck: View? = null
    private var greenCheck: View? = null
    private var yellowCheck: View? = null
    private var brownCheck: View? = null
    private var blackCheck: View? = null
    private var deckNameLY: TextInputLayout? = null
    private var deckFirstLanguageLY: TextInputLayout? = null
    private var deckSecondLanguageLY: TextInputLayout? = null
    private var buttonDialogueN: MaterialButton? = null
    private var buttonDialogueP: MaterialButton? = null

    private var listener: NewDialogListener? = null

    private var deckCategoryColor: String? = null
    private val supportedLanguages = FirebaseTranslatorHelper().getSupportedLang()

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_deck_layout_dialog, null)

        deckNameET = view?.findViewById(R.id.deckNameET)
        deckSecondLangET = view?.findViewById(R.id.deckSecondLanguageET)
        deckFirstLangET = view?.findViewById(R.id.deckFirstLanguageET)
        deckDescriptionET = view?.findViewById(R.id.deckDescriptionET)

        categoryWhiteBT = view?.findViewById(R.id.whiteCategoryButton)
        categoryColorRedBT = view?.findViewById(R.id.redCategoryButton)
        categoryColorPinkBT = view?.findViewById(R.id.pinkCategoryButton)
        categoryColorPurpleBT = view?.findViewById(R.id.purpleCategoryButton)
        categoryColorBlueBT = view?.findViewById(R.id.blueCategoryButton)
        categoryColorTealBT = view?.findViewById(R.id.tealCategoryButton)
        categoryColorGreenBT = view?.findViewById(R.id.greenCategoryButton)
        categoryColorYellowBT = view?.findViewById(R.id.yellowCategoryButton)
        categoryColorBrownBT = view?.findViewById(R.id.brownCategoryButton)
        categoryColorBlackBT = view?.findViewById(R.id.blackCategoryButton)

        whiteCheck = view?.findViewById(R.id.whiteChecked)
        redCheck = view?.findViewById(R.id.redChecked)
        pinkCheck = view?.findViewById(R.id.pinkChecked)
        purpleCheck = view?.findViewById(R.id.purpleChecked)
        blueCheck = view?.findViewById(R.id.blueChecked)
        tealCheck = view?.findViewById(R.id.tealChecked)
        greenCheck = view?.findViewById(R.id.greenChecked)
        yellowCheck = view?.findViewById(R.id.yellowChecked)
        brownCheck = view?.findViewById(R.id.brownChecked)
        blackCheck = view?.findViewById(R.id.blackChecked)

        deckNameLY = view?.findViewById(R.id.deckNameLY)
        deckFirstLanguageLY = view?.findViewById(R.id.deckFirstLanguageLY)
        deckSecondLanguageLY = view?.findViewById(R.id.deckSecondLanguageLY)
        buttonDialogueN = view?.findViewById(R.id.dialogueNegativeBT)
        buttonDialogueP = view?.findViewById(R.id.dialogPositiveBT)

        deckFirstLangET?.setSimpleItems(supportedLanguages)
        deckSecondLangET?.setSimpleItems(supportedLanguages)

        categoryWhiteBT?.setOnClickListener { onColorCategorySelected(WHITE) }
        categoryColorRedBT?.setOnClickListener { onColorCategorySelected(RED) }
        categoryColorPinkBT?.setOnClickListener { onColorCategorySelected(PINK) }
        categoryColorPurpleBT?.setOnClickListener { onColorCategorySelected(PURPLE) }
        categoryColorBlueBT?.setOnClickListener { onColorCategorySelected(BLUE) }
        categoryColorTealBT?.setOnClickListener { onColorCategorySelected(TEAL) }
        categoryColorGreenBT?.setOnClickListener { onColorCategorySelected(GREEN) }
        categoryColorYellowBT?.setOnClickListener { onColorCategorySelected(YELLOW) }
        categoryColorBrownBT?.setOnClickListener { onColorCategorySelected(BROWN) }
        categoryColorBlackBT?.setOnClickListener { onColorCategorySelected(BLACK) }

        if (deck != null) {
            deckNameET?.setText(deck.deckName)
            deckDescriptionET?.setText(deck.deckDescription)
            deckFirstLangET?.setText(deck.deckFirstLanguage)
            deckSecondLangET?.setText(deck.deckSecondLanguage)
            deck.deckColorCode?.let { onColorCategorySelected(it) }

            builder.setView(view)
                .setTitle("New Deck")

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

                        listener?.getDeck(newDeck, "Update")
                        dismiss()
                    }
                }
            }
        } else {
            builder.setView(view)
                .setTitle("New Deck")

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

                        listener?.getDeck(newDeck, "Add")
                        dismiss()
                    }
                }
            }
        }

        builder.setView(view)

        return builder.create()
    }

    private fun onColorCategorySelected(
        color: String
    ) {
        val checkButtonList = arrayListOf(
            whiteCheck, redCheck, pinkCheck, purpleCheck, blueCheck,
            tealCheck, greenCheck, yellowCheck, brownCheck, blackCheck
        )

        resetCheckBT(checkButtonList)
        when (color) {
            WHITE -> {
                whiteCheck?.visibility = View.VISIBLE
            }
            RED -> {
                redCheck?.visibility = View.VISIBLE
            }
            PURPLE -> {
                purpleCheck?.visibility = View.VISIBLE
            }
            BLUE -> {
                blueCheck?.visibility = View.VISIBLE
            }
            TEAL -> {
                tealCheck?.visibility = View.VISIBLE
            }
            GREEN -> {
                greenCheck?.visibility = View.VISIBLE
            }
            YELLOW -> {
                yellowCheck?.visibility = View.VISIBLE
            }
            BROWN -> {
                brownCheck?.visibility = View.VISIBLE
            }
            BLACK -> {
                blackCheck?.visibility = View.VISIBLE
            }
            else -> {
                resetCheckBT(checkButtonList)
            }
        }

        deckCategoryColor = color
    }

    private fun resetCheckBT(checkButtonList: ArrayList<View?>) {
        checkButtonList.forEach { checkMK -> checkMK?.visibility = View.GONE }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NewDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    interface NewDialogListener {
        fun getDeck(deck: Deck, action: String)
    }
}