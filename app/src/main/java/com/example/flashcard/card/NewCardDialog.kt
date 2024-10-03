package com.example.flashcard.card

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.util.CardLevel.L1
import com.example.flashcard.util.CardType.SINGLE_ANSWER_CARD
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.Constant
import com.example.flashcard.util.FirebaseTranslatorHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class NewCardDialog(
    private var card: ImmutableCard?,
    private val deck: ImmutableDeck,
    private val action: String
) : AppCompatDialogFragment() {

    private var cardContent: EditText? = null
    private var cardValue: EditText? = null
    private var tieContentMultiAnswerCard: TextInputEditText? = null
    private var tieDefinition1MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition2MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition3MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition4MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition5MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition6MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition7MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition8MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition9MultiAnswerCard: TextInputEditText? = null
    private var tieDefinition10MultiAnswerCard: TextInputEditText? = null
    private var tieContentTrueOrFalseCard: TextInputEditText? = null

    private var cardContentLY: TextInputLayout? = null
    private var cardValueLY: TextInputLayout? = null
    private var tilContentMultiAnswerCard: TextInputLayout? = null
    private var tilDefinition1MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition2MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition3MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition4MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition5MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition6MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition7MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition8MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition9MultiAnswerCard: TextInputLayout? = null
    private var tilDefinition10MultiAnswerCard: TextInputLayout? = null
    private var tilContentTrueOrFalseCard: TextInputLayout? = null
    private var cpTrue: Chip? = null
    private var cpFalse: Chip? = null

    private var cpAddFlashCard: Chip? = null
    private var cpAddTrueOrFalseCard: Chip? = null
    private var cpAddMultiAnswerCard: Chip? = null
    private var clAddMultiAnswerCardContainer: ConstraintLayout? = null
    private var llAddTrueOrFalseCardContainer: LinearLayout? = null
    private var llAddFlashCardContainer: LinearLayout? = null
    private var cpDefinition1IsTrue: MaterialCheckBox? = null
    private var cpDefinition2IsTrue: MaterialCheckBox? = null
    private var cpDefinition3IsTrue: MaterialCheckBox? = null
    private var cpDefinition4IsTrue: MaterialCheckBox? = null
    private var cpDefinition5IsTrue: MaterialCheckBox? = null
    private var cpDefinition6IsTrue: MaterialCheckBox? = null
    private var cpDefinition7IsTrue: MaterialCheckBox? = null
    private var cpDefinition8IsTrue: MaterialCheckBox? = null
    private var cpDefinition9IsTrue: MaterialCheckBox? = null
    private var cpDefinition10IsTrue: MaterialCheckBox? = null

    private var btDeleteField1: MaterialButton? = null
    private var btDeleteField2: MaterialButton? = null
    private var btDeleteField3: MaterialButton? = null
    private var btDeleteField4: MaterialButton? = null
    private var btDeleteField5: MaterialButton? = null
    private var btDeleteField6: MaterialButton? = null
    private var btDeleteField7: MaterialButton? = null
    private var btDeleteField8: MaterialButton? = null
    private var btDeleteField9: MaterialButton? = null
    private var btDeleteField10: MaterialButton? = null

    private var btAdd: MaterialButton? = null
    private var btCancel: MaterialButton? = null
    private var tvTitleAddedCards: TextView? = null
    private var tabAddAndUpdateNewCard: MaterialToolbar? = null
    private var rvAddedCard: RecyclerView? = null
    private var btAddMoreDefinition: MaterialButton? = null

    private lateinit var rvAddedCardRecyclerViewAdapter: AddedCardRecyclerViewAdapter

    private var appContext: Context? = null
    private var cardType = SINGLE_ANSWER_CARD
    private var definitionList = mutableSetOf<CardDefinition>()

    private var selectedField: EditText? = null
    private var actualFieldLanguage: String? = null

    private val newCardViewModel: NewCardDialogViewModel by viewModels()
    private var actionMode: ActionMode? = null
    private var uri: Uri? = null

    private var revealedDefinitionFields: Int = 1

    private lateinit var definitionFields: List<DefinitionFieldModel>

    private var takePreview =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val image: InputImage
                try {
                    image = InputImage.fromFilePath(requireContext(), uri!!)
                    detectTextWithMLKit(image)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private var takeFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri: Uri? ->
            if (imageUri != null) {
                val image: InputImage
                try {
                    image = InputImage.fromFilePath(requireContext(), imageUri)
                    detectTextWithMLKit(image)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    private var micListener =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultData ->
            if (resultData.resultCode == RESULT_OK) {
                val result =
                    resultData.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (result?.get(0).isNullOrBlank()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_message_no_text_detected),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    selectedField?.setText(result?.get(0))
                }
                actionMode = null
                selectedField = null
                actualFieldLanguage = null
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message_no_text_detected),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    companion object {
        private const val RecordAudioRequestCode = 3455
        const val TAG = "NewCardDialog"
        const val SAVE_CARDS_BUNDLE_KEY = "1"
        const val EDIT_CARD_BUNDLE_KEY = "2"
        const val REQUEST_CODE_CARD = "0"
        private const val PERMISSION_REQUEST_CODE_PHOTO_CAMERA = 123453244
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.QuizeoFullscreenDialogTheme)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_card_layout_dialog, container, false)

        appContext = activity?.applicationContext

//        cardContent = view?.findViewById(R.id.cardContentTV)
//        cardValue = view?.findViewById(R.id.cardValueTV)
        tieContentMultiAnswerCard = view?.findViewById(R.id.tie_content_multi_answer_card)
        tieDefinition1MultiAnswerCard = view?.findViewById(R.id.tie_definition_1_multi_answer_card)
        tieDefinition2MultiAnswerCard = view?.findViewById(R.id.tie_definition_2_multi_answer_card)
        tieDefinition3MultiAnswerCard = view?.findViewById(R.id.tie_definition_3_multi_answer_card)
        tieDefinition4MultiAnswerCard = view?.findViewById(R.id.tie_definition_4_multi_answer_card)
        tieDefinition5MultiAnswerCard = view?.findViewById(R.id.tie_definition_5_multi_answer_card)
        tieDefinition6MultiAnswerCard = view?.findViewById(R.id.tie_definition_6_multi_answer_card)
        tieDefinition7MultiAnswerCard = view?.findViewById(R.id.tie_definition_7_multi_answer_card)
        tieDefinition8MultiAnswerCard = view?.findViewById(R.id.tie_definition_8_multi_answer_card)
        tieDefinition9MultiAnswerCard = view?.findViewById(R.id.tie_definition_9_multi_answer_card)
        tieDefinition10MultiAnswerCard = view?.findViewById(R.id.tie_definition_10_multi_answer_card)
//        tieContentTrueOrFalseCard = view?.findViewById(R.id.tie_content_true_or_false_card)
//
//        cardContentLY = view?.findViewById(R.id.cardContentLY)
//        cardValueLY = view?.findViewById(R.id.cardValueLY)
        tilContentMultiAnswerCard = view?.findViewById(R.id.til_content_multi_answer_card)
        tilDefinition1MultiAnswerCard = view?.findViewById(R.id.til_definition_1_multi_answer_card)
        tilDefinition2MultiAnswerCard = view?.findViewById(R.id.til_definition_2_multi_answer_card)
        tilDefinition3MultiAnswerCard = view?.findViewById(R.id.til_definition_3_multi_answer_card)
        tilDefinition4MultiAnswerCard = view?.findViewById(R.id.til_definition_4_multi_answer_card)
        tilDefinition5MultiAnswerCard = view?.findViewById(R.id.til_definition_5_multi_answer_card)
        tilDefinition6MultiAnswerCard = view?.findViewById(R.id.til_definition_6_multi_answer_card)
        tilDefinition7MultiAnswerCard = view?.findViewById(R.id.til_definition_7_multi_answer_card)
        tilDefinition8MultiAnswerCard = view?.findViewById(R.id.til_definition_8_multi_answer_card)
        tilDefinition9MultiAnswerCard = view?.findViewById(R.id.til_definition_9_multi_answer_card)
        tilDefinition10MultiAnswerCard = view?.findViewById(R.id.til_definition_10_multi_answer_card)
//        tilContentTrueOrFalseCard = view?.findViewById(R.id.til_content_true_or_false_card)
//
//        cpAddFlashCard = view?.findViewById(R.id.cp_add_flash_card)
//        cpAddTrueOrFalseCard = view?.findViewById(R.id.cp_add_true_or_false_card)
//        cpAddMultiAnswerCard = view?.findViewById(R.id.cp_add_multi_answer)
//        llAddFlashCardContainer = view?.findViewById(R.id.ll_add_flash_card_container)
//        llAddTrueOrFalseCardContainer = view?.findViewById(R.id.ll_add_true_or_false_card_container)
        clAddMultiAnswerCardContainer = view?.findViewById(R.id.cl_add_multi_answer_card_container)
        cpDefinition1IsTrue = view?.findViewById(R.id.cp_definition_1_is_true)
        cpDefinition2IsTrue = view?.findViewById(R.id.cp_definition_2_is_true)
        cpDefinition3IsTrue = view?.findViewById(R.id.cp_definition_3_is_true)
        cpDefinition4IsTrue = view?.findViewById(R.id.cp_definition_4_is_true)
        cpDefinition5IsTrue = view?.findViewById(R.id.cp_definition_5_is_true)
        cpDefinition6IsTrue = view?.findViewById(R.id.cp_definition_6_is_true)
        cpDefinition7IsTrue = view?.findViewById(R.id.cp_definition_7_is_true)
        cpDefinition8IsTrue = view?.findViewById(R.id.cp_definition_8_is_true)
        cpDefinition9IsTrue = view?.findViewById(R.id.cp_definition_9_is_true)
        cpDefinition10IsTrue = view?.findViewById(R.id.cp_definition_10_is_true)

        btDeleteField1 = view?.findViewById(R.id.bt_delete_field_1)
        btDeleteField2 = view?.findViewById(R.id.bt_delete_field_2)
        btDeleteField3 = view?.findViewById(R.id.bt_delete_field_3)
        btDeleteField4 = view?.findViewById(R.id.bt_delete_field_4)
        btDeleteField5 = view?.findViewById(R.id.bt_delete_field_5)
        btDeleteField6 = view?.findViewById(R.id.bt_delete_field_6)
        btDeleteField7 = view?.findViewById(R.id.bt_delete_field_7)
        btDeleteField8 = view?.findViewById(R.id.bt_delete_field_8)
        btDeleteField9 = view?.findViewById(R.id.bt_delete_field_9)
        btDeleteField10 = view?.findViewById(R.id.bt_delete_field_10)
//        cpFalse = view?.findViewById(R.id.cp_false)
//        cpTrue = view?.findViewById(R.id.cp_true)

        btAdd = view?.findViewById(R.id.bt_add)
        btCancel = view?.findViewById(R.id.bt_cancel)

        tabAddAndUpdateNewCard = view?.findViewById(R.id.tab_add_new_update_card)

        rvAddedCard = view?.findViewById(R.id.rv_added_card)

        tvTitleAddedCards = view?.findViewById(R.id.tv_title_added_cards)

        btAddMoreDefinition = view?.findViewById(R.id.bt_more_definition)

        definitionFields = listOf(
            DefinitionFieldModel(tilDefinition1MultiAnswerCard!!, tieDefinition1MultiAnswerCard!!, cpDefinition1IsTrue!!, btDeleteField1!!),
            DefinitionFieldModel(tilDefinition2MultiAnswerCard!!, tieDefinition2MultiAnswerCard!!, cpDefinition2IsTrue!!, btDeleteField2!!),
            DefinitionFieldModel(tilDefinition3MultiAnswerCard!!, tieDefinition3MultiAnswerCard!!, cpDefinition3IsTrue!!, btDeleteField3!!),
            DefinitionFieldModel(tilDefinition4MultiAnswerCard!!, tieDefinition4MultiAnswerCard!!, cpDefinition4IsTrue!!, btDeleteField4!!),
            DefinitionFieldModel(tilDefinition5MultiAnswerCard!!, tieDefinition5MultiAnswerCard!!, cpDefinition5IsTrue!!, btDeleteField5!!),
            DefinitionFieldModel(tilDefinition6MultiAnswerCard!!, tieDefinition6MultiAnswerCard!!, cpDefinition6IsTrue!!, btDeleteField6!!),
            DefinitionFieldModel(tilDefinition7MultiAnswerCard!!, tieDefinition7MultiAnswerCard!!, cpDefinition7IsTrue!!, btDeleteField7!!),
            DefinitionFieldModel(tilDefinition8MultiAnswerCard!!, tieDefinition8MultiAnswerCard!!, cpDefinition8IsTrue!!, btDeleteField8!!),
            DefinitionFieldModel(tilDefinition9MultiAnswerCard!!, tieDefinition9MultiAnswerCard!!, cpDefinition9IsTrue!!, btDeleteField9!!),
            DefinitionFieldModel(tilDefinition10MultiAnswerCard!!, tieDefinition10MultiAnswerCard!!, cpDefinition10IsTrue!!, btDeleteField10!!),
        )

        // Add or update card
        if (card != null) {
            tvTitleAddedCards?.isVisible = false
            rvAddedCard?.isVisible = false
            tabAddAndUpdateNewCard?.title = getString(R.string.tv_update_card)
            onUpdateCard(card!!)
        } else {
            tvTitleAddedCards?.isVisible = true
            rvAddedCard?.isVisible = true
            tabAddAndUpdateNewCard?.title = getString(R.string.tv_add_new_card)

            btAdd?.apply {
                text = getString(R.string.bt_text_add)
                setOnClickListener {
                    onPositiveAction(Constant.ADD)
                }
            }
//            onAddFlashCard(true)
        }

        // Save or not (Card)
        tabAddAndUpdateNewCard?.setNavigationOnClickListener {
            onCloseDialog()
        }

        tabAddAndUpdateNewCard?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    // Handle saving
                    if (card != null && action == Constant.UPDATE) {
                        sendCardsOnEdit(
                            REQUEST_CODE_CARD,
                            EDIT_CARD_BUNDLE_KEY,
                            newCardViewModel.addedCards.value
                        )
                        dismiss()
                    } else {
                        sendCardsOnSave(
                            REQUEST_CODE_CARD,
                            SAVE_CARDS_BUNDLE_KEY,
                            newCardViewModel.addedCards.value
                        )
                        dismiss()
                    }
                    true
                }

                R.id.bt_scan_image -> {
                    true
                }

                else -> false
            }
        }

        cardValueLY?.setEndIconOnClickListener {
            val contentText = cardContent?.text.toString()
            onTranslateText(contentText)
        }

        // Chips handling
