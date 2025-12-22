package com.ssoaharison.recall.card

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.ssoaharison.recall.R
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
import com.ssoaharison.recall.backend.entities.Card
import com.ssoaharison.recall.backend.entities.relations.CardContentWithDefinitions
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCard
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.databinding.LyAddCardContentFieldBinding
import com.ssoaharison.recall.deck.DeckFragment.Companion.REQUEST_CODE
import com.ssoaharison.recall.deck.OpenTriviaQuizModel
import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.PhotoModel
import com.ssoaharison.recall.helper.playback.AndroidAudioPlayer
import com.ssoaharison.recall.helper.record.AndroidAudioRecorder
import com.ssoaharison.recall.util.AttachRef.ATTACH_AUDIO_RECORD
import com.ssoaharison.recall.util.AttachRef.ATTACH_IMAGE_FROM_CAMERA
import com.ssoaharison.recall.util.AttachRef.ATTACH_IMAGE_FROM_GALERI
import com.ssoaharison.recall.util.ScanRef.AUDIO_TO_TEXT
import com.ssoaharison.recall.util.ScanRef.IMAGE_FROM_CAMERA_TO_TEXT
import com.ssoaharison.recall.util.ScanRef.IMAGE_FROM_GALERI_TO_TEXT
import com.ssoaharison.recall.util.UiState
import com.ssoaharison.recall.util.parcelable
import com.ssoaharison.recall.util.textToImmutableCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import java.util.UUID

class NewCardDialog(
    private var card: ExternalCardWithContentAndDefinitions?,
    private val deck: ExternalDeck,
    private val action: String
) : AppCompatDialogFragment() {

    private var _binding: AddCardLayoutDialogBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    var imm: InputMethodManager? = null
    private var selectedField: FieldModel? = null
    private var actualFieldLanguage: String? = null
    private var cardUploadingJob: Job? = null
    private val supportedLanguages = LanguageUtil().getSupportedLang()
    private lateinit var importCardsFromDeviceModel: CardImportFromDeviceModel
    private lateinit var attachBottomSheetDialog: AttachBottomSheetDialog
    private lateinit var scanBottomSheetDialog: ScanBottomSheetDialog

    private val recorder by lazy {
        AndroidAudioRecorder(requireContext())
    }

    private val player by lazy {
        AndroidAudioPlayer(requireContext())
    }

    private val newCardViewModel by lazy {
        val openTriviaRepository =
            (requireActivity().application as FlashCardApplication).openTriviaRepository
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(
            this,
            NewCardDialogViewModelFactory(openTriviaRepository, repository)
        )[NewCardDialogViewModel::class.java]
    }

    private var actionMode: ActionMode? = null
    private var uri: Uri? = null
    private lateinit var definitionFields: MutableList<FieldModel>

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
                    selectedField?.ly?.tieText?.setText(result[0])
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

    private var openFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val cards = textFromUriToImmutableCards(uri, ":")
                newCardViewModel.insertCards(cards)
                Toast.makeText(
                    appContext,
                    getString(R.string.message_cards_added, "${cards.size}"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private var fileImageName: String? = null
    private var isPhotoSaved: Boolean = false

    val attachPhotoFromCamera =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            it?.let {
                fileImageName = "${UUID.randomUUID()}.jpg"
                isPhotoSaved = saveImageToInternalStorage(fileImageName!!, it)
                if (isPhotoSaved) {
                    val newPhoto = PhotoModel(fileImageName!!, it)
                    newCardViewModel.getActiveFieldIndex()?.let { activeFieldIndex ->
                        if (activeFieldIndex == -1) {
                            newCardViewModel.updateContentField(
                                updatedContentField = newCardViewModel.contentField.value.copy(
                                    contentImage = newPhoto
                                )
                            )
                            onSetContentFieldPhoto(newPhoto.bmp)
                        } else {
                            newCardViewModel.updateDefinitionImage(
                                id = newCardViewModel.getDefinitionFieldAt(activeFieldIndex).definitionId,
                                image = newPhoto
                            )
                            onSetDefinitionFieldPhoto(
                                actualField = definitionFields[activeFieldIndex],
                                imageBitmap = newPhoto.bmp
                            )
                        }
                    }
                }
            }
        }

    private val onAttachePhotoFromCameraRequestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            attachPhotoFromCamera.launch()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_message_permission_not_granted_camera),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private var onAttachPhotoFromGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val btm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            requireContext().contentResolver,
                            uri
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }
                val imageName = "${UUID.randomUUID()}.jpg"
                val isImageSaved = saveImageToInternalStorage(imageName, btm)
                if (isImageSaved) {
                    val newPhoto = PhotoModel(imageName, btm)
                    newCardViewModel.getActiveFieldIndex()?.let { activeFieldIndex ->
                        if (activeFieldIndex == -1) {
                            newCardViewModel.updateContentField(
                                updatedContentField = newCardViewModel.contentField.value.copy(
                                    contentImage = newPhoto
                                )
                            )
                            onSetContentFieldPhoto(newPhoto.bmp)
                        } else {
                            newCardViewModel.updateDefinitionImage(
                                id = newCardViewModel.getDefinitionFieldAt(activeFieldIndex).definitionId,
                                image = newPhoto
                            )
                            onSetDefinitionFieldPhoto(
                                actualField = definitionFields[activeFieldIndex],
                                imageBitmap = newPhoto.bmp
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Could not get image", Toast.LENGTH_LONG).show()
            }
        }


    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 3455
        const val TAG = "NewCardDialog"
        const val SAVE_CARDS_BUNDLE_KEY = "1"
        const val EDIT_CARD_BUNDLE_KEY = "2"
        const val REQUEST_CODE_CARD = "0"
        const val REQUEST_CODE_CARD_IMPORT_SOURCE = "3"
        const val REQUEST_CODE_IMPORT_CARD_FROM_DEVICE_SOURCE = "4"
        const val REQUEST_CODE_AUDIO_RECORDER = "5"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.QuizeoFullscreenDialogTheme)
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

//        WindowCompat.enableEdgeToEdge(dialog?.window!!)
//        ViewCompat.setOnApplyWindowInsetsListener(binding.ablAddNewCard) { v, windowInserts ->
//            val insets = windowInserts.getInsets(WindowInsetsCompat.Type.statusBars())
//            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                topMargin = insets.top
//            }
//            WindowInsetsCompat.CONSUMED
//        }

        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initFields()
        attachBottomSheetDialog = AttachBottomSheetDialog()
        scanBottomSheetDialog = ScanBottomSheetDialog()

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
                setOnItemClickListener { _, _, position, _ ->
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
                setOnItemClickListener { _, _, position, _ ->
                    (button as MaterialButton).text = supportedLanguages[position]
                    dismiss()
                }
                show()
            }
        }

        binding.tabAddNewUpdateCard.setNavigationOnClickListener {
            onCloseDialog()
        }

        binding.tabAddNewUpdateCard.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    onPositiveAction()
                    true
                }

                R.id.bt_import_card -> {
                    showCardImportSourceDialog()
                    true
                }

                else -> false
            }
        }

        binding.btMoreDefinition.setOnClickListener {
//            binding.lyContent.tieContentText.clearFocus()
            onAddMoreDefinition()
        }

        binding.btSave.setOnClickListener {
            onPositiveAction()
        }
        binding.btTranslate.setOnClickListener {
//            onTranslateText(
//                binding.lyContent.tieContentText.text.toString(),
//                selectedField?.ly?.tieText,
//                selectedField?.ly?.tilText
//            )
        }
        binding.btAddMedia.setOnClickListener {
            onAttach()
        }
        binding.btScan.setOnClickListener {
            onScan()
        }

    }

    private fun initFields() {
        definitionFields = mutableListOf(
            FieldModel(
                binding.llDefinition1Container,
                binding.lyDefinition1,
            ),
            FieldModel(
                binding.llDefinition2Container,
                binding.lyDefinition2,
            ),
            FieldModel(
                binding.llDefinition3Container,
                binding.lyDefinition3,
            ),
            FieldModel(
                binding.llDefinition4Container,
                binding.lyDefinition4,
            ),
            FieldModel(
                binding.llDefinition5Container,
                binding.lyDefinition5,
            ),
            FieldModel(
                binding.llDefinition6Container,
                binding.lyDefinition6,
            ),
            FieldModel(
                binding.llDefinition7Container,
                binding.lyDefinition7,
            ),
            FieldModel(
                binding.llDefinition8Container,
                binding.lyDefinition8,
            ),
            FieldModel(
                binding.llDefinition9Container,
                binding.lyDefinition9,
            ),
            FieldModel(
                binding.llDefinition10Container,
                binding.lyDefinition10,
            ),
        )
//        contentField = FieldModel(binding.clContainerContent, binding.lyContent)
//        binding.lyDefinition1.btDeleteField.visibility = View.GONE
        binding.lyContent.tieContentText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                onFieldFocused(
                    binding.clContainerContent,
                    binding.lyContent,
                    v,
                    getNewContentLanguage(),
                    hasFocus,
                )
                newCardViewModel.focusToContent()
            } else {
                v.clearFocus()
            }
        }
        binding.lyContent.tieContentText.addTextChangedListener { text ->
            newCardViewModel.updateContentField(newCardViewModel.contentField.value.copy(contentText = text.toString()))
        }
        binding.lyContent.btContentDeleteImage.setOnClickListener {
            newCardViewModel.contentField.value.contentImage?.let { image ->
                if (deleteImageFromInternalStorage(image.name)) {
                    newCardViewModel.deleteContentImageField()
                    onRemoveContentFieldPhoto()
                }
            }
        }
        binding.lyContent.lyContentAudio.btPlay.setOnClickListener {
            playPauseContendAudio()
        }


        binding.lyContent.lyContentAudio.btDelete.setOnClickListener {
            newCardViewModel.contentField.value.contentAudio?.let { audio ->
                if (deleteAudioFromInternalStorage(audio)) {
                    newCardViewModel.deleteContentAudioField()
                    onRemoveContentFieldAudio()
                }
            }
        }

        if (card != null && action == Constant.UPDATE) {
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_update_card)
            onUpdateCard(card!!)
        } else {
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_add_new_card)
            onAddCard()
            setCardLanguages()
        }
