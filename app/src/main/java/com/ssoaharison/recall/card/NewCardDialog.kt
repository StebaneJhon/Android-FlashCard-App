package com.ssoaharison.recall.card

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
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
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.entities.CardContent
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.databinding.AddCardLayoutDialogBinding
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import com.ssoaharison.recall.util.CardType.MULTIPLE_ANSWER_CARD
import com.ssoaharison.recall.util.CardType.MULTIPLE_CHOICE_CARD
import com.ssoaharison.recall.util.Constant
import com.ssoaharison.recall.util.LanguageUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.deck.DeckFragment.Companion.REQUEST_CODE
import com.ssoaharison.recall.deck.OpenTriviaQuizModel
import com.ssoaharison.recall.deck.UploadOpenTriviaQuizDialog
import com.ssoaharison.recall.util.UiState
import com.ssoaharison.recall.util.parcelable
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.util.Locale

class NewCardDialog(
    private var card: ImmutableCard?,
    private val deck: ImmutableDeck,
    private val action: String
) : AppCompatDialogFragment() {

    private var _binding: AddCardLayoutDialogBinding? = null
    private val binding get() = _binding!!
//    private lateinit var rvAddedCardRecyclerViewAdapter: AddedCardRecyclerViewAdapter
    private var appContext: Context? = null
    private var definitionList = mutableSetOf<CardDefinition>()
    private var selectedField: EditText? = null
    private var selectedFieldLy: TextInputLayout? = null
    private var actualFieldLanguage: String? = null
    private var cardUploadingJob: Job? = null
    private val supportedLanguages = LanguageUtil().getSupportedLang()

    private val newCardViewModel by lazy {
        val openTriviaRepository = (requireActivity().application as FlashCardApplication).openTriviaRepository
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(
            this,
            NewCardDialogViewModelFactory(openTriviaRepository, repository)
        )[NewCardDialogViewModel::class.java]
    }

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
                    detectTextFromAnImageWithMLKit(image)
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
                    detectTextFromAnImageWithMLKit(image)
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
    ) { isGranted ->
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
        private const val RECORD_AUDIO_REQUEST_CODE = 3455
        const val TAG = "NewCardDialog"
        const val SAVE_CARDS_BUNDLE_KEY = "1"
        const val EDIT_CARD_BUNDLE_KEY = "2"
        const val REQUEST_CODE_CARD = "0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.QuizeoFullscreenDialogTheme)
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

//    override fun onDestroy() {
//        super.onDestroy()
//        if (newCardViewModel.areThereUnSavedAddedCards()) {
//            if (newCardViewModel.areThereUnSavedAddedCards()) {
//                sendCardsOnSave(newCardViewModel.addedCards.value)
//            }
//        }
//    }

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
            DefinitionFieldModel(
                binding.tilDefinition1MultiAnswerCard,
                binding.tieDefinition1MultiAnswerCard,
                binding.cpDefinition1IsTrue,
                null
            ),
            DefinitionFieldModel(
                binding.tilDefinition2MultiAnswerCard,
                binding.tieDefinition2MultiAnswerCard,
                binding.cpDefinition2IsTrue,
                binding.btDeleteField2
            ),
            DefinitionFieldModel(
                binding.tilDefinition3MultiAnswerCard,
                binding.tieDefinition3MultiAnswerCard,
                binding.cpDefinition3IsTrue,
                binding.btDeleteField3
            ),
            DefinitionFieldModel(
                binding.tilDefinition4MultiAnswerCard,
                binding.tieDefinition4MultiAnswerCard,
                binding.cpDefinition4IsTrue,
                binding.btDeleteField4
            ),
            DefinitionFieldModel(
                binding.tilDefinition5MultiAnswerCard,
                binding.tieDefinition5MultiAnswerCard,
                binding.cpDefinition5IsTrue,
                binding.btDeleteField5
            ),
            DefinitionFieldModel(
                binding.tilDefinition6MultiAnswerCard,
                binding.tieDefinition6MultiAnswerCard,
                binding.cpDefinition6IsTrue,
                binding.btDeleteField6
            ),
            DefinitionFieldModel(
                binding.tilDefinition7MultiAnswerCard,
                binding.tieDefinition7MultiAnswerCard,
                binding.cpDefinition7IsTrue,
                binding.btDeleteField7
            ),
            DefinitionFieldModel(
                binding.tilDefinition8MultiAnswerCard,
                binding.tieDefinition8MultiAnswerCard,
                binding.cpDefinition8IsTrue,
                binding.btDeleteField8
            ),
            DefinitionFieldModel(
                binding.tilDefinition9MultiAnswerCard,
                binding.tieDefinition9MultiAnswerCard,
                binding.cpDefinition9IsTrue,
                binding.btDeleteField9
            ),
            DefinitionFieldModel(
                binding.tilDefinition10MultiAnswerCard,
                binding.tieDefinition10MultiAnswerCard,
                binding.cpDefinition10IsTrue,
                binding.btDeleteField10
            ),
        )

        val arrayAdapterSupportedLanguages =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)
        val listPopupWindow = ListPopupWindow(appContext!!, null)
        listPopupWindow.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.filter_spinner_dropdown_background,
                requireActivity().theme
            )
        )

        binding.btContentLanguage.setOnClickListener { button ->
            listPopupWindow.apply {
                anchorView = binding.llContainerContentDetails
                setAdapter(arrayAdapterSupportedLanguages)
                setOnItemClickListener { parent, view, position, id ->
                    (button as MaterialButton).text = supportedLanguages[position]
                    dismiss()
                }
                show()
            }
        }

        binding.btDefinitionLanguage.setOnClickListener { button ->
            listPopupWindow.apply {
                anchorView = binding.llContainerDefinitionDetails
                setAdapter(arrayAdapterSupportedLanguages)
                setOnItemClickListener { parent, view, position, id ->
                    (button as MaterialButton).text = supportedLanguages[position]
                    dismiss()
                }
                show()
            }
        }

        if (card != null && action == Constant.UPDATE) {
//            binding.tvTitleAddedCards.isVisible = false
//            binding.rvAddedCard.isVisible = false
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_update_card)
            onUpdateCard(card!!)
        } else {
//            binding.tvTitleAddedCards.isVisible = true
//            binding.rvAddedCard.isVisible = true
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_add_new_card)
            val contentLanguage = deck.cardContentDefaultLanguage
            val definitionLanguage = deck.cardDefinitionDefaultLanguage

            if (contentLanguage != null) {
//            isContentLanguageFieldShown(true)
//            binding.tieContentLanguage.setText(card.cardContentLanguage)
                binding.btContentLanguage.text = contentLanguage
            } else {
//            isContentLanguageFieldShown(false)
                binding.btContentLanguage.text = getString(R.string.text_content_language)
            }

            if (definitionLanguage != null) {
//            isDefinitionLanguageFieldShown(true)
//            binding.tieDefinitionLanguage.setText(card.cardDefinitionLanguage)
                binding.btDefinitionLanguage.text = definitionLanguage
            } else {
//            isDefinitionLanguageFieldShown(false)
                binding.btDefinitionLanguage.text = getString(R.string.text_definition_language)
            }

            binding.btAdd.apply {
                text = getString(R.string.bt_text_add)
                setOnClickListener {
                    onPositiveAction()
                }
            }
        }

        binding.tabAddNewUpdateCard.setNavigationOnClickListener {
            onCloseDialog()
        }

        binding.tabAddNewUpdateCard.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