//        cpAddFlashCard?.setOnCheckedChangeListener { _, isChecked ->
//            onAddFlashCard(isChecked)
//        }
//        cpAddMultiAnswerCard?.setOnCheckedChangeListener { _, isChecked ->
//            onAddMultiAnswerCard(isChecked)
//        }
//        cpAddTrueOrFalseCard?.setOnCheckedChangeListener { _, isChecked ->
//            onAddTrueOrFalseCard(isChecked)
//        }
//        cpDefinition1IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
//            onIsDefinitionIsTrueClicked(isChecked, buttonView)
//        }
//        cpDefinition2IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
//            onIsDefinitionIsTrueClicked(isChecked, buttonView)
//        }
//        cpDefinition3IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
//            onIsDefinitionIsTrueClicked(isChecked, buttonView)
//        }
//        cpDefinition4IsTrue?.setOnCheckedChangeListener { buttonView, isChecked ->
//            onIsDefinitionIsTrueClicked(isChecked, buttonView)
//        }

        // Show added cards
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newCardViewModel.addedCards.collect { cards ->
                    displayAddedCard(cards, deck)
                }
            }
        }

        val callback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(
                    R.menu.menu_add_new_card_top_app_bar_contextual_action_bar,
                    menu
                )
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.bt_scan_image -> {
                        showImageSelectedDialog()
                        true
                    }

                    R.id.bt_mic -> {
                        listen(actualFieldLanguage)
                        true
                    }

                    R.id.save -> {
                        if (card != null && action == Constant.UPDATE) {
                            sendCardsOnEdit(
                                REQUEST_CODE_CARD,
                                EDIT_CARD_BUNDLE_KEY,
                                newCardViewModel.addedCards.value
                            )
                            dismiss()
                        } else {
                            sendCardsOnSave(
                                REQUEST_CODE_CARD,
                                SAVE_CARDS_BUNDLE_KEY,
                                newCardViewModel.addedCards.value
                            )
                            dismiss()
                        }
                        true
                    }

                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }
        }

        btCancel?.setOnClickListener {
            initCardAdditionPanel()
        }

        cardContent?.apply {
            setOnFocusChangeListener { v, hasFocus ->
                onActiveTopAppBarMode(
                    v,
                    deck.deckSecondLanguage,
                    hasFocus,
                    callback,
                    getString(R.string.til_card_content_hint)
                )
            }
            setHint(getString(R.string.card_content_hint, deck.deckFirstLanguage))
        }