//        definitionFields.forEach { definitionField ->
//            definitionField.ly.tieText.setOnFocusChangeListener { v, hasFocus ->
//                onFieldFocused(
//                    definitionField.container,
//                    definitionField.ly,
//                    v,
//                    getNewDefinitionLanguage(),
//                    hasFocus,
//                )
//            }
//            definitionField.ly.btDeleteField.setOnClickListener {
//                deleteDefinitionField(definitionField.ly.tieText)
//            }
//            definitionField.ly.btIsTrue.setOnClickListener {
//                onClickChip(!chipState(definitionField.ly.btIsTrue), definitionField.ly.btIsTrue)
//            }
//            definitionField.ly.btDeleteAudio.setOnClickListener {
//                onDeleteAudio(definitionField.ly)
//            }
//            definitionField.ly.btDeleteImage.setOnClickListener {
//                onDeleteImage(definitionField)
//            }
//        }

    }

    private fun playPauseContendAudio() {
        when {
            player.hasPlayed() && !player.isPlaying() -> {
                binding.lyContent.lyContentAudio.btPlay.setIconResource(R.drawable.icon_pause)
                player.play()
                lifecycleScope.launch {
    //                        binding.lyContent.lyContentAudio.slider.max = player.getDuration()
                    while (player.isPlaying()) {
                        binding.lyContent.lyContentAudio.slider.progress =
                            player.getCurrentPosition()
                        delay(1000L)
                    }
                }
            }

            player.hasPlayed() && player.isPlaying() -> {
                binding.lyContent.lyContentAudio.btPlay.setIconResource(R.drawable.icon_play)
                player.pause()
            }

            !player.hasPlayed() && !player.isPlaying() -> {
                newCardViewModel.contentField.value.contentAudio?.let { audio ->
                    binding.lyContent.lyContentAudio.btPlay.setIconResource(R.drawable.icon_pause)
                    player.playFile(audio.file)
                    lifecycleScope.launch {
                        binding.lyContent.lyContentAudio.slider.max = player.getDuration()
                        while (player.isPlaying()) {
                            binding.lyContent.lyContentAudio.slider.progress =
                                player.getCurrentPosition()
                            delay(1000L)
                        }
                    }
                }
                player.onCompletion {
                    binding.lyContent.lyContentAudio.btPlay.setIconResource(R.drawable.icon_play)
                }
            }
        }
    }

    private fun onAttach() {
        attachBottomSheetDialog.show(childFragmentManager, "Attach Dialog")
        childFragmentManager.setFragmentResultListener(
            AttachBottomSheetDialog.ATTACH_REQUEST_CODE,
            this
        ) { _, bundle ->
            val attach = bundle.getString(AttachBottomSheetDialog.ATTACH_BUNDLE_KEY)
            attach?.let {
                when (it) {
                    ATTACH_IMAGE_FROM_CAMERA -> {
                        if (newCardViewModel.getActiveFieldIndex() != null) {
                            onTakePhoto()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.error_message_no_field_selected),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    ATTACH_IMAGE_FROM_GALERI -> {
                        if (newCardViewModel.getActiveFieldIndex() != null) {
                            onPickPhoto()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.error_message_no_field_selected),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    ATTACH_AUDIO_RECORD -> {
                        onRecordAudio(newCardViewModel.getActiveFieldIndex())

//                        if (selectedField != null) {
//                            onRecordAudio(newCardViewModel.getActiveFieldIndex())
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                getString(R.string.error_message_no_field_selected),
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
                    }
                }
            }
        }
    }

    private fun onScan() {
        scanBottomSheetDialog.show(childFragmentManager, "Scan Dialog")
        childFragmentManager.setFragmentResultListener(
            ScanBottomSheetDialog.SCAN_REQUEST_CODE,
            this
        ) { _, bundle ->
            val attach = bundle.getString(ScanBottomSheetDialog.SCAN_BUNDLE_KEY)
            attach?.let {
                when (it) {
                    IMAGE_FROM_CAMERA_TO_TEXT -> {
                        checkCameraPermission({
                            openCamera()
                        }) {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }

                    IMAGE_FROM_GALERI_TO_TEXT -> {
                        onSelectImageFromGallery()
                    }

                    AUDIO_TO_TEXT -> {
                        listen(actualFieldLanguage)
                    }
                }
            }
        }
    }

    private fun showCardImportSourceDialog() {
        val newImportCardsSourceDialog = ImportCardsSourceDialog()
        newImportCardsSourceDialog.show(childFragmentManager, "Import card source dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CARD_IMPORT_SOURCE,
            this
        ) { _, bundle ->
            val result = bundle.getString(ImportCardsSourceDialog.IMPORT_CARDS_SOURCE_BUNDLE_KEY)
            when (result) {
                ImportCardsSourceDialog.IMPORT_FROM_DEVICE -> {
                    showCardImportFromDeviceDialog()
                }

                ImportCardsSourceDialog.IMPORT_FROM_OTHERS -> {
                    showTriviaQuestionUploader()
                }
            }
        }
    }

    private fun showCardImportFromDeviceDialog() {
        val cardImportFromDeviceDialog = ImportCardsFromDeviceDialog()
        cardImportFromDeviceDialog.show(childFragmentManager, "Import card from device dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_IMPORT_CARD_FROM_DEVICE_SOURCE,
            this
        ) { _, bundle ->
            val result =
                bundle.parcelable<CardImportFromDeviceModel>(ImportCardsFromDeviceDialog.EXPORT_CARD_FROM_DEVICE_BUNDLE_KEY)
            if (result != null) {
                importCardsFromDeviceModel = result
                openFile.launch(arrayOf("text/*"))
            } else {
                Toast.makeText(
                    appContext,
                    getString(R.string.error_message_card_import_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

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

    private fun onFieldFocused(
        container: ConstraintLayout,
        ly: LyAddCardContentFieldBinding,
        v: View?,
        language: String?,
        hasFocus: Boolean,
    ) {
        if (hasFocus) {
            actualFieldLanguage = language
            ly.llContentField.setBackgroundResource(R.drawable.bg_add_card_field_focused)
            //TODO: To be improved. Scroll to focused view.
            v?.postDelayed({
                val y = ly.llContentField.bottom.plus(binding.dockedToolbar.height)
                binding.nestedScrollView.smoothScrollTo(0, maxOf(y, 0))
            }, 200)
        } else {
            ly.llContentField.setBackgroundResource(R.drawable.bg_add_card_field)
        }
    }

//    private fun showImageSelectedDialog() {
//        val builder = MaterialAlertDialogBuilder(
//            requireActivity(),
//            R.style.ThemeOverlay_App_MaterialAlertDialog
//        )
//        builder.apply {
//            setTitle("Select Image")
//            setMessage("Please select an option")
//            setPositiveButton(
//                "Camera"
//            ) { dialog, _ ->
//                checkCameraPermission()
//                dialog?.dismiss()
//            }
//
//            setNeutralButton(
//                "Cancel"
//            ) { dialog, _ -> dialog?.dismiss() }
//
//            setNegativeButton(
//                "Gallery"
//            ) { dialog, _ ->
//                onSelectImageFromGallery()
//                dialog?.dismiss()
//            }
//        }
//
//        val dialog = builder.create()
//        dialog.show()
//    }

//    private fun checkCameraPermission() {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
//        } else {
//            openCamera()
//        }
//    }

    private fun checkCameraPermission(
        onCameraPermissionGranted: () -> Unit,
        onCameraPermissionNotGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onCameraPermissionNotGranted()
//            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
//            openCamera()
            onCameraPermissionGranted()
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
                    selectedField?.ly?.tieText?.setText(visionText.text)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun initCardAdditionPanel() {
        // TODO: Clear Audio
//        binding.lyContent.tieText.text?.clear()
//        contentField.imageName = null
//        contentField.audioName = null
//        contentField.ly.clContainerImage.visibility = View.GONE
//        contentField.ly.imgPhoto.setImageBitmap(null)
//        binding.lyContent.tieText.error = null
//        definitionFields.forEach {
//            it.container.visibility = View.GONE
//            it.ly.tieText.text?.clear()
//            it.imageName = null
//            it.audioName = null
//            it.ly.clContainerImage.visibility = View.GONE
//            it.ly.imgPhoto.setImageBitmap(null)
//            it.ly.tilText.error = null
//            unCheckChip(it.ly.btIsTrue)
//        }
//        definitionFields.first().container.visibility = View.VISIBLE
//        actionMode?.finish()
//        binding.lyContent.tieContentText.text?.clear()
//        binding.lyContent.imgContentPhoto.setImageBitmap(null)
//        binding.lyContent.clContentContainerImage.visibility = View.GONE
//        newCardViewModel.clearContentField()
//        newCardViewModel.clearDefinitionFields()
        newCardViewModel.clearFields()
    }

    private fun chipState(chip: TextView): Boolean {
        return chip.text == getString(R.string.cp_true_text)
    }

    private fun checkChip(chip: TextView) {
        chip.apply {
            background.setTint(ContextCompat.getColor(context, R.color.green200))
            setTextColor(ContextCompat.getColor(context, R.color.green500))
            text = getString(R.string.cp_true_text)
        }
    }

    private fun unCheckChip(chip: TextView) {
        chip.apply {
            background.setTint(ContextCompat.getColor(context, R.color.red200))
            setTextColor(ContextCompat.getColor(context, R.color.red500))
            text = getString(R.string.cp_false_text)
        }
    }

    private fun onClickChip(state: Boolean, chip: TextView) {
        if (state) {
            checkChip(chip)
        } else {
            unCheckChip(chip)
        }
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
                    dialog?.dismiss()
                }
                .setNegativeButton(getString(R.string.bt_description_exit)) { _, _ ->
                    newCardViewModel.definitionFields.value.forEach { fieldModel ->
                        fieldModel.definitionImage?.let { image ->
                            deleteImageFromInternalStorage(image.name)
                        }
                        fieldModel.definitionAudio?.let { audio ->
                            deleteAudioFromInternalStorage(audio)
                        }
                    }
                    sendCardsOnSave(newCardViewModel.getAddedCardSum())
                    dismiss()
                }
                .show()
        }

    }

    private fun onAddCard() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newCardViewModel.definitionFields.collect { fields ->
                    displayDefinitionFields(fields)
                }
            }
        }
        newCardViewModel.initAddCardFields(card = null)
    }

    private fun onUpdateCard(card: ExternalCardWithContentAndDefinitions) {
//        newCardViewModel.initContentField(card.contentWithDefinitions.content)
//
//        card.contentWithDefinitions.content.contentText?.let {
//            binding.lyContent.tieContentText.setText(it)
//        }
//        card.contentWithDefinitions.content.contentImage?.let {
//            binding.lyContent.imgContentPhoto.setImageBitmap(it.bmp)
//            binding.lyContent.clContentContainerImage.visibility = View.VISIBLE
//        }
//        card.contentWithDefinitions.content.contentAudio?.let {
//            // TODO: Include audio content
//        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newCardViewModel.definitionFields.collect { fields ->
                    displayDefinitionFields(fields)
                }
            }
        }
        newCardViewModel.initAddCardFields(card = card)


//        definitionFields.forEachIndexed { index, fl ->
//            if (index < card.contentWithDefinitions.definitions.size) {
//                fl.container.visibility = View.VISIBLE
//                card.contentWithDefinitions.definitions[index].definitionText?.let {
//                    fl.ly.tieText.setText(it)
//                }
//                card.contentWithDefinitions.definitions[index].definitionImage?.let {
//                    fl.ly.imgPhoto.setImageBitmap(it.bmp)
//                    fl.ly.clContainerImage.visibility = View.VISIBLE
//                    fl.imageName = it.name
//                }
//                card.contentWithDefinitions.definitions[index].definitionAudio?.let {
//                    //TODO: Include audio definition
//                }
//                onClickChip(
//                    state = isCorrect(card.contentWithDefinitions.definitions[index].isCorrectDefinition),
//                    chip = fl.ly.btIsTrue
//                )
//                revealedDefinitionFields++
//            } else {
//                fl.container.visibility = View.GONE
//            }
//        }

        setCardLanguages(card.card)

    }

    private fun displayDefinitionFields(fields: List<DefinitionFieldModel>) {
        definitionFields.forEachIndexed { index, fieldView ->
            if (index < fields.size) {
                fieldView.container.visibility = View.VISIBLE
                val actualDefinitionFieldModel = fields[index]
                actualDefinitionFieldModel.definitionText?.let { text ->
                    fieldView.ly.tieText.setText(text)
                    fieldView.ly.tieText.setSelection(text.length)
                }
                if (actualDefinitionFieldModel.definitionImage != null) {
                    fieldView.ly.clContainerImage.visibility = View.VISIBLE
                    fieldView.ly.imgPhoto.setImageBitmap(actualDefinitionFieldModel.definitionImage!!.bmp)
                } else {
                    fieldView.ly.clContainerImage.visibility = View.GONE
                    fieldView.ly.imgPhoto.setImageBitmap(null)
                }
                if (actualDefinitionFieldModel.definitionAudio != null) {
                    fieldView.ly.llContainerAudio.visibility = View.VISIBLE
                } else {
                    fieldView.ly.llContainerAudio.visibility = View.GONE
                }

                if (actualDefinitionFieldModel.isCorrectDefinition) {
                    // TODO: On correct definition
                } else {
                    // TODO: On wrong definition
                }
                fieldView.ly.tieText.addTextChangedListener { text ->
                    newCardViewModel.updateDefinitionText(
                        id = actualDefinitionFieldModel.definitionId,
                        text = text.toString()
                    )
                }
                fieldView.ly.tieText.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        newCardViewModel.changeFieldFocus(index)
                    }
                }

                onClickChip(
                    state = actualDefinitionFieldModel.isCorrectDefinition,
                    chip = fieldView.ly.btIsTrue
                )

                fieldView.ly.btIsTrue.setOnClickListener {
                    onClickChip(
                        state = !actualDefinitionFieldModel.isCorrectDefinition,
                        chip = fieldView.ly.btIsTrue
                    )
                    newCardViewModel.updateDefinitionStatus(
                        id = actualDefinitionFieldModel.definitionId,
                        status = !newCardViewModel.getDefinitionStatusById(
                            actualDefinitionFieldModel.definitionId
                        )!!
                    )
                }

                if (index == 0) {
                    fieldView.ly.btDeleteField.visibility = View.GONE
                } else {
                    fieldView.ly.btDeleteField.visibility = View.VISIBLE
                    fieldView.ly.btDeleteField.setOnClickListener {
                        newCardViewModel.getDefinitionFieldAt(index).definitionImage?.name?.let { imageName ->
                            deleteImageFromInternalStorage(imageName)
                        }
                        newCardViewModel.getDefinitionFieldAt(index).definitionAudio?.let { audioModel ->
                            deleteAudioFromInternalStorage(audioModel)
                        }
                        newCardViewModel.deleteDefinitionField(actualDefinitionFieldModel.definitionId)

                    }
                }

                fieldView.ly.btDeleteImage.setOnClickListener {
                    newCardViewModel.getDefinitionFieldAt(index).definitionImage?.name?.let { imageName ->
                        if (deleteImageFromInternalStorage(imageName)) {
                            newCardViewModel.deleteDefinitionImageField(actualDefinitionFieldModel.definitionId)
                            onRemoveDefinitionFieldPhoto(fieldView)
                        }
                    }
                }

                fieldView.ly.lyContentAudio.btPlay.setOnClickListener {
                    newCardViewModel.getDefinitionFieldAt(index).definitionAudio?.let { audioModel ->
                        playPauseDefinitionAudio(fieldView, audioModel)
                    }
                }

                fieldView.ly.lyContentAudio.btDelete.setOnClickListener {
                    newCardViewModel.getDefinitionFieldAt(index).definitionAudio?.let { audioModel ->
                        if (deleteAudioFromInternalStorage(audioModel)) {
                            newCardViewModel.deleteDefinitionAudioField(actualDefinitionFieldModel.definitionId)
                            onRemoveDefinitionFieldAudio(definitionFields[index])
                        }
                    }
                }
//                newCardViewModel.getDefinitionFieldAt(index).definitionAudio?.let { audioModel ->
//                    fieldView.ly.lyContentAudio.btPlay.setOnClickListener {
//                        player.playFile(audioModel.file)
//                    }
//                }

                if (actualDefinitionFieldModel.hasFocus) {
                    fieldView.ly.tieText.requestFocus()
                    fieldView.ly.tieText.post {
                        imm?.showSoftInput(fieldView.ly.tieText, InputMethodManager.SHOW_IMPLICIT)
                    }
                } else {
                    fieldView.ly.tieText.clearFocus()
                    imm?.hideSoftInputFromWindow(fieldView.ly.tieText.windowToken, 0)
                }

            } else {
                fieldView.container.visibility = View.GONE
            }
        }
    }

    private fun playPauseDefinitionAudio(
        fieldView: FieldModel,
        audioModel: AudioModel
    ) {
        when {
            player.hasPlayed() && !player.isPlaying() -> {
                fieldView.ly.lyContentAudio.btPlay.setIconResource(R.drawable.icon_pause)
                player.play()
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        fieldView.ly.lyContentAudio.slider.progress =
                            player.getCurrentPosition()
                        delay(1000L)
                    }
                }
            }

            player.hasPlayed() && player.isPlaying() -> {
                fieldView.ly.lyContentAudio.btPlay.setIconResource(R.drawable.icon_play)
                player.pause()
            }

            !player.hasPlayed() && !player.isPlaying() -> {
                fieldView.ly.lyContentAudio.btPlay.setIconResource(R.drawable.icon_pause)
                player.playFile(audioModel.file)
                lifecycleScope.launch {
                    fieldView.ly.lyContentAudio.slider.max = player.getDuration()
                    while (player.isPlaying()) {
                        fieldView.ly.lyContentAudio.slider.progress = player.getCurrentPosition()
                        delay(1000L)
                    }
                }
                player.onCompletion {
                    fieldView.ly.lyContentAudio.btPlay.setIconResource(R.drawable.icon_play)
                }
            }
        }
    }

    private fun setCardLanguages(card: ExternalCard? = null) {
//        val contentLanguage = getContentLanguage(card)
//        val definitionLanguage = getDefinitionLanguage(card)
//        if (contentLanguage != null) {
//            binding.btContentLanguage.text = contentLanguage
//        } else {
//            binding.btContentLanguage.text = getString(R.string.text_content_language)
//        }
//        if (definitionLanguage != null) {
//            binding.btDefinitionLanguage.text = definitionLanguage
//        } else {
//            binding.btDefinitionLanguage.text = getString(R.string.text_definition_language)
//        }
    }

    private fun getDefinitionLanguage(card: ExternalCard?) = when {
        !card?.cardDefinitionLanguage.isNullOrBlank() -> card?.cardDefinitionLanguage
        !deck.cardDefinitionDefaultLanguage.isNullOrBlank() -> deck.cardDefinitionDefaultLanguage
        else -> null
    }

    private fun getContentLanguage(card: ExternalCard?) = when {
        !card?.cardContentLanguage.isNullOrBlank() -> card?.cardContentLanguage
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
            sendCardsOnEdit(newCard)
            dismiss()
        } else {
            newCardViewModel.insertCard(newCard)
            Toast.makeText(appContext, getString(R.string.message_card_added), Toast.LENGTH_LONG)
                .show()
        }
        initCardAdditionPanel()
        return true
    }

    private fun areThereAnOngoingCardCreation(): Boolean {
        when {
            newCardViewModel.contentField.value.contentText != null -> return true
            newCardViewModel.contentField.value.contentImage != null -> return true
            else -> {
                newCardViewModel.definitionFields.value.forEach {
                    if (it.definitionText != null || it.definitionImage != null) {
                        return true
                    }
                }
            }
        }
        return false
    }


    private fun generateCardOnUpdate(card: ExternalCardWithContentAndDefinitions): CardWithContentAndDefinitions? {
        //TODO: Update image error
        val content = getContent(
            cardId = card.card.cardId,
            contentId = card.contentWithDefinitions.content.contentId,
            deckId = card.card.deckOwnerId
        ) ?: return null
        val definitions = getDefinition(
            cardId = card.card.cardId,
            contentId = card.contentWithDefinitions.content.contentId,
            deckId = card.card.deckOwnerId
        ) ?: return null

//        val updateCardDefinitions = mutableListOf<CardDefinition>()
//        card.contentWithDefinitions.definitions.forEachIndexed { index, definition ->
//            if (index < definitions.size) {
//                val updatedDefinition = CardDefinition(
//                    definitionId = card.contentWithDefinitions.definitions[index].definitionId,
//                    cardOwnerId = card.contentWithDefinitions.definitions[index].cardOwnerId,
//                    deckOwnerId = card.contentWithDefinitions.definitions[index].deckOwnerId,
//                    contentOwnerId = card.contentWithDefinitions.definitions[index].contentOwnerId,
//                    isCorrectDefinition = definitions[index].isCorrectDefinition,
//                    definitionText = definitions[index].definitionText,
//                    definitionImageName = definitions[index].definitionImageName,
//                    definitionAudioName = definitions[index].definitionAudioName,
//                )
//                updateCardDefinitions.add(updatedDefinition)
//            }
//        }


//        for (i in 0..card.contentWithDefinitions.definitions.size.minus(1)) {
//            val definition = try {
//                definitions[i]
//            } catch (e: IndexOutOfBoundsException) {
//                newCardViewModel.createDefinition(
//                    "",
//                    null,
//                    null,
//                    false,
//                    card.card.cardId,
//                    card.contentWithDefinitions.content.contentId,
//                    card.card.deckOwnerId
//                )
//            }
//
//            val updatedDefinition = CardDefinition(
//                definitionId = card.contentWithDefinitions.definitions[i].definitionId,
//                cardOwnerId = card.contentWithDefinitions.definitions[i].cardOwnerId,
//                deckOwnerId = card.contentWithDefinitions.definitions[i].deckOwnerId,
//                contentOwnerId = card.contentWithDefinitions.definitions[i].contentOwnerId,
//                isCorrectDefinition = definition.isCorrectDefinition,
//                definitionText = definition.definitionText,
//                definitionImageName = definition.definitionImageName,
//                definitionAudioName = definition.definitionAudioName,
//            )
//            updateCardDefinitions.add(updatedDefinition)
//        }
//        if (definitions.size > card.contentWithDefinitions.definitions.size) {
//            for (j in (card.contentWithDefinitions.definitions.size)..definitions.size.minus(1)) {
//                updateCardDefinitions.add(definitions[j])
//            }
//        }

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

        val updatedCard = Card(
            cardId = card.card.cardId,
            deckOwnerId = card.card.deckOwnerId,
            cardLevel = card.card.cardLevel,
            cardType = getCardType(definitions),
            revisionTime = card.card.revisionTime,
            missedTime = card.card.missedTime,
            creationDate = card.card.creationDate,
            lastRevisionDate = card.card.lastRevisionDate,
            nextMissMemorisationDate = card.card.nextRevisionDate,
            nextRevisionDate = card.card.nextRevisionDate,
            cardContentLanguage = contentLanguage,
            cardDefinitionLanguage = definitionLanguage
        )

        val updatedContentWithDefinitions =
            CardContentWithDefinitions(content = content, definitions = definitions)

        return CardWithContentAndDefinitions(
            card = updatedCard,
            contentWithDefinitions = updatedContentWithDefinitions
        )
    }

    private fun generateCardOnAdd(): CardWithContentAndDefinitions? {

        val cardId = UUID.randomUUID().toString()
        val contentId = UUID.randomUUID().toString()
        val newCardContent = getContent(cardId, contentId, deck.deckId)
        val newCardDefinition = getDefinition(cardId, contentId, deck.deckId)

        val contentLanguage = getNewContentLanguage()
        val definitionLanguage = getNewDefinitionLanguage()

        if (newCardContent == null) {
            return null
        }
        if (newCardDefinition == null) {
            return null
        }

        val newCard = Card(
            cardId = cardId,
            deckOwnerId = deck.deckId,
            cardLevel = L1,
            cardType = getCardType(newCardDefinition),
            revisionTime = 0,
            missedTime = 0,
            creationDate = today(),
            lastRevisionDate = null,
            nextMissMemorisationDate = null,
            nextRevisionDate = null,
            cardContentLanguage = contentLanguage,
            cardDefinitionLanguage = definitionLanguage
        )

        return CardWithContentAndDefinitions(
            card = newCard,
            contentWithDefinitions = CardContentWithDefinitions(
                content = newCardContent,
                definitions = newCardDefinition
            )
        )
    }

    private fun getNewContentLanguage(): String? {
//        val addedContentLanguage = binding.btContentLanguage.text
//        if (addedContentLanguage.toString() !in supportedLanguages) {
//            val defaultContentLanguage = deck.cardContentDefaultLanguage
//            return if (defaultContentLanguage.isNullOrBlank()) {
//                null
//            } else {
//                defaultContentLanguage
//            }
//        } else {
//            return addedContentLanguage.toString()
//        }
        return null
    }

    private fun getNewDefinitionLanguage(): String? {
//        val addedDefinitionLanguage = binding.btDefinitionLanguage.text
//        if (addedDefinitionLanguage.toString() !in supportedLanguages) {
//            val defaultDefinitionLanguage = deck.cardDefinitionDefaultLanguage
//            return if (defaultDefinitionLanguage.isNullOrBlank()) {
//                null
//            } else {
//                defaultDefinitionLanguage
//            }
//        } else {
//            return addedDefinitionLanguage.toString()
//        }
        return null
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
        val contentField = newCardViewModel.contentField.value
        val cardContentText = contentField.contentText
        val cardContentImageName = contentField.contentImage?.name
        val cardContentAudioName = contentField.contentAudio?.file?.name
        return if (cardContentText != null) {
            newCardViewModel.generateCardContent(
                contentId = contentId,
                imageName = cardContentImageName,
                audioName = cardContentAudioName,
                cardId = cardId,
                deckId = deckId,
                text = cardContentText
            )
        } else {
//            binding.lyContent.tilContentText.error = getString(R.string.til_error_card_content)
            null
        }
    }

    private fun isDefinitionError(): Boolean {
        var isText = false
        var isTrueAnswer = false
//        definitionFields.forEach {
//            if (it.ly.tieText.text.toString().isNotEmpty() && it.ly.tieText.text.toString()
//                    .isNotBlank()
//            ) {
//                isText = true
//            }
//
//            if (chipState(it.ly.btIsTrue) && it.ly.tieText.text.toString()
//                    .isNotEmpty() && it.ly.tieText.text.toString().isNotBlank()
//            ) {
//                if (isText) {
//                    return false
//                }
//            }
//            if (chipState(it.ly.btIsTrue)) {
//                isTrueAnswer = true
//            }
//        }
//        if (!isTrueAnswer) {
//            binding.lyDefinition1.tilText.error =
//                getString(R.string.cp_error_correct_definition)
//        }
//        if (!isText) {
//            binding.lyDefinition1.tilText.error =
//                getString(R.string.til_error_card_definition)
//        }
        return false
    }

    private fun getDefinition(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition>? {
//        if (isDefinitionError()) {
//            return null
//        } else {
//            definitionList.clear()
//            definitionFields.forEach {
//                if (it.ly.tieText.text.toString().isNotEmpty() && it.ly.tieText.text.toString()
//                        .isNotBlank()
//                ) {
//                    definitionList.add(
//                        newCardViewModel.createDefinition(
//                            it.ly.tieText.text.toString(),
//                            it.imageName,
//                            it.audioName,
//                            chipState(it.ly.btIsTrue),
//                            cardId,
//                            contentId,
//                            deckId
//                        )
//                    )
//                }
//            }
//        }
        val definitions = arrayListOf<CardDefinition>()
        newCardViewModel.definitionFields.value.forEach { definitionField ->
            val definition = newCardViewModel.definitionFieldToCardDefinition(
                field = definitionField,
                cardId = cardId,
                contentId = contentId,
                deckId = deckId
            )
            definitions.add(definition)
        }
        return definitions
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
            val states = ColorStateList(
                arrayOf(intArrayOf()),
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
    }

    private fun sendCardsOnEdit(
        card: CardWithContentAndDefinitions
    ) {
        parentFragmentManager.setFragmentResult(
            REQUEST_CODE_CARD,
            bundleOf(EDIT_CARD_BUNDLE_KEY to card)
        )
    }

    private fun onAddMoreDefinition() {
//        if (revealedDefinitionFields < definitionFields.size) {
//            definitionFields[revealedDefinitionFields].apply {
//                container.isVisible = true
//            }
//            revealedDefinitionFields++
//
//        }
        if (newCardViewModel.getDefinitionFieldCount() < 10) {
            newCardViewModel.addDefinitionField(null)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_message_max_10_definitions),
                Toast.LENGTH_LONG
            ).show()
        }
    }

