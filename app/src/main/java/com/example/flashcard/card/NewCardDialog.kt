package com.example.flashcard.card

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.TypedValue
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.CardLevel.L1
import com.example.flashcard.util.CardType.FLASHCARD
import com.example.flashcard.util.CardType.ONE_OR_MULTI_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.Constant
import com.example.flashcard.util.FirebaseTranslatorHelper
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NewCardDialog(private val card: ImmutableCard?, private val deck: ImmutableDeck): AppCompatDialogFragment() {

    private var cardContent: EditText? = null
    private var cardContentDefinition: EditText? = null
    private var cardValue: EditText? = null
    private var cardValueDefinition: EditText? = null
    private var tieContentMultiAnswerCard: TextInputEditText? = null
    private var tieDefinition1MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition2MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition3MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition4MultiAnswerCard: TextInputEditText? = null
    private var tieContentTrueOrFalseCard: TextInputEditText? = null

    private var cardContentLY: TextInputLayout? = null
    private var cardValueLY: TextInputLayout? = null
    private var tilContentMultiAnswerCard: TextInputLayout? = null
    private var tilDefinition1MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition2MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition3MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition4MultiAnswerCard: TextInputLayout? = null
    private var tilContentTrueOrFalseCard: TextInputLayout? = null
    private var cpTrue: Chip? = null
    private var cpFalse: Chip? = null

    private var cpAddFlashCard: Chip? = null
    private var cpAddTrueOrFalseCard: Chip? = null
    private var cpAddMultiAnswerCard: Chip? = null
    private var clAddMultiAnswerCardContainer: ConstraintLayout? = null
    private var llAddTrueOrFalseCardContainer: LinearLayout? = null
    private var llAddFlashCardContainer: LinearLayout? = null
    private var cpDefinition1IsTrue: Chip? = null
    private var cpDefinition2IsTrue: Chip? = null
    private var cpDefinition3IsTrue: Chip? = null
    private var cpDefinition4IsTrue: Chip? = null


    private var listener: NewDialogListener? = null

    private var cardBackground: String? = null
    private var appContext: Context? = null
    private var cardType: String? = null
    private var definitionList = mutableSetOf<CardDefinition>()

    companion object {
        private const val REQUEST_PERMISSION_CODE_CONTENT_FLASH_CARD = 12
        private const val REQUEST_PERMISSION_CODE_CONTENT_MAC = 13
        private const val REQUEST_PERMISSION_CODE_DEFINITION_1_MAC = 14
        private const val REQUEST_PERMISSION_CODE_DEFINITION_2_MAC = 15
        private const val REQUEST_PERMISSION_CODE_DEFINITION_3_MAC = 16
        private const val REQUEST_PERMISSION_CODE_DEFINITION_4_MAC = 17
        private const val REQUEST_PERMISSION_CODE_CONTENT_TFC = 18
        private const val RecordAudioRequestCode = 3455
    }

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
        tieContentMultiAnswerCard = view?.findViewById(R.id.tie_content_multi_answer_card)
        tieDefinition1MultiAnswerCard = view?.findViewById(R.id.tie_definition_1_multi_answer_card)
        tieDefinition2MultiAnswerCard = view?.findViewById(R.id.tie_definition_2_multi_answer_card)
        tieDefinition3MultiAnswerCard = view?.findViewById(R.id.tie_definition_3_multi_answer_card)
        tieDefinition4MultiAnswerCard = view?.findViewById(R.id.tie_definition_4_multi_answer_card)
        tieContentTrueOrFalseCard = view?.findViewById(R.id.tie_content_true_or_false_card)

        cardContentLY = view?.findViewById(R.id.cardContentLY)
        cardValueLY = view?.findViewById(R.id.cardValueLY)
        tilContentMultiAnswerCard = view?.findViewById(R.id.til_content_multi_answer_card)
        tilDefinition1MultiAnswerCard = view?.findViewById(R.id.til_definition_1_multi_answer_card)
        tilDefinition2MultiAnswerCard = view?.findViewById(R.id.til_definition_2_multi_answer_card)
        tilDefinition3MultiAnswerCard = view?.findViewById(R.id.til_definition_3_multi_answer_card)
        tilDefinition4MultiAnswerCard = view?.findViewById(R.id.til_definition_4_multi_answer_card)
        tilContentTrueOrFalseCard = view?.findViewById(R.id.til_content_true_or_false_card)

        cpAddFlashCard = view?.findViewById(R.id.cp_add_flash_card)
        cpAddTrueOrFalseCard = view?.findViewById(R.id.cp_add_true_or_false_card)
        cpAddMultiAnswerCard = view?.findViewById(R.id.cp_add_multi_answer)
        llAddFlashCardContainer = view?.findViewById(R.id.ll_add_flash_card_container)
        llAddTrueOrFalseCardContainer = view?.findViewById(R.id.ll_add_true_or_false_card_container)
        clAddMultiAnswerCardContainer = view?.findViewById(R.id.cl_add_multi_answer_card_container)
        cpDefinition1IsTrue = view?.findViewById(R.id.cp_definition_1_is_true)
        cpDefinition2IsTrue = view?.findViewById(R.id.cp_definition_2_is_true)
        cpDefinition3IsTrue = view?.findViewById(R.id.cp_definition_3_is_true)
        cpDefinition4IsTrue = view?.findViewById(R.id.cp_definition_4_is_true)
        cpFalse = view?.findViewById(R.id.cp_false)
        cpTrue = view?.findViewById(R.id.cp_true)

        if (card != null) {
            cardContent?.setText(card.cardContent?.content)
            cardContentDefinition?.setText(card.contentDescription)
            cardValue?.setText(card.cardDefinition?.first()?.definition)
            cardValueDefinition?.setText(card.valueDefinition)

            builder.setView(view)
                .setTitle("Update Card")
                .setNegativeButton("Cancel") { _, _ ->  dismiss()}
                .setPositiveButton("Update"
                ) { _, _ ->
                    onPositiveAction(Constant.UPDATE)
                }
        } else {
            cardContent?.hint = getString(R.string.card_content_hint, deck.deckFirstLanguage)
            cardContentDefinition?.hint = getString(R.string.card_value_definition_hint, deck.deckFirstLanguage)
            cardValue?.hint = getString(R.string.card_definition, deck.deckSecondLanguage)
            cardValueDefinition?.hint = getString(R.string.card_value_definition_hint, deck.deckSecondLanguage)
            builder.setView(view)
                .setTitle("New Card")
                .setNegativeButton("Cancel") { _, _ -> dismiss()}
                .setPositiveButton("Add"
                ) { _, _ ->
                    onPositiveAction(Constant.ADD)
                }
        }

        cardContentLY?.setEndIconOnClickListener {
            listen(REQUEST_PERMISSION_CODE_CONTENT_FLASH_CARD)
        }
        tilContentMultiAnswerCard?.setEndIconOnClickListener {
            listen(REQUEST_PERMISSION_CODE_CONTENT_MAC)
        }
        tilDefinition1MultiAnswerCard?.setEndIconOnClickListener {
            listen(REQUEST_PERMISSION_CODE_DEFINITION_1_MAC)
        }
        tilDefinition2MultiAnswerCard?.setEndIconOnClickListener {
            listen(REQUEST_PERMISSION_CODE_DEFINITION_2_MAC)
        }
        tilDefinition3MultiAnswerCard?.setEndIconOnClickListener {
            listen(REQUEST_PERMISSION_CODE_DEFINITION_3_MAC)
        }
        tilDefinition4MultiAnswerCard?.setEndIconOnClickListener {
            listen(REQUEST_PERMISSION_CODE_DEFINITION_4_MAC)
        }
        tilContentTrueOrFalseCard?.setEndIconOnClickListener {
            listen(REQUEST_PERMISSION_CODE_CONTENT_TFC)
        }

        cardValueLY?.setEndIconOnClickListener {
            val contentText = cardContent?.text.toString()
            onTranslateText(contentText)
        }

        cpAddFlashCard?.setOnCheckedChangeListener { _, isChecked ->
            onAddFlashCard(isChecked)
        }

        cpAddMultiAnswerCard?.setOnCheckedChangeListener { _, isChecked ->
            onAddMultiAnswerCard(isChecked)
        }

        cpAddTrueOrFalseCard?.setOnCheckedChangeListener { _, isChecked ->
            onAddTrueOrFalseCard(isChecked)
        }

        cpDefinition1IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
            onIsDefinitionIsTrueClicked(isChecked, buttonView)
        }

        cpDefinition2IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
            onIsDefinitionIsTrueClicked(isChecked, buttonView)
        }

        cpDefinition3IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
            onIsDefinitionIsTrueClicked(isChecked, buttonView)
        }

        cpDefinition4IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
            onIsDefinitionIsTrueClicked(isChecked, buttonView)
        }

        return builder.create()
    }

    private fun onIsDefinitionIsTrueClicked(isChecked: Boolean, buttonView: CompoundButton) {
        if (isChecked) {
            buttonView.text = getString(R.string.cp_true_text)
        } else {
            buttonView.text = getString(R.string.cp_false_text)
        }
    }

    private fun onAddTrueOrFalseCard(isChecked: Boolean) {
        if (isChecked) {
            llAddTrueOrFalseCardContainer?.isVisible = true
            llAddFlashCardContainer?.isVisible = false
            clAddMultiAnswerCardContainer?.isVisible = false
            cardType = TRUE_OR_FALSE_CARD
        }
    }

    private fun onAddMultiAnswerCard(isChecked: Boolean) {
        if (isChecked) {
            clAddMultiAnswerCardContainer?.isVisible = true
            llAddTrueOrFalseCardContainer?.isVisible = false
            llAddFlashCardContainer?.isVisible = false
            cardType = ONE_OR_MULTI_ANSWER_CARD
        }
    }

    private fun onAddFlashCard(isChecked: Boolean) {
        if (isChecked) {
            llAddFlashCardContainer?.isVisible = true
            llAddTrueOrFalseCardContainer?.isVisible = false
            clAddMultiAnswerCardContainer?.isVisible = false
            cardType = FLASHCARD
        }
    }

    private fun NewCardDialog.listen( requestCode: Int ) {
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, FirebaseTranslatorHelper().getLanguageCodeForSpeechAndText(deck.deckFirstLanguage!!))
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something to translate")

            try {
                startActivityForResult(intent, requestCode)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions( requireActivity(), arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                RecordAudioRequestCode
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_PERMISSION_CODE_CONTENT_FLASH_CARD -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                cardContent?.setText(result?.get(0))
            }
            REQUEST_PERMISSION_CODE_CONTENT_MAC -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                tieContentMultiAnswerCard?.setText(result?.get(0))
            }
            REQUEST_PERMISSION_CODE_DEFINITION_1_MAC -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                tieDefinition1MultiAnswerCard?.setText(result?.get(0))
            }
            REQUEST_PERMISSION_CODE_DEFINITION_2_MAC -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                tieDefinition2MultiAnswerCard?.setText(result?.get(0))
            }
            REQUEST_PERMISSION_CODE_DEFINITION_3_MAC -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                tieDefinition3MultiAnswerCard?.setText(result?.get(0))
            }
            REQUEST_PERMISSION_CODE_DEFINITION_4_MAC -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                tieDefinition4MultiAnswerCard?.setText(result?.get(0))
            }
            REQUEST_PERMISSION_CODE_CONTENT_TFC -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                tieContentTrueOrFalseCard?.setText(result?.get(0))
            }

        }
    }

    private fun onTranslateText(text: String) {
        animProgressBar()

        val fl = FirebaseTranslatorHelper().getLanguageCodeForTranslation(deck.deckFirstLanguage!!)
        val tl = FirebaseTranslatorHelper().getLanguageCodeForTranslation(deck.deckSecondLanguage!!)
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

    private fun onPositiveAction(action: String) {

        val newCard = if (action == Constant.ADD) {

            val newCardContent = getContent()
            val newCardDefinition = getDefinitions()

             ImmutableCard(
                null,
                newCardContent!!,
                null,
                newCardDefinition,
                null,
                deck.deckId,
                 cardBackground,
                 false,
                 0,
                 0,
                 today(),
                 null,
                 L1,
                 null,
                 null,
                 cardType,
                 now()
            )
        } else {

            val newCardContent = CardContent(
                card?.cardContent?.contentId,
                card?.cardContent?.cardId,
                cardContent?.text.toString()
            )
            val newCardDefinition = CardDefinition(
                card?.cardDefinition?.first()?.definitionId,
                card?.cardDefinition?.first()?.cardId,
                card?.cardDefinition?.first()?.contentId,
                cardValue?.text.toString(),
                card?.cardDefinition?.first()?.isCorrectDefinition
            )

            ImmutableCard(
                card?.cardId,
                newCardContent,
                cardContentDefinition?.text.toString(),
                listOf(newCardDefinition),
                cardValueDefinition?.text.toString(),
                card?.deckId,
                cardBackground,
                card?.isFavorite,
                card?.revisionTime,
                card?.missedTime,
                card?.creationDate,
                card?.lastRevisionDate,
                card?.cardStatus,
                card?.nextMissMemorisationDate,
                card?.nextRevisionDate,
                card?.cardType,
                card?.creationDateTime,
            )
        }


        listener?.getCard(newCard, action, deck)
    }

    private fun getDefinitions() = when (cardType ) {
        FLASHCARD -> {getDefinitionOnAddFC()}
        ONE_OR_MULTI_ANSWER_CARD -> {getDefinitionOnAddMAC()}
        TRUE_OR_FALSE_CARD -> { getDefinitionOnAddTFC() }
        else -> {listOf<CardDefinition>()}
    }

    private fun getContent(): CardContent? {
        return when (cardType) {
            FLASHCARD -> {getContentOnAddFC()}
            ONE_OR_MULTI_ANSWER_CARD -> {getContentOnAddMAC()}
            TRUE_OR_FALSE_CARD -> { getContentOnAddTFC() }
            else -> {null}
        }
    }

    private fun getContentOnAddFC(): CardContent? {
        val cardContentText = cardContent?.text.toString()
        return if (cardContentText.isNotEmpty()) {
            CardContent(
                null,
                null,
                cardContentText
            )
        } else {
            null
        }
    }

    private fun getContentOnAddMAC(): CardContent? {
        val cardContentText = tieContentMultiAnswerCard?.text.toString()
        return if (cardContentText.isNotEmpty()) {
            CardContent(
                null,
                null,
                cardContentText
            )
        } else {
            null
        }
    }

    private fun getContentOnAddTFC(): CardContent? {
        val cardContentText = tieContentTrueOrFalseCard?.text.toString()
        return if (cardContentText.isNotEmpty()) {
            CardContent(
                null,
                null,
                cardContentText
            )
        } else {
            null
        }
    }

    private fun getDefinitionOnAddMAC(): List<CardDefinition> {
        definitionList.clear()
        val definition1Text = tieDefinition1MultiAnswerCard?.text.toString()
        if (definition1Text.isNotEmpty()) {
            definitionList.add(createDefinition(definition1Text, cpDefinition1IsTrue?.isChecked!!))
        }
        val definition2Text = tieDefinition2MultiAnswerCard?.text.toString()
        if (definition2Text.isNotEmpty()) {
            definitionList.add(createDefinition(definition2Text, cpDefinition2IsTrue?.isChecked!!))
        }
        val definition3Text = tieDefinition3MultiAnswerCard?.text.toString()
        if (definition3Text.isNotEmpty()) {
            definitionList.add(createDefinition(definition3Text, cpDefinition3IsTrue?.isChecked!!))
        }
        val definition4Text = tieDefinition4MultiAnswerCard?.text.toString()
        if (definition4Text.isNotEmpty()) {
            definitionList.add(createDefinition(definition4Text, cpDefinition4IsTrue?.isChecked!!))
        }
        return definitionList.toList()
    }

    private fun getDefinitionOnAddFC(): List<CardDefinition> {
        definitionList.clear()
        val definitionText = cardValue?.text.toString()
        if (definitionText.isNotEmpty()) {
            definitionList.add(createDefinition(definitionText, true))
        }
        return definitionList.toList()
    }

    private fun getDefinitionOnAddTFC(): List<CardDefinition> {
        definitionList.clear()
        val cpFalseState = cpFalse?.isChecked
        val cpTrueState = cpTrue?.isChecked
        definitionList.add(createDefinition( "False", cpFalseState!!))
        definitionList.add(createDefinition( "True", cpTrueState!!))
        return definitionList.toList()
    }

    private fun createDefinition(text: String, isCorrect: Boolean) = CardDefinition(
        null,
        null,
        null,
        text,
        isCorrect
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NewDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement NewDialogListener")
        }
    }

    private fun today(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return formatter.format(LocalDate.now())
    }

    private fun now(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        return LocalDateTime.now().format(formatter)
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
        fun getCard(card: ImmutableCard, action: String, deck: ImmutableDeck)
    }
}