//        cardValue?.apply {
//            setOnFocusChangeListener { v, hasFocus ->
//                onActiveTopAppBarMode(
//                    v,
//                    deck.deckSecondLanguage,
//                    hasFocus,
//                    callback,
//                    getString(R.string.til_card_definition_hint)
//                )
//            }
//            setHint(getString(R.string.card_definition, deck.deckSecondLanguage))
//        }
//        tieContentTrueOrFalseCard?.apply {
//            setOnFocusChangeListener { v, hasFocus ->
//                onActiveTopAppBarMode(
//                    v,
//                    deck.deckSecondLanguage,
//                    hasFocus,
//                    callback,
//                    getString(R.string.til_card_content_hint)
//                )
//            }
//            setHint(getString(R.string.card_content_hint, deck.deckFirstLanguage))
//        }
        tieContentMultiAnswerCard?.apply {
            setOnFocusChangeListener { v, hasFocus ->
                onActiveTopAppBarMode(
                    v,
                    deck.deckSecondLanguage,
                    hasFocus,
                    callback,
                    getString(R.string.til_card_content_hint)
                )
            }
            setHint(getString(R.string.card_content_hint, deck.deckFirstLanguage))
        }

        definitionFields.forEach { definitionField ->
            definitionField.fieldEd.setOnFocusChangeListener { v, hasFocus ->
                onActiveTopAppBarMode(
                    v,
                    deck.deckSecondLanguage,
                    hasFocus,
                    callback,
                    getString(R.string.til_card_definition_hint)
                )
            }
            definitionField.fieldEd.setHint(getString(R.string.card_definition, deck.deckSecondLanguage))
            definitionField.chip.setOnCheckedChangeListener { buttonView, isChecked ->
                onIsDefinitionIsTrueClicked(isChecked, buttonView)
            }
            definitionField.btDeleteField.setOnClickListener {
//                definitionField.fieldLy.isVisible = false
//                definitionField.chip.isVisible = false
//                definitionField.btDeleteField.isVisible = false
//                revealedDefinitionFields--
                deleteDefinitionField(definitionField.fieldEd)
            }
        }

