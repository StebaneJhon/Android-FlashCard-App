package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.util.Constant
import com.example.flashcard.util.FirebaseTranslatorHelper
import com.example.flashcard.util.cardBackgroundConst.CURVE_PATTERN
import com.example.flashcard.util.cardBackgroundConst.DATES_PATTERN
import com.example.flashcard.util.cardBackgroundConst.FLORAL_PATTERN
import com.example.flashcard.util.cardBackgroundConst.MAP_PATTERN
import com.example.flashcard.util.cardBackgroundConst.SQUARE_PATTERN
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import kotlin.ClassCastException

class NewCardDialog(private val card: Card?, private val deck: ImmutableDeck): AppCompatDialogFragment() {

    private var cardContent: EditText? = null
    private var cardContentDefinition: EditText? = null
    private var cardValue: EditText? = null
    private var cardValueDefinition: EditText? = null
    private var curvePatternBT: LinearLayout? = null
    private var mapPatternBT: LinearLayout? = null
    private var floralPatternBT: LinearLayout? = null
    private var datesPatternBT: LinearLayout? = null
    private var cardContentLY: TextInputLayout? = null
    private var cardValueLY: TextInputLayout? = null

    private var listener: NewDialogListener? = null

    private var cardBackground: String? = null
    private var appContext: Context? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_card_layout_dialog, null)

        appContext = activity?.applicationContext

        cardContent = view?.findViewById(R.id.cardContentTV)
        cardContentDefinition = view?.findViewById(R.id.cardContentDefinitionTV)
        cardValue = view?.findViewById(R.id.cardValueTV)
        cardValueDefinition = view?.findViewById(R.id.cardValueDefinitionTV)
        cardContentLY = view?.findViewById(R.id.cardContentLY)
        cardValueLY = view?.findViewById(R.id.cardValueLY)

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

        cardContentLY?.setEndIconOnClickListener {

        }

        cardValueLY?.setEndIconOnClickListener {
            val contentText = cardContent?.text.toString()
            onTranslateText(contentText)
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

    private fun onTranslateText(text: String) {
        animProgressBar()

        val fl = FirebaseTranslatorHelper().getLanguageCode(deck.deckFirstLanguage!!)
        val tl = FirebaseTranslatorHelper().getLanguageCode(deck.deckSecondLanguage!!)
        if (fl != null && tl != null) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(fl)
                .setTargetLanguage(tl)
                .build()
            val appTranslator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            appTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    cardValue?.setText("Translation in progress...")
                    appTranslator.translate(text)
                        .addOnSuccessListener {
                            cardValue?.setText(it)
                            setEditTextEndIconOnClick()
                        }
                        .addOnFailureListener {
                            cardValue?.setText(it.toString())
                            setEditTextEndIconOnClick()
                        }
                }
                .addOnFailureListener { exception ->
                    setEditTextEndIconOnClick()
                    cardValue?.setText(exception.toString())
                }
        }
    }

    private fun animProgressBar() {
        val drawableChargeIcon = appContext?.getProgressBarDrawable()
        cardValueLY?.endIconMode = TextInputLayout.END_ICON_CUSTOM
        cardValueLY?.endIconDrawable = drawableChargeIcon
        (drawableChargeIcon as? Animatable)?.start()
    }

    private fun setEditTextEndIconOnClick() {
        lifecycleScope.launch {
            val states = ColorStateList(arrayOf(intArrayOf()),
                appContext?.fetchPrimaryColor()?.let { intArrayOf(it) })
            val drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.icon_translate)
            cardValueLY?.setEndIconTintList(states)
            drawable?.setTintList(states)
            cardValueLY?.endIconMode = TextInputLayout.END_ICON_CUSTOM
            cardValueLY?.endIconDrawable = drawable
        }
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


        listener?.getCard(newCard, action, deck)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NewDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement NewDialogListener")
        }
    }

    private fun Context.getProgressBarDrawable(): Drawable {
        val value = TypedValue()
        theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
        val progressBarStyle = value.data
        val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
        val array = obtainStyledAttributes(progressBarStyle, attributes)
        val drawable = array.getDrawableOrThrow(0)
        array.recycle()
        return drawable
    }

    private fun Context.fetchPrimaryColor(): Int {
        val array = obtainStyledAttributes(intArrayOf(android.R.attr.colorPrimary))
        val color = array.getColorOrThrow(0)
        array.recycle()
        return color
    }

    interface NewDialogListener {
        fun getCard(card: Card, action: String, deck: ImmutableDeck)
    }
}