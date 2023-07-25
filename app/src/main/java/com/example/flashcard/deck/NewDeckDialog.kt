package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.card.NewCardDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NewDeckDialog(val deck: ImmutableDeck?): AppCompatDialogFragment() {

    private var deckNameET: EditText? = null
    private var deckDescriptionET: EditText? = null
    private var deckFirstLangET: EditText? = null
    private var deckSecondLangET: EditText? = null
    private var categoryColorBlackBT: LinearLayout? = null
    private var categoryColorRedBT: LinearLayout? = null
    private var categoryColorPurpleBT: LinearLayout? = null
    private var redCheck: View? = null
    private var blackCheck: View? = null
    private var purpleCheck: View? = null

    private var listener: NewDialogListener? = null

    private var deckCategoryColor: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_deck_layout_dialog, null)

        redCheck = view?.findViewById(R.id.redChecked)
        blackCheck = view?.findViewById(R.id.blackChecked)
        purpleCheck = view?.findViewById(R.id.purpleChecked)

        deckNameET = view?.findViewById(R.id.deckNameET)
        deckSecondLangET = view?.findViewById(R.id.deckSecondLanguageET)
        deckFirstLangET = view?.findViewById(R.id.deckFirstLanguageET)
        deckDescriptionET = view?.findViewById(R.id.deckDescriptionET)

        categoryColorBlackBT = view?.findViewById(R.id.blackCategoryButton)
        categoryColorRedBT = view?.findViewById(R.id.redCategoryButton)
        categoryColorPurpleBT = view?.findViewById(R.id.purpleCategoryButton)

        categoryColorBlackBT?.setOnClickListener {
            onColorCategorySelected("black")
        }
        categoryColorRedBT?.setOnClickListener {
            onColorCategorySelected("red")
        }
        categoryColorPurpleBT?.setOnClickListener {
            onColorCategorySelected("purple")
        }

        if (deck != null) {
            deckNameET?.setText(deck.deckName)
            deckDescriptionET?.setText(deck.deckDescription)
            deckFirstLangET?.setText(deck.deckFirstLanguage)
            deckSecondLangET?.setText(deck.deckSecondLanguage)
            deck.deckColorCode?.let { onColorCategorySelected(it) }

            builder.setView(view)
                .setTitle("New Deck")
                .setNegativeButton("Cancel", object: DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                    }
                })
                .setPositiveButton("Update", object: DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
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
                    }

                })
        } else {
            builder.setView(view)
                .setTitle("New Deck")
                .setNegativeButton("Cancel", object: DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                    }
                })
                .setPositiveButton("Add", object: DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
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
                    }

                })
        }

        builder.setView(view)

        return builder.create()
    }

    private fun onColorCategorySelected(
        color: String
    ) {
        when (color) {
            "red" -> {
                redCheck?.visibility = View.VISIBLE
                purpleCheck?.visibility = View.GONE
                blackCheck?.visibility = View.GONE
            }
            "purple" -> {
                redCheck?.visibility = View.GONE
                purpleCheck?.visibility = View.VISIBLE
                blackCheck?.visibility = View.GONE
            }
            "black" -> {
                redCheck?.visibility = View.GONE
                purpleCheck?.visibility = View.GONE
                blackCheck?.visibility = View.VISIBLE
            }
            else -> {
                redCheck?.visibility = View.GONE
                purpleCheck?.visibility = View.GONE
                blackCheck?.visibility = View.GONE
            }
        }

        deckCategoryColor = color
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