//        tieDefinition1MultiAnswerCard?.apply {
//            setOnFocusChangeListener { v, hasFocus ->
//                onActiveTopAppBarMode(
//                    v,
//                    deck.deckSecondLanguage,
//                    hasFocus,
//                    callback,
//                    getString(R.string.til_card_definition_hint)
//                )
//            }
//            setHint(getString(R.string.card_definition, deck.deckSecondLanguage))
//        }
//        tieDefinition2MultiAnswerCard?.apply {
//            setOnFocusChangeListener { v, hasFocus ->
//                onActiveTopAppBarMode(
//                    v,
//                    deck.deckSecondLanguage,
//                    hasFocus,
//                    callback,
//                    getString(R.string.til_card_definition_hint)
//                )
//            }
//            setHint(getString(R.string.card_definition, deck.deckSecondLanguage))
//        }
//        tieDefinition3MultiAnswerCard?.apply {
//            setOnFocusChangeListener { v, hasFocus ->
//                onActiveTopAppBarMode(
//                    v,
//                    deck.deckSecondLanguage,
//                    hasFocus,
//                    callback,
//                    getString(R.string.til_card_definition_hint)
//                )
//            }
//            setHint(getString(R.string.card_definition, deck.deckSecondLanguage))
//        }
//        tieDefinition4MultiAnswerCard?.apply {
//            setOnFocusChangeListener { v, hasFocus ->
//                onActiveTopAppBarMode(
//                    v,
//                    deck.deckSecondLanguage!!,
//                    hasFocus,
//                    callback,
//                    getString(R.string.til_card_definition_hint)
//                )
//            }
//            setHint(getString(R.string.card_definition, deck.deckSecondLanguage))
//        }

        btAddMoreDefinition?.setOnClickListener {
            onAddMoreDefinition()
        }

        return view
    }

    private fun onActiveTopAppBarMode(
        v: View?,
        language: String?,
        hasFocus: Boolean,
        callback: ActionMode.Callback,
        title: String
    ) {
        if (hasFocus) {
            selectedField = v as EditText
            actionMode = view?.startActionMode(callback)
            actionMode?.title = title
            actualFieldLanguage = language
        }
    }

    private fun showImageSelectedDialog() {
        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
        builder.apply {
            setTitle("Select Image")
            setMessage("Please select an option")
            setPositiveButton(
                "Camera"
            ) { dialog, _ ->
                checkCameraPermission()
                dialog?.dismiss()
            }

            setNeutralButton(
                "Cancel"
            ) { dialog, _ -> dialog?.dismiss() }

            setNegativeButton(
                "Gallery"
            ) { dialog, _ ->
                onSelectImageFromGallery()
                dialog?.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.CAMERA
                ),
                PERMISSION_REQUEST_CODE_PHOTO_CAMERA
            )
        } else {
            openCamera2()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE_PHOTO_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera2()
        }
    }

    private fun openCamera2() {
        uri = createImageUri()
        takePreview.launch(uri)
    }

    private fun createImageUri(): Uri {
        val image =
            File(appContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            "package com.example.flashcard.card.FileProvider",
            image
        )
    }

    private fun onSelectImageFromGallery() {
        takeFromGallery.launch("image/*")
    }

    private fun getRecognizer(language: String?): TextRecognizer {
        return when (language) {
            "Chinese" -> {
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            }
            "Vietnamese" -> {
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            }
            "Japanese" -> {
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            }
            "Korean" -> {
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            }
            "Indonesian" -> {
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            }
            "Hindi" -> {
                TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            }
            "Marathi" -> {
                TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            }
            else -> {
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
        }
    }

    private fun detectTextWithMLKit(image: InputImage, language: String? = actualFieldLanguage) {
        val recognizer = getRecognizer(language)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_message_no_text_detected),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    selectedField?.setText(visionText.text)
                }
                selectedField = null
                actionMode = null
                actualFieldLanguage = null
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun displayAddedCard(cardList: List<ImmutableCard?>, deck: ImmutableDeck) {
        rvAddedCardRecyclerViewAdapter = appContext?.let { it ->
            AddedCardRecyclerViewAdapter(
                it,
                cardList,
                deck,
                { cardWithPosition ->
                    card = cardWithPosition.cardToEdit
                    onUpdateCard(card!!, cardWithPosition.position)
                },
                { cardToRemove ->
                    initCardAdditionPanel()
                    areCardTypesEnabled(true)
                    newCardViewModel.removeCard(cardToRemove)
                    rvAddedCardRecyclerViewAdapter.notifyDataSetChanged()
                },
            )
        }!!
        rvAddedCard?.apply {
            adapter = rvAddedCardRecyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(appContext)
        }
    }

    private fun initCardAdditionPanel() {
        tieContentMultiAnswerCard?.text?.clear()
        tilContentMultiAnswerCard?.error = null
//        tieContentTrueOrFalseCard?.text?.clear()
//        tilContentTrueOrFalseCard?.error = null
//        cardContent?.text?.clear()
//        cardContentLY?.error = null
//        cardValue?.text?.clear()
//        cardValueLY?.error = null

        definitionFields.forEach {
            it.fieldEd.text?.clear()
            it.fieldLy.error = null
            it.chip.isChecked = false
        }

//        tieDefinition1MultiAnswerCard?.text?.clear()
//        tilDefinition1MultiAnswerCard?.error = null
//        tieDefinition2MultiAnswerCard?.text?.clear()
//        tilDefinition2MultiAnswerCard?.error = null
//        tieDefinition3MultiAnswerCard?.text?.clear()
//        tilDefinition3MultiAnswerCard?.error = null
//        tieDefinition4MultiAnswerCard?.text?.clear()
//        tilDefinition4MultiAnswerCard?.error = null
//        cpTrue?.isChecked = true
//        cpFalse?.isChecked = false
//        cpDefinition1IsTrue?.isChecked = false
//        cpDefinition2IsTrue?.isChecked = false
//        cpDefinition3IsTrue?.isChecked = false
//        cpDefinition4IsTrue?.isChecked = false
        btAdd?.text = getString(R.string.bt_text_add)
    }

    private fun onCloseDialog() {
        if (newCardViewModel.addedCards.value.isEmpty()) {
            dismiss()
        } else {
            MaterialAlertDialogBuilder(
                requireActivity(),
                R.style.ThemeOverlay_App_MaterialAlertDialog
            )
                .setTitle(getString(R.string.title_unsaved_cards))
                .setMessage(getString(R.string.message_unsaved_cards))
                .setPositiveButton("Yes") { _, _ ->
                    sendCardsOnSave(
                        REQUEST_CODE_CARD,
                        SAVE_CARDS_BUNDLE_KEY,
                        newCardViewModel.addedCards.value
                    )
                    Toast.makeText(
                        appContext,
                        getString(R.string.message_card_registered),
                        Toast.LENGTH_LONG
                    ).show()
                    dismiss()
                }
                .setNegativeButton("No") { _, _ ->
                    Toast.makeText(
                        appContext,
                        getString(R.string.message_card_not_registered),
                        Toast.LENGTH_LONG
                    ).show()
                    dismiss()
                }
                .setNeutralButton("Keep adding") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun onUpdateCard(card: ImmutableCard, indexCard: Int? = null) {
        tieContentMultiAnswerCard?.setText(card.cardContent?.content)
        btAdd?.apply {
            text = getString(R.string.bt_text_update)
            setOnClickListener {
                if (indexCard == null) {
                    onPositiveAction(Constant.UPDATE)
                    sendCardsOnEdit(
                        REQUEST_CODE_CARD,
                        EDIT_CARD_BUNDLE_KEY,
                        newCardViewModel.addedCards.value
                    )
                    dismiss()
                } else {
                    onPositiveAction(Constant.UPDATE, indexCard)
                }
            }
        }

        definitionFields.forEachIndexed { index, fl ->
            if (index < card.cardDefinition?.size!!) {
                fl.fieldLy.visibility = View.VISIBLE
                fl.chip.visibility = View.VISIBLE
                fl.btDeleteField.visibility = View.VISIBLE
                fl.fieldEd.setText(card.cardDefinition[index].definition)
                fl.chip.isChecked = isCorrect(card.cardDefinition[index].isCorrectDefinition)
            } else {
                fl.fieldLy.visibility = View.GONE
                fl.chip.visibility = View.GONE
            }
        }

//        when (card.cardType) {
//            SINGLE_ANSWER_CARD -> {
//                onAddFlashCard(true)
//                cardContent?.setText(card.cardContent?.content)
//                cardValue?.setText(card.cardDefinition?.first()?.definition)
//            }
//
//            TRUE_OR_FALSE_CARD -> {
//                onAddTrueOrFalseCard(true)
//                tieContentTrueOrFalseCard?.setText(card.cardContent?.content)
//                cpFalse?.isChecked = isCorrect(card.cardDefinition?.get(0)?.isCorrectDefinition!!)
//                cpTrue?.isChecked = isCorrect(card.cardDefinition[1].isCorrectDefinition)
//            }
//
//            MULTIPLE_ANSWER_CARD -> {
//                onAddMultiAnswerCard(true)
//                tieContentMultiAnswerCard?.setText(card.cardContent?.content)
//                when (card.cardDefinition?.size) {
//                    1 -> {
//                        tieDefinition1MultiAnswerCard?.setText(card.cardDefinition[0].definition)
//                        cpDefinition1IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[0].isCorrectDefinition)
//                    }
//
//                    2 -> {
//                        tieDefinition1MultiAnswerCard?.setText(card.cardDefinition[0].definition)
//                        cpDefinition1IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[0].isCorrectDefinition)
//                        tieDefinition2MultiAnswerCard?.setText(card.cardDefinition[1].definition)
//                        cpDefinition2IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[1].isCorrectDefinition)
//                    }
//
//                    3 -> {
//                        tieDefinition1MultiAnswerCard?.setText(card.cardDefinition[0].definition)
//                        cpDefinition1IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[0].isCorrectDefinition)
//                        tieDefinition2MultiAnswerCard?.setText(card.cardDefinition[1].definition)
//                        cpDefinition2IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[1].isCorrectDefinition)
//                        tieDefinition3MultiAnswerCard?.setText(card.cardDefinition[2].definition)
//                        cpDefinition3IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[2].isCorrectDefinition)
//                    }
//
//                    4 -> {
//                        tieDefinition1MultiAnswerCard?.setText(card.cardDefinition[0].definition)
//                        cpDefinition1IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[0].isCorrectDefinition)
//                        tieDefinition2MultiAnswerCard?.setText(card.cardDefinition[1].definition)
//                        cpDefinition2IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[1].isCorrectDefinition)
//                        tieDefinition3MultiAnswerCard?.setText(card.cardDefinition[2].definition)
//                        cpDefinition3IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[2].isCorrectDefinition)
//                        tieDefinition4MultiAnswerCard?.setText(card.cardDefinition[3].definition)
//                        cpDefinition4IsTrue?.isChecked =
//                            isCorrect(card.cardDefinition[3].isCorrectDefinition)
//                    }
//                }
//            }
//        }
        areCardTypesEnabled(false)

    }

    fun isCorrect(index: Int?) = index == 1
    fun isCorrectRevers(isCorrect: Boolean?) = if (isCorrect == true) 1 else 0

    private fun onIsDefinitionIsTrueClicked(isChecked: Boolean, buttonView: CompoundButton) {
        if (isChecked) {
            buttonView.text = getString(R.string.cp_true_text)
        } else {
            buttonView.text = getString(R.string.cp_false_text)
        }
    }

    private fun onAddTrueOrFalseCard(isChecked: Boolean) {
        if (isChecked) {
            cpAddTrueOrFalseCard?.isChecked = true
            llAddTrueOrFalseCardContainer?.isVisible = true
            llAddFlashCardContainer?.isVisible = false
            clAddMultiAnswerCardContainer?.isVisible = false
            cardType = TRUE_OR_FALSE_CARD
        }
    }

    private fun onAddMultiAnswerCard(isChecked: Boolean) {
        if (isChecked) {
            cpAddMultiAnswerCard?.isChecked = true
            clAddMultiAnswerCardContainer?.isVisible = true
            llAddTrueOrFalseCardContainer?.isVisible = false
            llAddFlashCardContainer?.isVisible = false
            cardType = MULTIPLE_ANSWER_CARD
        }
    }

    private fun onAddFlashCard(isChecked: Boolean) {
        if (isChecked) {
            cpAddFlashCard?.isChecked = true
            llAddFlashCardContainer?.isVisible = true
            llAddTrueOrFalseCardContainer?.isVisible = false
            clAddMultiAnswerCardContainer?.isVisible = false
            cardType = SINGLE_ANSWER_CARD
        }
    }

    private fun onPositiveAction(action: String, indexCardOnUpdate: Int? = null) {
        val newCard = if (action == Constant.ADD) {
            generateCardOnAdd() ?: return
        } else {
            generateCardOnUpdate() ?: return
        }

        if (action == Constant.UPDATE && indexCardOnUpdate != null) {
            newCardViewModel.updateCard(newCard, indexCardOnUpdate)
            areCardTypesEnabled(true)
        } else {
            newCardViewModel.addCard(newCard)
        }
        rvAddedCardRecyclerViewAdapter.notifyDataSetChanged()
        initCardAdditionPanel()
    }

    private fun areCardTypesEnabled(enabled: Boolean) {
        cpAddFlashCard?.apply {
            isCheckable = enabled
            isEnabled = enabled
        }
        cpAddMultiAnswerCard?.apply {
            isCheckable = enabled
            isEnabled = enabled
        }
        cpAddTrueOrFalseCard?.apply {
            isCheckable = enabled
            isEnabled = enabled
        }
    }

    private fun generateCardOnUpdate(): ImmutableCard? {
//        val content = getContent(card?.cardId!!, card!!.cardContent?.contentId!!, card?.deckId!!)
//            ?: return null
//
//        val definitions =
//            getDefinitions(card?.cardId!!, card!!.cardContent?.contentId!!, card?.deckId!!)
//                ?: return null
        val content = getContent2(card?.cardId!!, card!!.cardContent?.contentId!!, card?.deckId!!)
            ?: return null

        val definitions =
            getDefinition2(card?.cardId!!, card!!.cardContent?.contentId!!, card?.deckId!!)
                ?: return null
        val updateCardDefinitions = mutableListOf<CardDefinition>()
        for (i in 0..card?.cardDefinition?.size?.minus(1)!!) {
            val definition = try {
                definitions[i]
            } catch (e: java.lang.IndexOutOfBoundsException) {
                createDefinition(
                    "",
                    false,
                    card!!.cardId,
                    card!!.cardContent?.contentId!!,
                    card?.deckId!!
                )
            }

            val updatedDefinition = CardDefinition(
                card!!.cardDefinition?.get(i)?.definitionId,
                card!!.cardDefinition?.get(i)?.cardId!!,
                card!!.cardDefinition?.get(i)?.deckId!!,
                card!!.cardDefinition?.get(i)?.contentId!!,
                definition.definition,
                definition.isCorrectDefinition
            )
            updateCardDefinitions.add(updatedDefinition)
        }
        if (definitions.size > card!!.cardDefinition?.size!!) {
            for (j in (card!!.cardDefinition?.size ?: 0)..definitions.size.minus(1)) {
                updateCardDefinitions.add(definitions[j])
            }
        }

        return ImmutableCard(
            card!!.cardId,
            content,
            updateCardDefinitions,
            card!!.deckId,
            card!!.isFavorite,
            card!!.revisionTime,
            card!!.missedTime,
            card!!.creationDate,
            card!!.lastRevisionDate,
            card!!.cardStatus,
            card!!.nextMissMemorisationDate,
            card!!.nextRevisionDate,
            getCardType(definitions),
        )
    }

    fun generateCardOnAdd(): ImmutableCard? {
        val cardId = now()
        val contentId = now()
//        val newCardContent = getContent(cardId, contentId, deck.deckId)
//        val newCardDefinition = getDefinitions(cardId, contentId, deck.deckId)
        val newCardContent = getContent2(cardId, contentId, deck.deckId)
        val newCardDefinition = getDefinition2(cardId, contentId, deck.deckId)

        if (newCardContent == null) {
            return null
        }
        if (newCardDefinition == null) {
            return null
        }

        return ImmutableCard(
            cardId,
            newCardContent,
            newCardDefinition,
            deck.deckId,
            isCorrect(0),
            0,
            0,
            today(),
            null,
            L1,
            null,
            null,
            getCardType(newCardDefinition),
        )
    }

    private fun getCardType(definitions: List<CardDefinition>): String {
        return if (definitions.size > 1) {
            MULTIPLE_ANSWER_CARD
        } else {
            SINGLE_ANSWER_CARD
        }
    }

    private fun getDefinitions(cardId: String, contentId: String, deckId: String) =
        when (cardType) {
            SINGLE_ANSWER_CARD -> {
                getDefinitionOnAddFC(cardId, contentId, deckId)
            }

            MULTIPLE_ANSWER_CARD -> {
                getDefinitionOnAddMAC(cardId, contentId, deckId)
            }

            TRUE_OR_FALSE_CARD -> {
                getDefinitionOnAddTFC(cardId, contentId, deckId)
            }

            else -> {
                listOf<CardDefinition>()
            }
        }

    private fun getContent2(cardId: String, contentId: String, deckId: String): CardContent? {
        val cardContentText = tieContentMultiAnswerCard?.text.toString()
        return if (cardContentText.isNotEmpty() && cardContentText.isNotBlank()) {
            CardContent(
                contentId,
                cardId,
                deckId,
                cardContentText
            )
        } else {
            tilContentMultiAnswerCard?.error = getString(R.string.til_error_card_content)
            null
        }
    }

    private fun isDefinitionError(): Boolean {
        var isText = false
        var isTrueAnswer = false
        definitionFields.forEach {
            if (it.fieldEd.text.toString().isNotEmpty() && it.fieldEd.text.toString().isNotBlank())  {
                isText = true
            }
            if (it.chip.isChecked && it.fieldEd.text.toString().isNotEmpty() && it.fieldEd.text.toString().isNotBlank()) {
                if (isText) {
                    return false
                }
            }
        }
        return true
    }

    private fun getDefinition2(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition>? {
        if (isDefinitionError()) {
            tilDefinition1MultiAnswerCard?.error = getString(R.string.til_error_card_definition)
            return null
        } else {
            definitionList.clear()
            definitionFields.forEach {
                if (it.fieldEd.text.toString().isNotEmpty() && it.fieldEd.text.toString().isNotBlank()) {
                    definitionList.add(
                        createDefinition(
                            it.fieldEd.text.toString(),
                            it.chip.isChecked,
                            cardId,
                            contentId,
                            deckId
                        )
                    )
                }
            }
        }
        return definitionList.toList()
    }

    private fun getContent(cardId: String, contentId: String, deckId: String): CardContent? {
        return when (cardType) {
            SINGLE_ANSWER_CARD -> {
                getContentOnAddFC(cardId, contentId, deckId)
            }

            MULTIPLE_ANSWER_CARD -> {
                getContentOnAddMAC(cardId, contentId, deckId)
            }

            TRUE_OR_FALSE_CARD -> {
                getContentOnAddTFC(cardId, contentId, deckId)
            }

            else -> {
                null
            }
        }
    }

    private fun getContentOnAddFC(cardId: String, contentId: String, deckId: String): CardContent? {
        val cardContentText = cardContent?.text.toString()
        return if (cardContentText.isNotBlank() && cardContentText.isNotEmpty()) {
            CardContent(
                contentId,
                cardId,
                deckId,
                cardContentText
            )
        } else {
            cardContentLY?.error = getString(R.string.til_error_card_definition)
            null
        }
    }

    private fun getContentOnAddMAC(
        cardId: String,
        contentId: String,
        deckId: String
    ): CardContent? {
        val cardContentText = tieContentMultiAnswerCard?.text.toString()
        return if (cardContentText.isNotEmpty() && cardContentText.isNotBlank()) {
            CardContent(
                contentId,
                cardId,
                deckId,
                cardContentText
            )
        } else {
            tilContentMultiAnswerCard?.error = getString(R.string.til_error_card_content)
            null
        }
    }

    private fun getContentOnAddTFC(
        cardId: String,
        contentId: String,
        deckId: String
    ): CardContent? {
        val cardContentText = tieContentTrueOrFalseCard?.text.toString()
        return if (cardContentText.isNotEmpty() && cardContentText.isNotBlank()) {
            CardContent(
                contentId,
                cardId,
                deckId,
                cardContentText
            )
        } else {
            tilContentTrueOrFalseCard?.error = getString(R.string.til_error_card_content)
            null
        }
    }

    private fun getDefinitionOnAddMAC(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition>? {
        definitionList.clear()
        val definition1Text = tieDefinition1MultiAnswerCard?.text.toString()
        val definition2Text = tieDefinition2MultiAnswerCard?.text.toString()
        val definition3Text = tieDefinition3MultiAnswerCard?.text.toString()
        val definition4Text = tieDefinition4MultiAnswerCard?.text.toString()

        if (
            definition1Text.isEmpty() &&
            definition2Text.isEmpty() &&
            definition3Text.isEmpty() &&
            definition4Text.isEmpty() ||
            definition1Text.isBlank() &&
            definition2Text.isBlank() &&
            definition3Text.isBlank() &&
            definition4Text.isBlank()
        ) {
            tilDefinition1MultiAnswerCard?.error = getString(R.string.til_error_card_definition)
            return null
        }

        if (
            !cpDefinition1IsTrue?.isChecked!! &&
            !cpDefinition2IsTrue?.isChecked!! &&
            !cpDefinition3IsTrue?.isChecked!! &&
            !cpDefinition4IsTrue?.isChecked!!
        ) {
            tilDefinition1MultiAnswerCard?.error = getString(R.string.cp_error_correct_definition)
            return null
        }

        if (definition1Text.isNotEmpty() && definition1Text.isNotBlank()) {
            definitionList.add(
                createDefinition(
                    definition1Text,
                    cpDefinition1IsTrue?.isChecked!!,
                    cardId,
                    contentId,
                    deckId
                )
            )
        }
        if (definition2Text.isNotEmpty() && definition2Text.isNotBlank()) {
            definitionList.add(
                createDefinition(
                    definition2Text,
                    cpDefinition2IsTrue?.isChecked!!,
                    cardId,
                    contentId,
                    deckId
                )
            )
        }
        if (definition3Text.isNotEmpty() && definition3Text.isNotBlank()) {
            definitionList.add(
                createDefinition(
                    definition3Text,
                    cpDefinition3IsTrue?.isChecked!!,
                    cardId,
                    contentId,
                    deckId
                )
            )
        }
        if (definition4Text.isNotEmpty() && definition4Text.isNotBlank()) {
            definitionList.add(
                createDefinition(
                    definition4Text,
                    cpDefinition4IsTrue?.isChecked!!,
                    cardId,
                    contentId,
                    deckId
                )
            )
        }
        return definitionList.toList()
    }

    private fun getDefinitionOnAddFC(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition>? {
        definitionList.clear()
        val definitionText = cardValue?.text.toString()
        if (definitionText.isNotEmpty() && definitionText.isNotBlank()) {
            definitionList.add(createDefinition(definitionText, true, cardId, contentId, deckId))
        } else {
            cardValueLY?.error = getString(R.string.til_error_card_definition)
            return null
        }
        return definitionList.toList()
    }

    private fun getDefinitionOnAddTFC(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition> {
        definitionList.clear()
        val cpFalseState = cpFalse?.isChecked
        val cpTrueState = cpTrue?.isChecked
        definitionList.add(createDefinition("False", cpFalseState!!, cardId, contentId, deckId))
        definitionList.add(createDefinition("True", cpTrueState!!, cardId, contentId, deckId))
        return definitionList.toList()
    }

    private fun createDefinition(
        text: String,
        isCorrect: Boolean,
        cardId: String,
        contentId: String,
        deckId: String
    ) = CardDefinition(
        null,
        cardId,
        deckId,
        contentId,
        text,
        isCorrectRevers(isCorrect)
    )

    private fun today(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return formatter.format(LocalDate.now())
    }

    private fun now(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        return LocalDateTime.now().format(formatter)
    }

    private fun NewCardDialog.listen(language: String?) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        } else {
            val languageExtra =
                if (language != null) FirebaseTranslatorHelper().getLanguageCodeForSpeechAndText(
                    language
                ) else Locale.getDefault()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                languageExtra
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.message_on_voice_to_text_recording)
            )

            try {
                micListener.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                RecordAudioRequestCode
            )
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
            val drawable =
                AppCompatResources.getDrawable(requireContext(), R.drawable.icon_translate)
            cardValueLY?.setEndIconTintList(states)
            drawable?.setTintList(states)
            cardValueLY?.endIconMode = TextInputLayout.END_ICON_CUSTOM
            cardValueLY?.endIconDrawable = drawable
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

    private fun sendCardsOnSave(
        requestCode: String,
        bundleCode: String,
        cards: List<ImmutableCard>
    ) {
        parentFragmentManager.setFragmentResult(requestCode, bundleOf(bundleCode to cards))
        newCardViewModel.removeAllCards()
    }

    private fun sendCardsOnEdit(
        requestCode: String,
        bundleCode: String,
        cards: List<ImmutableCard>
    ) {
        parentFragmentManager.setFragmentResult(requestCode, bundleOf(bundleCode to cards))
        newCardViewModel.removeAllCards()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (newCardViewModel.areThereUnSavedAddedCards()) {
            if (newCardViewModel.areThereUnSavedAddedCards()) {
                sendCardsOnSave(
                    REQUEST_CODE_CARD,
                    SAVE_CARDS_BUNDLE_KEY,
                    newCardViewModel.addedCards.value
                )
            }
        }
    }

    private fun onAddMoreDefinition() {

        if (revealedDefinitionFields < definitionFields.size) {
            definitionFields[revealedDefinitionFields].apply {
                fieldLy.isVisible = true
                fieldEd.isVisible = true
                chip.isVisible = true
                btDeleteField.isVisible = true
            }
            revealedDefinitionFields++

        }
    }

    private fun deleteDefinitionField(field: TextInputEditText) {

        if (field == tieDefinition10MultiAnswerCard) {
            clearField(definitionFields.last())
            return
        }

        var index = 0
        while (true) {
            val actualField = definitionFields[index]
            if (actualField.fieldEd == field) {
                break
            }
            index++
        }
        while (true) {
            val actualField = definitionFields[index]
            val nextField = definitionFields[index.plus(1)]
            if (!nextField.fieldLy.isVisible) {
                clearField(actualField)
                break
            }
            if (
                nextField.fieldEd.text?.isNotBlank() == true &&
                nextField.fieldEd.text?.isNotEmpty() == true
            ) {
                actualField.fieldEd.text = nextField.fieldEd.text
                nextField.fieldEd.text?.clear()
            } else {
                actualField.fieldEd.text?.clear()
                clearField(definitionFields.filter { it.fieldLy.isVisible }.last())
                break
            }
            index++
        }

    }

    private fun clearField(field: DefinitionFieldModel) {
        field.apply {
            btDeleteField.visibility = View.GONE
            fieldLy.visibility = View.GONE
            fieldEd.apply {
                visibility = View.GONE
                text?.clear()
            }
            chip.apply {
                visibility = View.GONE
                isChecked = false
            }
        }
        revealedDefinitionFields--
    }

}