//                    if (card != null && action == Constant.UPDATE) {
//                        sendCardsOnEdit(newCardViewModel.addedCards.value)
//                        dismiss()
//                    } else {
//                        sendCardsOnSave(newCardViewModel.addedCards.value)
//                        dismiss()
//                    }
//                    val action = if (card == null) Constant.ADD else Constant.UPDATE
//                    if (card)
                    onPositiveAction()
                    true
                }

                R.id.bt_upload_card -> {
                    showTriviaQuestionUploader()
                    true
                }

                else -> false
            }
        }

//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                newCardViewModel.addedCards.collect { cards ->
//                    displayAddedCard(cards, deck)
//                }
//            }
//        }

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
                        onTranslateText(
                            binding.tieContentMultiAnswerCard.text.toString(),
                            selectedField,
                            selectedFieldLy
                        )
                        true
                    }

                    R.id.save -> {
//                        val action = if (card == null) Constant.ADD else Constant.UPDATE
                        onPositiveAction()
//                        if (card != null && action == Constant.UPDATE) {
//
//                            sendCardsOnEdit(newCardViewModel.addedCards.value)
//
//                            dismiss()
//                        } else {
//                            sendCardsOnSave(newCardViewModel.addedCards.value)
//                            dismiss()
//                        }
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
                    getNewContentLanguage(),
                    hasFocus,
                    callback,
                    getString(R.string.til_card_content_hint)
                )
            }
        }

