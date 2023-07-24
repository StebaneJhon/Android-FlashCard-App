package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.flashcard.R
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.Constant
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.ClassCastException

class NewCardDialog(private val card: Card?): AppCompatDialogFragment() {

    private var cardContent: EditText? = null
    private var cardContentDefinition: EditText? = null
    private var cardValue: EditText? = null
    private var cardValueDefinition: EditText? = null

    private var listener: NewDialogListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_card_layout_dialog, null)

        cardContent = view?.findViewById(R.id.cardContentTV)
        cardContentDefinition = view?.findViewById(R.id.cardContentDefinitionTV)
        cardValue = view?.findViewById(R.id.cardValueTV)
        cardValueDefinition = view?.findViewById(R.id.cardValueDefinitionTV)

        if (card != null) {

            cardContent?.setText(card.cardContent)
            cardContentDefinition?.setText(card.contentDescription)
            cardValue?.setText(card.cardDefinition)
            cardValueDefinition?.setText(card.valueDefinition)

            builder.setView(view)
                .setTitle("New Card")
                .setNegativeButton("Cancel") { _, _ ->  }
                .setPositiveButton("Update"
                ) { _, _ ->
                    onPositiveAction(Constant.UPDATE)
                }
        } else {
            builder.setView(view)
                .setTitle("New Card")
                .setNegativeButton("Cancel") { _, _ ->  }
                .setPositiveButton("Add"
                ) { _, _ ->
                    onPositiveAction(Constant.ADD)
                }
        }



        return builder.create()
    }

    private fun onPositiveAction(action: String) {
        val newCard = if (action == Constant.ADD) {
             Card(
                null,
                cardContent?.text.toString(),
                cardContentDefinition?.text.toString(),
                cardValue?.text.toString(),
                cardValueDefinition?.text.toString(),
                null,
                 "",
                 false,
                 0,
                 0
            )
        } else {
            Card(
                card?.cardId,
                cardContent?.text.toString(),
                cardContentDefinition?.text.toString(),
                cardValue?.text.toString(),
                cardValueDefinition?.text.toString(),
                card?.deckId,
                card?.backgroundImg,
                card?.isFavorite,
                card?.revisionTime,
                card?.missedTime
            )
        }


        listener?.getCard(newCard, action)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NewDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement NewDialogListener")
        }
    }

    interface NewDialogListener {
        fun getCard(card: Card, action: String)
    }
}