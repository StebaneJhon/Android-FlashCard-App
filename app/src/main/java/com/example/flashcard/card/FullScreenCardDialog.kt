package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FullScreenCardDialog(private val card: Card, private val deck: ImmutableDeck) :
    AppCompatDialogFragment() {

    private var languageHint: TextView? = null
    private var onCardText: TextView? = null
    private var onCardTExtDescription: TextView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.full_screan_card_layout, null)

        languageHint = view?.findViewById(R.id.languageHint)
        onCardTExtDescription = view?.findViewById(R.id.onCardTextDescriptionTV)
        onCardText = view?.findViewById(R.id.onCardTextTV)

        languageHint?.text = deck.deckFirstLanguage
        onCardText?.text = card.cardContent
        onCardTExtDescription?.text = card.contentDescription

        builder.setView(view)

        return builder.create()
    }
}