//        binding.tilContentMultiAnswerCard.hint =
//            getString(R.string.card_content_hint, deck.cardContentDefaultLanguage)

        definitionFields.forEach { definitionField ->
            definitionField.fieldEd.setOnFocusChangeListener { v, hasFocus ->
                onActiveTopAppBarMode(
                    definitionField.fieldLy,
                    v,
                    getNewDefinitionLanguage(),
                    hasFocus,
                    callback,
                    getString(R.string.til_card_definition_hint)
                )
            }
//            definitionField.fieldLy.hint = getString(
//                R.string.card_definition,
//                deck.cardDefinitionDefaultLanguage
//            )
            definitionField.btDeleteField?.setOnClickListener {
                deleteDefinitionField(definitionField.fieldEd)
            }
        }


        binding.btMoreDefinition.setOnClickListener {
            onAddMoreDefinition()
        }

//        binding.btShowContentLanguageField.setOnClickListener {
//            isContentLanguageFieldShown(!binding.tilContentLanguage.isVisible)
//        }

//        binding.btShowDefinitionLanguageField.setOnClickListener {
//            isDefinitionLanguageFieldShown(!binding.tilDefinitionLanguage.isVisible)
//        }

//        binding.tieContentLanguage.apply {
//            setAdapter(arrayAdapterSupportedLanguages)
//            setDropDownBackgroundDrawable(
//                ResourcesCompat.getDrawable(
//                    resources,
//                    R.drawable.filter_spinner_dropdown_background,
//                    requireActivity().theme
//                )
//            )
//        }

//        binding.tieDefinitionLanguage.apply {
//            setAdapter(arrayAdapterSupportedLanguages)
//            setDropDownBackgroundDrawable(
//                ResourcesCompat.getDrawable(
//                    resources,
//                    R.drawable.filter_spinner_dropdown_background,
//                    requireActivity().theme
//                )
//            )
//        }

    }

//    private fun isContentLanguageFieldShown(isShown: Boolean) {
//        binding.tilContentLanguage.isVisible = isShown
//        if (isShown) {
//            binding.btShowContentLanguageField.setIconResource(R.drawable.icon_expand_less)
//        } else {
//            binding.btShowContentLanguageField.setIconResource(R.drawable.icon_expand_more)
//        }
//    }

