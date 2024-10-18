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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
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
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardContent
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.databinding.AddCardLayoutDialogBinding
import com.example.flashcard.util.CardLevel.L1
import com.example.flashcard.util.CardType.SINGLE_ANSWER_CARD
import com.example.flashcard.util.CardType.MULTIPLE_ANSWER_CARD
import com.example.flashcard.util.CardType.MULTIPLE_CHOICE_CARD
import com.example.flashcard.util.Constant
import com.example.flashcard.util.LanguageUtil
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
import kotlinx.coroutines.delay
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

    private var _binding: AddCardLayoutDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAddedCardRecyclerViewAdapter: AddedCardRecyclerViewAdapter
    private var appContext: Context? = null
    private var definitionList = mutableSetOf<CardDefinition>()
    private var selectedField: EditText? = null
    private var selectedFieldLy: TextInputLayout? = null
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
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message_no_text_detected),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_message_permission_not_granted_camera),
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
    ): View {
        _binding = AddCardLayoutDialogBinding.inflate(inflater, container, false)
        appContext = activity?.applicationContext
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        definitionFields = listOf(
            DefinitionFieldModel(binding.tilDefinition1MultiAnswerCard, binding.tieDefinition1MultiAnswerCard, binding.cpDefinition1IsTrue, binding.btDeleteField1),
            DefinitionFieldModel(binding.tilDefinition2MultiAnswerCard, binding.tieDefinition2MultiAnswerCard, binding.cpDefinition2IsTrue, binding.btDeleteField2),
            DefinitionFieldModel(binding.tilDefinition3MultiAnswerCard, binding.tieDefinition3MultiAnswerCard, binding.cpDefinition3IsTrue, binding.btDeleteField3),
            DefinitionFieldModel(binding.tilDefinition4MultiAnswerCard, binding.tieDefinition4MultiAnswerCard, binding.cpDefinition4IsTrue, binding.btDeleteField4),
            DefinitionFieldModel(binding.tilDefinition5MultiAnswerCard, binding.tieDefinition5MultiAnswerCard, binding.cpDefinition5IsTrue, binding.btDeleteField5),
            DefinitionFieldModel(binding.tilDefinition6MultiAnswerCard, binding.tieDefinition6MultiAnswerCard, binding.cpDefinition6IsTrue, binding.btDeleteField6),
            DefinitionFieldModel(binding.tilDefinition7MultiAnswerCard, binding.tieDefinition7MultiAnswerCard, binding.cpDefinition7IsTrue, binding.btDeleteField7),
            DefinitionFieldModel(binding.tilDefinition8MultiAnswerCard, binding.tieDefinition8MultiAnswerCard, binding.cpDefinition8IsTrue, binding.btDeleteField8),
            DefinitionFieldModel(binding.tilDefinition9MultiAnswerCard, binding.tieDefinition9MultiAnswerCard, binding.cpDefinition9IsTrue, binding.btDeleteField9),
            DefinitionFieldModel(binding.tilDefinition10MultiAnswerCard, binding.tieDefinition10MultiAnswerCard, binding.cpDefinition10IsTrue, binding.btDeleteField10),
        )

        if (card != null) {
            binding.tvTitleAddedCards.isVisible = false
            binding.rvAddedCard.isVisible = false
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_update_card)
            onUpdateCard(card!!)
        } else {
            binding.tvTitleAddedCards.isVisible = true
            binding.rvAddedCard.isVisible = true
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_add_new_card)

            binding.btAdd.apply {
                text = getString(R.string.bt_text_add)
                setOnClickListener {
                    onPositiveAction(Constant.ADD)
                }
            }
        }

        binding.tabAddNewUpdateCard.setNavigationOnClickListener {
            onCloseDialog()
        }

        binding.tabAddNewUpdateCard.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
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

                R.id.bt_scan_image -> {
                    true
                }

                else -> false
            }
        }

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

                    R.id.bt_translate -> {
                        onTranslateText(binding.tieContentMultiAnswerCard.text.toString(), selectedField, selectedFieldLy)
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

        binding.btCancel.setOnClickListener {
            initCardAdditionPanel()
        }

        binding.tieContentMultiAnswerCard.apply {
            setOnFocusChangeListener { v, hasFocus ->
                onActiveTopAppBarMode(
                    binding.tilContentMultiAnswerCard,
                    v,
                    deck.deckFirstLanguage,
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
                    definitionField.fieldLy,
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
                deleteDefinitionField(definitionField.fieldEd)
            }
        }


        binding.btMoreDefinition.setOnClickListener {
            onAddMoreDefinition()
        }

    }

    private fun onActiveTopAppBarMode(
        ly: TextInputLayout?,
        v: View?,
        language: String?,
        hasFocus: Boolean,
        callback: ActionMode.Callback,
        title: String
    ) {
        if (hasFocus) {
            selectedFieldLy = ly
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
            startCameraPermissionRequest()
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
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
                    newCardViewModel.removeCard(cardToRemove)
                    rvAddedCardRecyclerViewAdapter.notifyDataSetChanged()
                },
            )
        }!!
        binding.rvAddedCard.apply {
            adapter = rvAddedCardRecyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(appContext)
        }
    }

    private fun initCardAdditionPanel() {
        binding.tieContentMultiAnswerCard.text?.clear()
        binding.tilContentMultiAnswerCard.error = null
        definitionFields.forEach {
            it.fieldEd.text?.clear()
            it.fieldLy.error = null
            it.chip.isChecked = false
        }
        binding.btAdd.text = getString(R.string.bt_text_add)
        actionMode?.finish()
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
        binding.tieContentMultiAnswerCard.setText(card.cardContent?.content)
        binding.btAdd.apply {
            text = getString(R.string.bt_text_update)
            setOnClickListener {
                if (indexCard == null) {

                    if (onPositiveAction(Constant.UPDATE)) {
                        sendCardsOnEdit(
                            REQUEST_CODE_CARD,
                            EDIT_CARD_BUNDLE_KEY,
                            newCardViewModel.addedCards.value
                        )
                        dismiss()
                    }
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

    private fun onPositiveAction(action: String, indexCardOnUpdate: Int? = null): Boolean {
        val newCard = if (action == Constant.ADD) {
            generateCardOnAdd() ?: return false
        } else {
            generateCardOnUpdate() ?: return false
        }

        if (action == Constant.UPDATE && indexCardOnUpdate != null) {
            newCardViewModel.updateCard(newCard, indexCardOnUpdate)
        } else {
            newCardViewModel.addCard(newCard)
        }
        rvAddedCardRecyclerViewAdapter.notifyDataSetChanged()
        initCardAdditionPanel()
        return true
    }


    private fun generateCardOnUpdate(): ImmutableCard? {

        val content = getContent2(card?.cardId!!, card!!.cardContent?.contentId!!, card?.deckId!!) ?: return null
        val definitions = getDefinition2(card?.cardId!!, card!!.cardContent?.contentId!!, card?.deckId!!) ?: return null

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

    private fun generateCardOnAdd(): ImmutableCard? {
        val cardId = now()
        val contentId = now()
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
        val definitionSum = definitions.size
        val correctDefinitions = (definitions.filter { isCorrect(it.isCorrectDefinition) }).size
        return when {
            correctDefinitions == 1 && definitionSum == 1 -> {
                SINGLE_ANSWER_CARD
            }
            correctDefinitions == 1 && definitionSum > 1 -> {
                MULTIPLE_CHOICE_CARD
            }
            else -> {
                MULTIPLE_ANSWER_CARD
            }
        }
    }

    private fun getContent2(cardId: String, contentId: String, deckId: String): CardContent? {
        val cardContentText = binding.tieContentMultiAnswerCard.text.toString()
        return if (cardContentText.isNotEmpty() && cardContentText.isNotBlank()) {
            CardContent(
                contentId,
                cardId,
                deckId,
                cardContentText
            )
        } else {
            binding.tilContentMultiAnswerCard.error = getString(R.string.til_error_card_content)
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
            if (it.chip.isChecked) {
                isTrueAnswer = true
            }
        }
        if (!isTrueAnswer) {
            binding.tilDefinition1MultiAnswerCard.error = getString(R.string.cp_error_correct_definition)
        }
        if (!isText) {
            binding.tilDefinition1MultiAnswerCard.error = getString(R.string.til_error_card_definition)
        }
        return true
    }

    private fun getDefinition2(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition>? {
        if (isDefinitionError()) {
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

    private fun getContentOnAddMAC(
        cardId: String,
        contentId: String,
        deckId: String
    ): CardContent? {
        val cardContentText = binding.tieContentMultiAnswerCard.text.toString()
        return if (cardContentText.isNotEmpty() && cardContentText.isNotBlank()) {
            CardContent(
                contentId,
                cardId,
                deckId,
                cardContentText
            )
        } else {
            binding.tilContentMultiAnswerCard.error = getString(R.string.til_error_card_content)
            null
        }
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
                if (language != null) LanguageUtil().getLanguageCodeForSpeechAndText(
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

    private fun onTranslateText(
        text: String,
        actualField: EditText?,
        ly: TextInputLayout?
    ) {
        animProgressBar(ly)

        val fl = LanguageUtil().getLanguageCodeForTranslation(deck.deckFirstLanguage!!)
        val tl = LanguageUtil().getLanguageCodeForTranslation(deck.deckSecondLanguage!!)
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
                    actualField?.setText(getString(R.string.message_translation_in_progress))
                    appTranslator.translate(text)
                        .addOnSuccessListener {
                            actualField?.setText(it)
                            setEditTextEndIconOnClick(ly)
                        }
                        .addOnFailureListener {
                            ly?.error = it.toString()
                            setEditTextEndIconOnClick(ly)
                        }
                }
                .addOnFailureListener { exception ->
                    setEditTextEndIconOnClick(ly)
                    ly?.error = exception.toString()
                }
        }
    }

    private fun animProgressBar(ly: TextInputLayout?) {
        val drawableChargeIcon = appContext?.getProgressBarDrawable()
        ly?.endIconMode = TextInputLayout.END_ICON_CUSTOM
        ly?.endIconDrawable = drawableChargeIcon
        (drawableChargeIcon as? Animatable)?.start()
    }

    private fun setEditTextEndIconOnClick(ly: TextInputLayout?) {
        lifecycleScope.launch {
            val states = ColorStateList(arrayOf(intArrayOf()),
                appContext?.fetchPrimaryColor()?.let { intArrayOf(it) })
            val drawable =
                AppCompatResources.getDrawable(requireContext(), R.drawable.icon_translate)
            ly?.setEndIconTintList(states)
            drawable?.setTintList(states)
            ly?.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
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

        if (field == binding.tieDefinition10MultiAnswerCard) {
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

    private fun startCameraPermissionRequest() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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