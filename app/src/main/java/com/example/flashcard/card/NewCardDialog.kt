package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.flashcard.R
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.Constant
import com.example.flashcard.util.cardBackgroundConst.CURVE_PATTERN
import com.example.flashcard.util.cardBackgroundConst.DATES_PATTERN
import com.example.flashcard.util.cardBackgroundConst.FLORAL_PATTERN
import com.example.flashcard.util.cardBackgroundConst.MAP_PATTERN
import com.example.flashcard.util.cardBackgroundConst.SQUARE_PATTERN
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.ClassCastException

class NewCardDialog(private val card: Card?): AppCompatDialogFragment() {

    private var cardContent: EditText? = null
    private var cardContentDefinition: EditText? = null
    private var cardValue: EditText? = null
    private var cardValueDefinition: EditText? = null
    private var curvePatternBT: LinearLayout? = null
    private var mapPatternBT: LinearLayout? = null
    private var floralPatternBT: LinearLayout? = null
    private var datesPatternBT: LinearLayout? = null

    private var listener: NewDialogListener? = null

    private var cardBackground: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_card_layout_dialog, null)

        cardContent = view?.findViewById(R.id.cardContentTV)
        cardContentDefinition = view?.findViewById(R.id.cardContentDefinitionTV)
        cardValue = view?.findViewById(R.id.cardValueTV)
        cardValueDefinition = view?.findViewById(R.id.cardValueDefinitionTV)

        curvePatternBT = view?.findViewById(R.id.curvePatternBT)
        mapPatternBT = view?.findViewById(R.id.mapPatternBT)
        floralPatternBT = view?.findViewById(R.id.floralPatternBT)
        datesPatternBT = view?.findViewById(R.id.datesPatternBT)

        if (card != null) {

            cardContent?.setText(card.cardContent)
            cardContentDefinition?.setText(card.contentDescription)
            cardValue?.setText(card.cardDefinition)
            cardValueDefinition?.setText(card.valueDefinition)
            card.backgroundImg?.let { onCardBackgroundSelected(it) }

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

        mapPatternBT?.setOnClickListener {
            onCardBackgroundSelected(MAP_PATTERN)
        }
        curvePatternBT?.setOnClickListener {
            onCardBackgroundSelected(CURVE_PATTERN)
        }
        floralPatternBT?.setOnClickListener {
            onCardBackgroundSelected(FLORAL_PATTERN)
        }
        datesPatternBT?.setOnClickListener {
            onCardBackgroundSelected(DATES_PATTERN)
        }



        return builder.create()
    }

    private fun onCardBackgroundSelected(background: String) {
        val onActiveBTdForeground = ContextCompat.getDrawable(requireContext(), R.drawable.card_foreground_active)
        val onInactiveBTdForeground = ContextCompat.getDrawable(requireContext(), R.drawable.card_foreground_inactive)
        when (background) {
            MAP_PATTERN -> {
                mapPatternBT?.foreground = onActiveBTdForeground
                curvePatternBT?.foreground = onInactiveBTdForeground
                floralPatternBT?.foreground = onInactiveBTdForeground
                datesPatternBT?.foreground = onInactiveBTdForeground
            }
            CURVE_PATTERN -> {
                mapPatternBT?.foreground = onInactiveBTdForeground
                curvePatternBT?.foreground = onActiveBTdForeground
                floralPatternBT?.foreground = onInactiveBTdForeground
                datesPatternBT?.foreground = onInactiveBTdForeground
            }
            FLORAL_PATTERN -> {
                mapPatternBT?.foreground = onInactiveBTdForeground
                curvePatternBT?.foreground = onInactiveBTdForeground
                floralPatternBT?.foreground = onActiveBTdForeground
                datesPatternBT?.foreground = onInactiveBTdForeground
            }
            DATES_PATTERN -> {
                mapPatternBT?.foreground = onInactiveBTdForeground
                curvePatternBT?.foreground = onInactiveBTdForeground
                floralPatternBT?.foreground = onInactiveBTdForeground
                datesPatternBT?.foreground = onActiveBTdForeground
            }
        }
        cardBackground = background
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
                 cardBackground,
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
                cardBackground,
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