package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Deck

class NewDeckDialog(val deck: ImmutableDeck?): AppCompatDialogFragment() {

    private var deckNameET: EditText? = null
    private var deckDescriptionET: EditText? = null
    private var deckFirstLangET: EditText? = null
    private var deckSecondLangET: EditText? = null
    private var deckColorET: EditText? = null

    private var listener: NewDialogListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_deck_layout_dialog, null)

        deckNameET = view?.findViewById(R.id.deckNameET)
        deckColorET = view?.findViewById(R.id.deckDeckColorET)
        deckSecondLangET = view?.findViewById(R.id.deckSecondLanguageET)
        deckFirstLangET = view?.findViewById(R.id.deckFirstLanguageET)
        deckDescriptionET = view?.findViewById(R.id.deckDescriptionET)

        val aa = deck
        if (deck != null) {
            deckColorET?.setText(deck.deckColorCode)
            deckNameET?.setText(deck.deckName)
            deckDescriptionET?.setText(deck.deckDescription)
            deckFirstLangET?.setText(deck.deckFirstLanguage)
            deckSecondLangET?.setText(deck.deckSecondLanguage)

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
                                deckColorET?.text.toString(),
                                deck.cardSum
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
                                deckColorET?.text.toString(),
                                0
                            )

                        listener?.getDeck(newDeck, "Add")
                    }

                })
        }

        return builder.create()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        try {
            listener = context as NewDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement NewDialogListener")
        }


    }

    interface NewDialogListener {
        fun getDeck(deck: Deck, action: String)
    }
}