//    private fun deleteDefinitionField(field: TextInputEditText) {
//        // TODO: On delete field with image and audio
//        if (field == binding.lyDefinition10.tieText) {
//            clearField(definitionFields.last())
//            return
//        }
//        var index = 0
//        while (true) {
//            val actualField = definitionFields[index]
//            if (actualField.ly.tieText == field) {
//                break
//            }
//            index++
//        }
//        while (true) {
//            val actualField = definitionFields[index]
//            val nextField = definitionFields[index.plus(1)]
//            if (!nextField.ly.tieText.isVisible) {
//                clearField(actualField)
//                break
//            }
//            if (
//                nextField.ly.tieText.text?.isNotBlank() == true &&
//                nextField.ly.tieText.text?.isNotEmpty() == true
//            ) {
//                actualField.ly.tieText.text = nextField.ly.tieText.text
//                nextField.ly.tieText.text?.clear()
//            } else {
//                actualField.ly.tieText.text?.clear()
//                clearField(definitionFields.last { it.container.isVisible })
//                break
//            }
//            index++
//        }
//    }

//    private fun deleteDefinitionField(field: TextInputEditText) {
//        // TODO: On delete field with image and audio
//        if (field == binding.lyDefinition10.tieText) {
//            clearField(definitionFields.last())
//            return
//        }
//        var index = 0
//        while (true) {
//            val actualField = definitionFields[index]
//            if (actualField.ly.tieText == field) {
//                break
//            }
//            index++
//        }
//        while (true) {
//            val actualField = definitionFields[index]
//            val nextField = definitionFields[index.plus(1)]
//            if (!nextField.container.isVisible) {
//                clearField(actualField)
//                break
//            }
//            if (
//                nextField.ly.tieText.text?.isNotBlank() == true &&
//                nextField.ly.tieText.text?.isNotEmpty() == true ||
//                nextField.imageName != null ||
//                nextField.audioName != null
//            ) {
//                actualField.ly.tieText.text = nextField.ly.tieText.text
//                nextField.imageName?.let { name ->
//                    actualField.imageName = name
//                    actualField.ly.clContainerImage.visibility = View.VISIBLE
//                    val filePhotoDefinition = File(requireContext().filesDir, name)
//                    val bytesPhotoDefinition = filePhotoDefinition.readBytes()
//                    val bmpPhotoDefinition = BitmapFactory.decodeByteArray(
//                        bytesPhotoDefinition,
//                        0,
//                        bytesPhotoDefinition.size
//                    )
//                    actualField.ly.imgPhoto.setImageBitmap(bmpPhotoDefinition)
//                }
//                nextField.audioName?.let { name ->
//                    // TODO: Move audio content from next field to actual field
//                    actualField.ly.llContainerAudio.visibility = View.VISIBLE
//                    actualField.audioName = name
//                }
//                nextField.ly.tieText.text?.clear()
//                nextField.audioName = null
//                nextField.imageName = null
//                nextField.ly.imgPhoto.setImageBitmap(null)
//                nextField.ly.clContainerImage.visibility = View.GONE
//                nextField.ly.llContainerAudio.visibility = View.GONE
//            } else {
//                actualField.ly.tieText.text?.clear()
//                actualField.audioName = null
//                actualField.imageName = null
//                actualField.ly.imgPhoto.setImageBitmap(null)
//                actualField.ly.clContainerImage.visibility = View.GONE
//                actualField.ly.llContainerAudio.visibility = View.GONE
//                clearField(definitionFields.last { it.container.isVisible })
//                break
//            }
//            index++
//        }
//    }