//    private fun isDefinitionLanguageFieldShown(isShown: Boolean) {
//        binding.tilDefinitionLanguage.isVisible = isShown
//        if (isShown) {
//            binding.btShowDefinitionLanguageField.setIconResource(R.drawable.icon_expand_less)
//        } else {
//            binding.btShowDefinitionLanguageField.setIconResource(R.drawable.icon_expand_more)
//        }
//    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showTriviaQuestionUploader() {
        val newDeckDialog = UploadOpenTriviaQuizDialog()
        newDeckDialog.show(childFragmentManager, "upload open trivia quiz dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { _, bundle ->
            val result =
                bundle.parcelable<OpenTriviaQuizModel>(UploadOpenTriviaQuizDialog.OPEN_TRIVIA_QUIZ_MODEL_BUNDLE_KEY)
            cardUploadingJob?.cancel()
            cardUploadingJob = lifecycleScope.launch {
                newCardViewModel.getOpenTriviaQuestions(
                    result?.number!!,
                    result.category,
                    result.difficulty,
                    result.type
                )
                newCardViewModel.openTriviaResponse.collect { response ->
                    when (response) {
                        is UiState.Error -> {
                            val message = when (response.errorMessage) {
                                "1" -> {
                                    getString(R.string.error_message_open_trivia_1)
                                }

                                "2" -> {
                                    getString(R.string.error_message_open_trivia_2)
                                }

                                "5" -> {
                                    getString(R.string.error_message_open_trivia_5)
                                }

                                else -> {
                                    getString(R.string.error_message_open_trivia_4)
                                }
                            }
                            binding.llAddCardProgressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }

                        is UiState.Loading -> {
                            binding.llAddCardProgressBar.isVisible = true
                        }

                        is UiState.Success -> {
                            val newCards = newCardViewModel.resultsToImmutableCards(
                                deck.deckId,
                                response.data.results
                            )
                            newCardViewModel.insertCards(newCards)
                            binding.llAddCardProgressBar.visibility = View.GONE
                            Toast.makeText(
                                appContext,
                                getString(R.string.message_cards_added, "${newCards.size}"),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

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
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
            "com.ssoaharison.recall.FileProvider",
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

    private fun detectTextFromAnImageWithMLKit(
        image: InputImage,
        language: String? = actualFieldLanguage
    ) {
        val recognizer = if (language.isNullOrBlank()) {
            getRecognizer(LanguageUtil().getLanguageByCode(Locale.getDefault().language))
        } else {
            getRecognizer(language)
        }
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

//    @SuppressLint("NotifyDataSetChanged")
//    private fun displayAddedCard(cardList: List<ImmutableCard?>, deck: ImmutableDeck) {
//        val pref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
//        val appTheme = pref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
//        rvAddedCardRecyclerViewAdapter = appContext?.let {
//            AddedCardRecyclerViewAdapter(
//                it,
//                cardList,
//                deck,
//                appTheme,
//                { cardWithPosition ->
//                    card = cardWithPosition.cardToEdit
//                    onUpdateCard(card!!, cardWithPosition.position)
//                },
//                { cardToRemove ->
//                    initCardAdditionPanel()
//                    newCardViewModel.removeCard(cardToRemove)
//                    rvAddedCardRecyclerViewAdapter.notifyDataSetChanged()
//                },
//            )
//        }!!
//        binding.rvAddedCard.apply {
//            adapter = rvAddedCardRecyclerViewAdapter
//            setHasFixedSize(true)
//            layoutManager = LinearLayoutManager(appContext)
//        }
//    }

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
//        isContentLanguageFieldShown(false)
//        isDefinitionLanguageFieldShown(false)
//        binding.tieDefinitionLanguage.apply {
//            text?.clear()
//            error = null
//        }
//        binding.tieContentLanguage.apply {
//            text?.clear()
//            error = null
//        }
    }

    private fun onCloseDialog() {
        if (!areThereAnOngoingCardCreation()) {
            sendCardsOnSave(newCardViewModel.getAddedCardSum())
            dismiss()
        } else {
            MaterialAlertDialogBuilder(
                requireActivity(),
                R.style.ThemeOverlay_App_MaterialAlertDialog
            )
                .setTitle(getString(R.string.title_unsaved_cards))
                .setMessage(getString(R.string.message_unsaved_cards))
                .setPositiveButton(getString(R.string.button_keep_add)) { dialog, _ ->
//                    sendCardsOnSave(newCardViewModel.addedCards.value)
//                    Toast.makeText(
//                        appContext,
//                        getString(R.string.message_card_registered),
//                        Toast.LENGTH_LONG
//                    ).show()
                    dialog?.dismiss()
//                    if (card == null) {
//                        sendCardsOnSave(generateCardOnAdd())
//                    } else {
//                        generateCardOnUpdate(card!!)
//                    }
//                    onPositiveAction(action)
//                    sendCardsOnSave()
//                    dismiss()
                }
                .setNegativeButton(getString(R.string.bt_description_exit)) { _, _ ->
//                    Toast.makeText(
//                        appContext,
//                        getString(R.string.message_card_not_registered),
//                        Toast.LENGTH_LONG
//                    ).show()
//                    newCardViewModel.clearAddedCards()
                    sendCardsOnSave(newCardViewModel.getAddedCardSum())
                    dismiss()
                }
//                .setNeutralButton("Keep adding") { dialog, _ ->
//                    dialog.dismiss()
//                }
                .show()
        }

    }

    private fun onUpdateCard(card: ImmutableCard) {
        binding.tieContentMultiAnswerCard.setText(card.cardContent?.content)
        binding.btAdd.apply {
            text = getString(R.string.bt_text_update)
            setOnClickListener {
//                if (indexCard == null) {
//
//                    if (onPositiveAction(Constant.UPDATE)) {
//                        sendCardsOnEdit(newCardViewModel.addedCards.value)
//                        dismiss()
//                    }
//                } else {
//                    onPositiveAction(Constant.UPDATE, indexCard)
//                }
//                newCardViewModel.updateCard(card)
                onPositiveAction()
//                Toast.makeText(appContext, getString(R.string.message_card_updated), Toast.LENGTH_LONG).show()
//                dismiss()
            }
        }

        definitionFields.forEachIndexed { index, fl ->
            if (index < card.cardDefinition?.size!!) {
                fl.fieldLy.visibility = View.VISIBLE
                fl.chip.visibility = View.VISIBLE
                fl.btDeleteField?.visibility = View.VISIBLE
                fl.fieldEd.setText(card.cardDefinition[index].definition)
                fl.chip.isChecked = isCorrect(card.cardDefinition[index].isCorrectDefinition)
                revealedDefinitionFields++
            } else {
                fl.fieldLy.visibility = View.GONE
                fl.chip.visibility = View.GONE
            }
        }

        val contentLanguage = getContentLanguage(card)

        val definitionLanguage = getDefinitionLanguage(card)

        if (contentLanguage != null) {
//            isContentLanguageFieldShown(true)
//            binding.tieContentLanguage.setText(card.cardContentLanguage)
            binding.btContentLanguage.text = contentLanguage
        } else {
//            isContentLanguageFieldShown(false)
            binding.btContentLanguage.text = getString(R.string.text_content_language)
        }

        if (definitionLanguage != null) {
//            isDefinitionLanguageFieldShown(true)
//            binding.tieDefinitionLanguage.setText(card.cardDefinitionLanguage)
            binding.btDefinitionLanguage.text = definitionLanguage
        } else {
//            isDefinitionLanguageFieldShown(false)
            binding.btDefinitionLanguage.text = getString(R.string.text_definition_language)
        }

    }

    private fun getDefinitionLanguage(card: ImmutableCard) = when {
        !card.cardDefinitionLanguage.isNullOrBlank() -> card.cardDefinitionLanguage
        !deck.cardDefinitionDefaultLanguage.isNullOrBlank() -> deck.cardDefinitionDefaultLanguage
        else -> null
    }

    private fun getContentLanguage(card: ImmutableCard) = when {
        !card.cardContentLanguage.isNullOrBlank() -> card.cardContentLanguage
        !deck.cardContentDefaultLanguage.isNullOrBlank() -> deck.cardContentDefaultLanguage
        else -> null
    }

    fun isCorrect(index: Int?) = index == 1

    @SuppressLint("NotifyDataSetChanged")
    private fun onPositiveAction(): Boolean {
        val newCard = if (action == Constant.ADD) {
            generateCardOnAdd() ?: return false
        } else {
            generateCardOnUpdate(card = card!!) ?: return false
        }

        if (action == Constant.UPDATE) {
//            newCardViewModel.updateCard(newCard)
//            Toast.makeText(appContext, getString(R.string.message_card_updated), Toast.LENGTH_LONG).show()
            sendCardsOnEdit(newCard)
            dismiss()
        } else {
            newCardViewModel.insertCard(newCard)
            Toast.makeText(appContext, getString(R.string.message_card_added), Toast.LENGTH_LONG).show()
        }
//        rvAddedCardRecyclerViewAdapter.notifyDataSetChanged()
        initCardAdditionPanel()
        return true
    }

    private fun areThereAnOngoingCardCreation(): Boolean {
        when {
            !binding.tieContentMultiAnswerCard.text.isNullOrBlank() -> return true
            !binding.tieContentMultiAnswerCard.text.isNullOrEmpty() -> return true
            else -> {
                definitionFields.forEach {
                    if (it.chip.isChecked) {
                        return true
                    }
                    if (!it.fieldEd.text.isNullOrBlank() || !it.fieldEd.text.isNullOrEmpty()) {
                        return true
                    }
                }
            }
        }
        return false
    }


    private fun generateCardOnUpdate(card: ImmutableCard): ImmutableCard? {

        val content = getContent(card.cardId, card.cardContent?.contentId!!, card.deckId)
            ?: return null
        val definitions =
            getDefinition(card.cardId, card.cardContent.contentId, card.deckId)
                ?: return null

        val updateCardDefinitions = mutableListOf<CardDefinition>()
        for (i in 0..card.cardDefinition?.size?.minus(1)!!) {
            val definition = try {
                definitions[i]
            } catch (e: IndexOutOfBoundsException) {
                newCardViewModel.createDefinition(
                    "",
                    false,
                    card.cardId,
                    card.cardContent.contentId,
                    card.deckId
                )
            }

            val updatedDefinition = CardDefinition(
                card.cardDefinition[i].definitionId,
                card.cardDefinition[i].cardId,
                card.cardDefinition[i].deckId,
                card.cardDefinition[i].contentId,
                definition.definition,
                definition.isCorrectDefinition
            )
            updateCardDefinitions.add(updatedDefinition)
        }
        if (definitions.size > card.cardDefinition.size) {
            for (j in (card.cardDefinition.size)..definitions.size.minus(1)) {
                updateCardDefinitions.add(definitions[j])
            }
        }

        val contentLanguage = getNewContentLanguage()
        val definitionLanguage = getNewDefinitionLanguage()

//        if (contentLanguage != null && contentLanguage !in supportedLanguages) {
//            binding.tilContentLanguage.error =
//                getString(R.string.error_message_deck_language_not_supported)
//            return null
//        }

//        if (definitionLanguage != null && definitionLanguage !in supportedLanguages) {
//            binding.tilDefinitionLanguage.error =
//                getString(R.string.error_message_deck_language_not_supported)
//            return null
//        }

        return ImmutableCard(
            card.cardId,
            content,
            updateCardDefinitions,
            card.deckId,
            card.isFavorite,
            card.revisionTime,
            card.missedTime,
            card.creationDate,
            card.lastRevisionDate,
            card.cardStatus,
            card.nextMissMemorisationDate,
            card.nextRevisionDate,
            getCardType(definitions),
            contentLanguage,
            definitionLanguage,
        )
    }

    private fun generateCardOnAdd(): ImmutableCard? {
        val cardId = now()
        val contentId = now()
        val newCardContent = getContent(cardId, contentId, deck.deckId)
        val newCardDefinition = getDefinition(cardId, contentId, deck.deckId)

        val contentLanguage = getNewContentLanguage()
        val definitionLanguage = getNewDefinitionLanguage()

//        if (contentLanguage != null && contentLanguage !in supportedLanguages) {
//            binding.tilContentLanguage.error =
//                getString(R.string.error_message_deck_language_not_supported)
//            return null
//        }
//
//        if (definitionLanguage != null && definitionLanguage !in supportedLanguages) {
//            binding.tilDefinitionLanguage.error =
//                getString(R.string.error_message_deck_language_not_supported)
//            return null
//        }

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
            contentLanguage,
            definitionLanguage
        )
    }

    private fun getNewContentLanguage(): String? {
        val addedContentLanguage = binding.btContentLanguage.text
        if (addedContentLanguage.toString() !in supportedLanguages) {
            val defaultContentLanguage = deck.cardContentDefaultLanguage
            return if (defaultContentLanguage.isNullOrBlank()) {
                null
            } else {
                defaultContentLanguage
            }
        } else {
            return addedContentLanguage.toString()
        }
    }

    private fun getNewDefinitionLanguage(): String? {
        val addedDefinitionLanguage = binding.btDefinitionLanguage.text
        if (addedDefinitionLanguage.toString() !in supportedLanguages) {
            val defaultDefinitionLanguage = deck.cardDefinitionDefaultLanguage
            return if (defaultDefinitionLanguage.isNullOrBlank()) {
                null
            } else {
                defaultDefinitionLanguage
            }
        } else {
            return addedDefinitionLanguage.toString()
        }

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

    private fun getContent(cardId: String, contentId: String, deckId: String): CardContent? {
        val cardContentText = binding.tieContentMultiAnswerCard.text.toString()
        return if (cardContentText.isNotEmpty() && cardContentText.isNotBlank()) {
            newCardViewModel.generateCardContent(
                contentId = contentId,
                cardId = cardId,
                deckId = deckId,
                text = cardContentText
            )
//            CardContent(
//                contentId,
//                cardId,
//                deckId,
//                cardContentText
//            )
        } else {
            binding.tilContentMultiAnswerCard.error = getString(R.string.til_error_card_content)
            null
        }
    }

    private fun isDefinitionError(): Boolean {
        var isText = false
        var isTrueAnswer = false
        definitionFields.forEach {
            if (it.fieldEd.text.toString().isNotEmpty() && it.fieldEd.text.toString()
                    .isNotBlank()
            ) {
                isText = true
            }
            if (it.chip.isChecked && it.fieldEd.text.toString()
                    .isNotEmpty() && it.fieldEd.text.toString().isNotBlank()
            ) {
                if (isText) {
                    return false
                }
            }
            if (it.chip.isChecked) {
                isTrueAnswer = true
            }
        }
        if (!isTrueAnswer) {
            binding.tilDefinition1MultiAnswerCard.error =
                getString(R.string.cp_error_correct_definition)
        }
        if (!isText) {
            binding.tilDefinition1MultiAnswerCard.error =
                getString(R.string.til_error_card_definition)
        }
        return true
    }

    private fun getDefinition(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition>? {
        if (isDefinitionError()) {
            return null
        } else {
            definitionList.clear()
            definitionFields.forEach {
                if (it.fieldEd.text.toString().isNotEmpty() && it.fieldEd.text.toString()
                        .isNotBlank()
                ) {
                    definitionList.add(
                        newCardViewModel.createDefinition(
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
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    private fun onTranslateText(
        text: String,
        actualField: EditText?,
        ly: TextInputLayout?
    ) {
        ly?.error = null
        animProgressBar(ly)
        actualField?.setText(getString(R.string.message_translation_in_progress))
        val definitionLanguage = getNewDefinitionLanguage()
        val contentLanguage = getNewContentLanguage()

        LanguageUtil().startTranslation(
            text = text,
            definitionLanguage = definitionLanguage,
            contentLanguage = contentLanguage,
            onStartTranslation = { translationLanguages ->
                translate(
                    fl = translationLanguages.fl,
                    tl = translationLanguages.tl,
                    actualField = actualField,
                    ly = ly,
                    text = text,
                )
            },
            onLanguageDetectionLanguageNotSupported = {
                showSnackBar(R.string.error_message_language_not_supported)
            },
        )
    }

    private fun showSnackBar(
        @StringRes messageRes: Int
    ) {
        Snackbar.make(
            binding.clAddCardRoot,
            getString(messageRes),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun translate(
        fl: String?,
        tl: String?,
        actualField: EditText?,
        ly: TextInputLayout?,
        text: String
    ) {
        val languageUtil = LanguageUtil()
        languageUtil.prepareTranslation(
            context = requireContext(),
            fl = fl,
            tl = tl,
            onDownloadingLanguageMode = {
                actualField?.setText(getString(R.string.message_translation_downloading_language_model))
            },
            onMissingCardDefinitionLanguage = {
                setEditTextEndIconOnClick(ly)
                ly?.error =
                    appContext?.getString(R.string.error_message_no_card_definition_language)
                showSnackBar(R.string.error_message_no_card_definition_language)
            },
            onModelDownloadingFailure = {
                ly?.error = getString(R.string.error_translation_unknown)
            },
            onSuccess = { translatorModel ->
                languageUtil.translate(
                    appTranslator = translatorModel.appTranslator,
                    conditions = translatorModel.condition,
                    text = text,
                    onSuccess = { translation ->
                        actualField?.setText(translation)
                        setEditTextEndIconOnClick(ly)
                    },
                    onTranslationFailure = { exception ->
                        ly?.error = exception.toString()
                        setEditTextEndIconOnClick(ly)
                    },
                    onModelDownloadingFailure = {
                        setEditTextEndIconOnClick(ly)
                        ly?.error =
                            getString(R.string.error_translation_language_model_not_downloaded)
                    },
                )
            },
            onNoInternet = {
                setEditTextEndIconOnClick(ly)
                ly?.error = getString(R.string.error_translation_no_internet)
            },
            onInternetViaCellular = { translatorModel ->
                MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_App_MaterialAlertDialog
                )
                    .setTitle(getString(R.string.title_no_wifi))
                    .setMessage(getString(R.string.message_no_wifi))
                    .setNegativeButton(getString(R.string.option2_no_wifi)) { dialog, _ ->
                        setEditTextEndIconOnClick(ly)
                        actualField?.text?.clear()
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.option1_no_wifi)) { dialog, _ ->
                        languageUtil.translate(
                            appTranslator = translatorModel.appTranslator,
                            conditions = translatorModel.condition,
                            text = text,
                            onSuccess = { translation ->
                                actualField?.setText(translation)
                                setEditTextEndIconOnClick(ly)
                            },
                            onTranslationFailure = { exception ->
                                ly?.error = exception.toString()
                                setEditTextEndIconOnClick(ly)
                            },
                            onModelDownloadingFailure = {
                                setEditTextEndIconOnClick(ly)
                                ly?.error =
                                    getString(R.string.error_translation_language_model_not_downloaded)
                            },
                        )
                        dialog.dismiss()
                    }
                    .show()
            },
        )
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
        cardCount: Int
    ) {
        parentFragmentManager.setFragmentResult(
            REQUEST_CODE_CARD,
            bundleOf(SAVE_CARDS_BUNDLE_KEY to cardCount)
        )
//        newCardViewModel.removeAllCards()
    }

    private fun sendCardsOnEdit(
        card: ImmutableCard
    ) {
        parentFragmentManager.setFragmentResult(
            REQUEST_CODE_CARD,
            bundleOf(EDIT_CARD_BUNDLE_KEY to card)
        )
//        newCardViewModel.removeAllCards()
    }

    private fun onAddMoreDefinition() {
        if (revealedDefinitionFields < definitionFields.size) {
            definitionFields[revealedDefinitionFields].apply {
                fieldLy.isVisible = true
                fieldEd.isVisible = true
                chip.isVisible = true
                btDeleteField?.isVisible = true
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
                clearField(definitionFields.last { it.fieldLy.isVisible })
                break
            }
            index++
        }
    }

    private fun clearField(field: DefinitionFieldModel) {
        field.apply {
            btDeleteField?.visibility = View.GONE
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