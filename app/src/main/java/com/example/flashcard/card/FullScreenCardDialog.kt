package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FullScreenCardDialog(private val card: Card, private val deck: ImmutableDeck) :
    AppCompatDialogFragment() {

    private var languageHint: TextView? = null
    private var onCardText: TextView? = null
    private var onCardTextDescription: TextView? = null
    private var dismissButton: ImageButton? = null
    private var cardRoot: ConstraintLayout? = null

    private var isCardRevealed = false

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.full_screan_card_layout, null)

        languageHint = view?.findViewById(R.id.languageHint)
        onCardTextDescription = view?.findViewById(R.id.onCardTextDescriptionTV)
        onCardText = view?.findViewById(R.id.onCardTextTV)
        dismissButton = view?.findViewById(R.id.exitFullscreenCardButton)
        cardRoot = view?.findViewById(R.id.fullScreenCardRoot)

        languageHint?.text = deck.deckFirstLanguage
        onCardText?.text = card.cardContent
        onCardTextDescription?.text = card.contentDescription
        dismissButton?.setOnClickListener { dismiss() }
        cardRoot?.setOnClickListener { flipCard(card, deck) }

        builder.setView(view)


        return builder.create()
    }

    private fun flipCard(card: Card, deck: ImmutableDeck) {
        if (!isCardRevealed) {
            languageHint?.text = deck.deckSecondLanguage
            onCardText?.text = card.cardDefinition
            onCardTextDescription?.text = card.valueDefinition
            isCardRevealed = true
        } else {
            languageHint?.text = deck.deckFirstLanguage
            onCardText?.text = card.cardContent
            onCardTextDescription?.text = card.contentDescription
            isCardRevealed = false
        }
    }
}