//    private fun clearField(field: FieldModel) {
//        field.apply {
//            container.visibility = View.GONE
//            ly.tieText.text?.clear()
//            unCheckChip(ly.btIsTrue)
//            field.imageName?.let {
////                onDeleteImage(field)
//            }
//            field.audioName?.let {
//                // TODO: Delete audio
//            }
//        }
//        revealedDefinitionFields--
//    }

    @Throws(IOException::class)
    private fun textFromUriToImmutableCards(
        uri: Uri,
        separator: String
    ): List<CardWithContentAndDefinitions> {
        val result: MutableList<CardWithContentAndDefinitions> = mutableListOf()
        appContext?.contentResolver?.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    val card = textToImmutableCard(line, separator, deck.deckId)
                    result.add(card)
                }
            }
        }
        return result
    }

    fun onTakePhoto() {
        //TODO: Improve onTakePhoto
        checkCameraPermission(
            {
                attachPhotoFromCamera.launch()
            },
            {
                onAttachePhotoFromCameraRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }

    fun onPickPhoto() {
        onAttachPhotoFromGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun onRecordAudio(selectedFieldPosition: Int?) {

        if (selectedFieldPosition != null) {
            val audioRecorder = AudioRecorderDialog()
            audioRecorder.show(childFragmentManager, "AudioRecorderDialog")
            childFragmentManager.setFragmentResultListener(
                REQUEST_CODE_AUDIO_RECORDER, this
            ) { _, bundle ->

                val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(
                        AudioRecorderDialog.AUDIO_RECORDER_BUNDLE_KEY,
                        AudioModel::class.java
                    )
                } else {
                    bundle.getParcelable<AudioModel>(AudioRecorderDialog.AUDIO_RECORDER_BUNDLE_KEY)
                }

                result?.let { newAudio ->
                    // TODO: Update audio field
                    if (selectedFieldPosition < 0) {
                        binding.lyContent.llContentContainerAudio.visibility = View.VISIBLE
                        newCardViewModel.updateContentField(
                            updatedContentField = newCardViewModel.contentField.value.copy(
                                contentAudio = newAudio
                            )
                        )
                        onSetContentFieldAudio()
                    } else {
                        newCardViewModel.updateDefinitionAudio(
                            id = newCardViewModel.getDefinitionFieldAt(selectedFieldPosition).definitionId,
                            audio = newAudio
                        )
                        onSetDefinitionFieldAudio(definitionFields[selectedFieldPosition])
                    }
                }
            }
        } else {
            Toast.makeText(
                appContext,
                getString(R.string.error_message_no_field_selected),
                Toast.LENGTH_SHORT
            ).show()
        }


//        if (selectedFieldPosition != null) {
//            if (selectedFieldPosition < 0) {
//                if (!recorder.isRecording()) {
//                    binding.lyContent.llContentContainerAudio.visibility = View.VISIBLE
//                    Toast.makeText(requireContext(), "Started recording", Toast.LENGTH_LONG).show()
//                    val audioName = "${UUID.randomUUID()}.mp3"
//                    File(appContext?.filesDir, audioName).also {
//                        recorder.start(it)
//                        val newAudio = AudioModel(
//                            file = it,
//                        )
//                        newCardViewModel.updateContentField(updatedContentField = newCardViewModel.contentField.value.copy(
//                            contentAudio = newAudio
//                        ))
//                        onSetContentFieldAudio()
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Stoped recording", Toast.LENGTH_SHORT).show()
//                    recorder.stop()
//                }
//
//            } else {
//                if(!recorder.isRecording()) {
//                    val audioName = "${UUID.randomUUID()}.mp3"
//                    File(appContext?.filesDir, audioName).also {
//                        recorder.start(it)
//                        val newAudio = AudioModel(
//                            file = it,
//                        )
//                        newCardViewModel.updateDefinitionAudio(
//                            id = newCardViewModel.getDefinitionFieldAt(selectedFieldPosition).definitionId,
//                            audio = newAudio
//                        )
//                        onSetDefinitionFieldAudio(definitionFields[selectedFieldPosition])
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Stoped recording", Toast.LENGTH_SHORT).show()
//                    recorder.stop()
//                }
//
//            }
//        }

//        Toast.makeText(appContext, "Record audio in development", Toast.LENGTH_SHORT).show()
    }

    fun onDeleteAudio(selectedFieldPosition: Int) {
        //TODO: Implement Delete audio
        if (selectedFieldPosition < 0) {
            // TODO: Content field
//            binding.lyContent.llContentContainerAudio.visibility = View.GONE
        } else {
            // TODO: Definition field
//            selectedField.llContainerAudio.visibility = View.VISIBLE
        }

        Toast.makeText(appContext, "Delete audio in development", Toast.LENGTH_SHORT).show()
    }

    fun onDeleteImage(fieldPosition: Int) {
        val imageName = if (fieldPosition == -1) {
            newCardViewModel.contentField.value.contentImage?.name
        } else {
            newCardViewModel.definitionFields.value[fieldPosition].definitionImage?.name
        }
        imageName?.let {
            deleteImageFromInternalStorage(it)
        }
    }

    private fun saveImageToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return try {
            appContext!!.openFileOutput(filename, MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun deleteImageFromInternalStorage(filename: String): Boolean {
        return try {
            appContext!!.deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun deleteAudioFromInternalStorage(audio: AudioModel): Boolean {
        return try {
            appContext!!.deleteFile(audio.file.name)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(appContext, "Could not delete audio", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun onSetDefinitionFieldPhoto(actualField: FieldModel, imageBitmap: Bitmap?) {
        actualField.ly.clContainerImage.visibility = View.VISIBLE
        actualField.ly.imgPhoto.setImageBitmap(imageBitmap)
    }

    fun onSetDefinitionFieldAudio(actualField: FieldModel) {
        actualField.ly.llContainerAudio.visibility = View.VISIBLE
    }

    fun onRemoveDefinitionFieldPhoto(actualField: FieldModel) {
        actualField.ly.clContainerImage.visibility = View.GONE
        actualField.ly.imgPhoto.setImageBitmap(null)
    }

    fun onRemoveDefinitionFieldAudio(actualField: FieldModel) {
        actualField.ly.llContainerAudio.visibility = View.GONE
    }

    fun onRemoveContentFieldAudio() {
        binding.lyContent.llContentContainerAudio.visibility = View.GONE
    }

    fun onSetContentFieldPhoto(imageBitmap: Bitmap) {
        binding.lyContent.clContentContainerImage.visibility = View.VISIBLE
        binding.lyContent.imgContentPhoto.setImageBitmap(imageBitmap)
    }

    fun onRemoveContentFieldPhoto() {
        binding.lyContent.clContentContainerImage.visibility = View.GONE
        binding.lyContent.imgContentPhoto.setImageBitmap(null)
    }

    fun onSetContentFieldAudio() {
        binding.lyContent.llContentContainerAudio.visibility = View.VISIBLE
    }


    private fun dateFormat(duration: Int): String {
        var d = duration / 1000
        var s = d % 60
        var m = (d / 60 % 60)
        var h = ((d - m * 60) / 360).toInt()
        val f: DecimalFormat = DecimalFormat("00")
        var str = "$m:${f.format(s)}"
        if (h > 0) {
            str = "$h:$str"
        }
        return str
    }

}