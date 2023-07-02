package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.flashcard.R
import com.example.flashcard.backend.entities.Card
import kotlin.ClassCastException

class NewCardDialog: AppCompatDialogFragment() {

    private var cardContent: EditText? = null
    private var cardContentDefinition: EditText? = null
    private var cardValue: EditText? = null
    private var cardValueDefinition: EditText? = null

    private var listener: NewDialogListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_card_layout_dialog, null)

        cardContent = view?.findViewById(R.id.cardContentTV)
        cardContentDefinition = view?.findViewById(R.id.cardContentDefinitionTV)
        cardValue = view?.findViewById(R.id.cardValueTV)
        cardValueDefinition = view?.findViewById(R.id.cardValueDefinitionTV)

        builder.setView(view)
            .setTitle("New Card")
            .setNegativeButton("Cancel") { _, _ ->  }
            .setPositiveButton("Add"
            ) { _, _ ->
                val newCard = Card(
                    null,
                    cardContent?.text.toString(),
                    cardContentDefinition?.text.toString(),
                    cardValue?.text.toString(),
                    cardValueDefinition?.text.toString(),
                    null
                )

                listener?.getCard(newCard)
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
        fun getCard(card: Card)
    }
}