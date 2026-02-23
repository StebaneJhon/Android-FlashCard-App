package com.soaharisonstebane.mneme.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
import android.text.Spannable
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.TextView
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.entities.CardContent
import com.soaharisonstebane.mneme.backend.entities.CardDefinition
import com.soaharisonstebane.mneme.databinding.AddCardLayoutDialogBinding
import com.soaharisonstebane.mneme.util.CardLevel.L1
import com.soaharisonstebane.mneme.util.CardType.SINGLE_ANSWER_CARD
import com.soaharisonstebane.mneme.util.CardType.MULTIPLE_ANSWER_CARD
import com.soaharisonstebane.mneme.util.CardType.MULTIPLE_CHOICE_CARD
import com.soaharisonstebane.mneme.util.Constant
import com.soaharisonstebane.mneme.helper.LanguageUtil
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
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.backend.entities.Card
import com.soaharisonstebane.mneme.backend.entities.relations.CardContentWithDefinitions
import com.soaharisonstebane.mneme.backend.entities.relations.CardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalCard
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalDeck
import com.soaharisonstebane.mneme.helper.AppMath
import com.soaharisonstebane.mneme.helper.AppThemeHelper
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel
import com.soaharisonstebane.mneme.helper.playback.AndroidAudioPlayer
import com.soaharisonstebane.mneme.util.AttachRef.ATTACH_AUDIO_RECORD
import com.soaharisonstebane.mneme.util.AttachRef.ATTACH_IMAGE_FROM_CAMERA
import com.soaharisonstebane.mneme.util.AttachRef.ATTACH_IMAGE_FROM_GALERI
import com.soaharisonstebane.mneme.util.ScanRef.AUDIO_TO_TEXT
import com.soaharisonstebane.mneme.util.ScanRef.IMAGE_FROM_CAMERA_TO_TEXT
import com.soaharisonstebane.mneme.util.ScanRef.IMAGE_FROM_GALERI_TO_TEXT
import com.soaharisonstebane.mneme.util.UiState
import com.soaharisonstebane.mneme.helper.parcelable
import com.soaharisonstebane.mneme.helper.showSnackbar
import com.soaharisonstebane.mneme.helper.textToImmutableCard
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import java.util.UUID
import kotlin.getValue

class NewCardDialog(
    private var card: ExternalCardWithContentAndDefinitions?,
    private val deck: ExternalDeck,
    private val action: String
) : AppCompatDialogFragment() {

    private var _binding: AddCardLayoutDialogBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    var imm: InputMethodManager? = null
    private var actualFieldLanguage: String? = null
    private var cardUploadingJob: Job? = null
    private val supportedLanguages = LanguageUtil().getSupportedLang()
    private lateinit var importCardsFromDeviceModel: CardImportFromDeviceModel
    private lateinit var attachBottomSheetDialog: AttachBottomSheetDialog
    private lateinit var scanBottomSheetDialog: ScanBottomSheetDialog

    var colorSurfaceContainerLow: Int = Color.WHITE
    var colorPrimary: Int = Color.BLACK

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

    val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    private var uri: Uri? = null
    private lateinit var definitionFields: MutableList<FieldModel>

    private var takePreview =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val image: InputImage
                try {
                    image = InputImage.fromFilePath(requireContext(), uri!!)
                    detectTextFromAnImageWithMLKit(image) { text ->
                        newCardViewModel.getActiveFieldIndex().let { activeFieldIndex ->
                            when {
                                activeFieldIndex == null -> {
                                    showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
                                }

                                activeFieldIndex < 0 -> {
                                    setFieldText(text, activeFieldIndex)
                                }

                                activeFieldIndex >= 0 -> {
                                    setFieldText(text, activeFieldIndex)
                                }
                            }
                        }
                    }
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
                    detectTextFromAnImageWithMLKit(image) { text ->
                        newCardViewModel.getActiveFieldIndex().let { activeFieldIndex ->
                            when {
                                activeFieldIndex == null -> {
                                    showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
                                }

                                activeFieldIndex < 0 -> {
                                    setFieldText(text, activeFieldIndex)
                                }

                                activeFieldIndex >= 0 -> {
                                    setFieldText(text, activeFieldIndex)
                                }
                            }
                        }
                    }
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
                    showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_text_detected)
                } else {
                    newCardViewModel.getActiveFieldIndex().let { activeFieldIndex ->
                        when {
                            activeFieldIndex == null -> {
                                showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
                            }

                            activeFieldIndex < 0 -> {
                                setFieldText(result[0], activeFieldIndex)
                            }

                            activeFieldIndex >= 0 -> {
                                setFieldText(result[0], activeFieldIndex)
                            }
                        }
                    }
                }
            } else {
                showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_text_detected)
            }
        }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_permission_not_granted_camera)
        }
    }

    private var openFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val cards = textFromUriToImmutableCards(uri, ":")
                newCardViewModel.insertCards(cards)
                showSnackbar(binding.root, binding.dockedToolbar, getString(R.string.message_cards_added, "${cards.size}"))
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
            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_permission_not_granted_camera)
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
                showSnackbar(binding.root, binding.dockedToolbar, R.string.could_not_get_image)
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
        const val REQUEST_CODE_TRANSLATION_OPTION = "9"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.QuizeoFullscreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewTheme = deckPathViewModel.getViewTheme()
        val contextThemeWrapper = ContextThemeWrapper(requireContext(), viewTheme)
        val themeInflater = inflater.cloneInContext(contextThemeWrapper)
        _binding = AddCardLayoutDialogBinding.inflate(themeInflater, container, false)
        appContext = activity?.applicationContext
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { v, insets ->
                val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, imeInsets.bottom)
                insets
            }
        } else {
            @Suppress("DEPRECATION")
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }


        initFields()

        colorSurfaceContainerLow = MaterialColors.getColor(
            binding.inFormatOptions.btFormatBold,
            com.google.android.material.R.attr.colorSurfaceContainerLow
        )
        colorPrimary = MaterialColors.getColor(
            binding.inFormatOptions.btFormatBold,
            com.google.android.material.R.attr.colorPrimarySurface
        )

        attachBottomSheetDialog = AttachBottomSheetDialog()
        scanBottomSheetDialog = ScanBottomSheetDialog()

        val arrayAdapterSupportedLanguages = ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)
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
            onAddMoreDefinition()
        }

        binding.btSave.setOnClickListener {
            onPositiveAction()
        }
        binding.btTranslate.setOnClickListener {
            onTranslateText(
                newCardViewModel.getActiveFieldIndex()
            )
        }
        binding.btAddMedia.setOnClickListener {
            onAttach()
        }
        binding.btScan.setOnClickListener {
            onScan()
        }
        binding.btFormat.setOnClickListener {
            if (newCardViewModel.getActiveFieldIndex() != null) {
                binding.containerFormatOptions.visibility = View.VISIBLE
            } else {
                showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
            }
        }

        binding.inFormatOptions.btClose.setOnClickListener {
            binding.containerFormatOptions.visibility = View.GONE
        }

        binding.inFormatOptions.btFormatBold.setOnClickListener {
            onFormatButtonClicked(binding.inFormatOptions.btFormatBold, StyleSpan(Typeface.BOLD))
        }
        binding.inFormatOptions.btFormatItalic.setOnClickListener {
            onFormatButtonClicked(binding.inFormatOptions.btFormatItalic, StyleSpan(Typeface.ITALIC))
        }
        binding.inFormatOptions.btFormatUnderlined.setOnClickListener {
            onFormatButtonClicked(binding.inFormatOptions.btFormatUnderlined, UnderlineSpan())
        }
        binding.inFormatOptions.btFormatStrikethrough.setOnClickListener {
            onFormatButtonClicked(binding.inFormatOptions.btFormatStrikethrough, StrikethroughSpan())
        }

    }

    fun onFormatButtonClicked(button: MaterialButton, span: CharacterStyle) {
        val selectedFieldIndex = newCardViewModel.getActiveFieldIndex()
        if (selectedFieldIndex != null) {
            if (selectedFieldIndex == -1) {
                onFormatText(
                    button = button,
                    field = binding.lyContent.tieContentText,
                    span = span
                )
                val htmText = Html.toHtml(binding.lyContent.tieContentText.text, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE).trim()
                newCardViewModel.updateContentText(htmText)
            } else {
                onFormatText(
                    button = button,
                    field = definitionFields[selectedFieldIndex].ly.tieText,
                    span = span
                )
                val htmlText = Html.toHtml(definitionFields[selectedFieldIndex].ly.tieText.text, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE).trim()
                newCardViewModel.updateDefinitionText(
                    id = newCardViewModel.getDefinitionFieldAt(selectedFieldIndex).definitionId,
                    text = htmlText
                )
            }
        } else {
            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
        }

    }

    fun onFormatText(button: MaterialButton, field: TextInputEditText, span: CharacterStyle) {
        val start = field.selectionStart
        val end = field.selectionEnd
        val editable = field.text

        editable?.let { editable ->
            val spans = when (span) {
                is StyleSpan -> editable.getSpans(start, end, StyleSpan::class.java)?.filter { it.style == span.style}
                is UnderlineSpan -> editable.getSpans(start, end, UnderlineSpan::class.java).toList()
                is StrikethroughSpan -> editable.getSpans(start, end, StrikethroughSpan::class.java).toList()
                else -> emptyList<CharacterStyle>()
            }

            if (spans?.isNotEmpty() == true) {
                button.apply {
                    backgroundTintList = ColorStateList.valueOf(colorSurfaceContainerLow)
                    iconTint = ColorStateList.valueOf(colorPrimary)
                }
                spans.forEach { span ->
                    val s = editable.getSpanStart(span)
                    if (start == end) {
                        editable.setSpan(span, s, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else {
                        editable.removeSpan(span)
                    }
                }
            } else {
                button.apply {
                    backgroundTintList = ColorStateList.valueOf(colorPrimary)
                    iconTint = ColorStateList.valueOf(colorSurfaceContainerLow)
                }
                applyFormatToSelection(field, span, start, end)
            }
        }
    }

    private fun applyFormatToSelection(field: TextInputEditText, span: Any, start: Int, end: Int) {
        field.text?.setSpan(
            span,
            start,
            end,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
    }

    private fun updateFormatButtonUi(field: TextInputEditText) {
        val start = field.selectionStart
        val end = field.selectionEnd
        val editable = field.text

        val isBold = editable?.getSpans(start, end, StyleSpan::class.java)?.any {it.style == Typeface.BOLD } ?: false
        val isItalic = editable?.getSpans(start, end, StyleSpan::class.java)?.any {it.style == Typeface.ITALIC } ?: false
        val underlineSpan = editable?.getSpans(start, end, UnderlineSpan::class.java)?.isNotEmpty() ?: false
        val strikethroughSpan = editable?.getSpans(start, end, StrikethroughSpan::class.java)?.isNotEmpty() ?: false

        binding.inFormatOptions.btFormatBold.apply {
            backgroundTintList = ColorStateList.valueOf(if (isBold) colorPrimary else colorSurfaceContainerLow)
            iconTint = ColorStateList.valueOf(if (isBold) colorSurfaceContainerLow else colorPrimary)
        }
        binding.inFormatOptions.btFormatItalic.apply {
            backgroundTintList = ColorStateList.valueOf(if (isItalic) colorPrimary else colorSurfaceContainerLow)
            iconTint = ColorStateList.valueOf(if (isItalic) colorSurfaceContainerLow else colorPrimary)
        }
        binding.inFormatOptions.btFormatUnderlined.apply {
            backgroundTintList = ColorStateList.valueOf(if (underlineSpan) colorPrimary else colorSurfaceContainerLow)
            iconTint = ColorStateList.valueOf(if (underlineSpan) colorSurfaceContainerLow else colorPrimary)
        }
        binding.inFormatOptions.btFormatStrikethrough.apply {
            backgroundTintList = ColorStateList.valueOf(if (strikethroughSpan) colorPrimary else colorSurfaceContainerLow)
            iconTint = ColorStateList.valueOf(if (strikethroughSpan) colorSurfaceContainerLow else colorPrimary)
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newCardViewModel.definitionFields.collect { fields ->
                    displayDefinitionFields(fields)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                newCardViewModel.contentField.collect { content ->
                    displayContentField(content)
                }
            }
        }

        if (card != null && action == Constant.UPDATE) {
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_update_card)
            onUpdateCard(card!!)
        } else {
            binding.tabAddNewUpdateCard.title = getString(R.string.tv_add_new_card)
            onAddCard()
        }

    }

    private fun playPauseContendAudio() {
        when {
            player.hasPlayed() && !player.isPlaying() -> {
                binding.lyContent.lyContentAudio.btPlay.setIconResource(R.drawable.icon_pause)
                player.play()
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(player.getCurrentPosition(), player.getDuration())
                        binding.lyContent.lyContentAudio.lpiAudioProgression.progress = progress
                        delay(100L)
                    }
                }
            }

            player.hasPlayed() && player.isPlaying() -> {
                binding.lyContent.lyContentAudio.btPlay.setIconResource(R.drawable.icon_play)
                player.pause()
            }

            !player.hasPlayed() && !player.isPlaying() -> {
                newCardViewModel.contentField.value.contentAudio?.let { audioModel ->
                    binding.lyContent.lyContentAudio.btPlay.setIconResource(R.drawable.icon_pause)
                    val audioFile = File(context?.filesDir, audioModel.name)
                    player.playFile(audioFile)
                    lifecycleScope.launch {
                        while (player.isPlaying()) {
                            val progress = AppMath().normalize(player.getCurrentPosition(), player.getDuration())
                            binding.lyContent.lyContentAudio.lpiAudioProgression.progress = progress
                            delay(100L)
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
                            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
                        }
                    }

                    ATTACH_IMAGE_FROM_GALERI -> {
                        if (newCardViewModel.getActiveFieldIndex() != null) {
                            onPickPhoto()
                        } else {
                            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
                        }
                    }

                    ATTACH_AUDIO_RECORD -> {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            checkRecordAudioPermission()
                        } else {
                            onRecordAudio(newCardViewModel.getActiveFieldIndex())
                        }

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
                        val fieldIndex = newCardViewModel.getActiveFieldIndex()
                        when {
                            fieldIndex == null -> {
                                showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
                            }

                            fieldIndex < 0 -> {
                                listen(getNewContentLanguage())
                            }

                            fieldIndex >= 0 -> {
                                listen(getNewDefinitionLanguage())
                            }
                        }
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
            val result = bundle.parcelable<CardImportFromDeviceModel>(ImportCardsFromDeviceDialog.EXPORT_CARD_FROM_DEVICE_BUNDLE_KEY)
            if (result != null) {
                importCardsFromDeviceModel = result
                openFile.launch(arrayOf("text/*"))
            } else {
                showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_card_import_failed)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showTriviaQuestionUploader() {
        val newDeckDialog = UploadOpenTriviaQuizDialog()
        newDeckDialog.show(childFragmentManager, "upload open trivia quiz dialog")
        childFragmentManager.setFragmentResultListener(CardFragment.REQUEST_CODE, this) { _, bundle ->
            val result = bundle.parcelable<OpenTriviaQuizModel>(UploadOpenTriviaQuizDialog.OPEN_TRIVIA_QUIZ_MODEL_BUNDLE_KEY)
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
                            showSnackbar(binding.root, binding.dockedToolbar, message)
                        }

                        is UiState.Loading -> {
                            binding.llAddCardProgressBar.isVisible = true
                        }

                        is UiState.Success -> {

                            val a = response.data.results

                            val newCards = newCardViewModel.resultsToImmutableCards(
                                deck.deckId,
                                response.data.results
                            )
                            newCardViewModel.insertCards(newCards)
                            binding.llAddCardProgressBar.visibility = View.GONE
                            showSnackbar(binding.root, binding.dockedToolbar, getString(R.string.message_cards_added, "${newCards.size}"))
                        }
                    }
                }
            }

        }
    }

    private fun onFieldFocused(
        fieldViewContainer: ConstraintLayout,
        v: View?,
        hasFocus: Boolean,
    ) {
        if (hasFocus) {
            fieldViewContainer.setBackgroundResource(R.drawable.bg_add_card_field_focused)
            //TODO: To be improved. Scroll to focused view.
            v?.postDelayed({
                val y = fieldViewContainer.bottom.plus(binding.dockedToolbar.height)
                binding.nestedScrollView.smoothScrollTo(0, maxOf(y, 0))
            }, 200)
        } else {
            fieldViewContainer.setBackgroundResource(R.drawable.bg_add_card_field)
        }
    }

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
        } else {
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
            "com.soaharisonstebane.mneme.FileProvider",
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
        language: String? = actualFieldLanguage,
        onTextDetected: (String) -> Unit
    ) {
        val recognizer = if (language.isNullOrBlank()) {
            getRecognizer(LanguageUtil().getLanguageByCode(Locale.getDefault().language))
        } else {
            getRecognizer(language)
        }
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isBlank()) {
                    showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_text_detected)
                } else {
                    onTextDetected(visionText.text)
                }
            }
            .addOnFailureListener { e ->
                showSnackbar(binding.root, binding.dockedToolbar, e.message.toString())
            }
    }

    private fun initCardAdditionPanel() {
        newCardViewModel.clearFields()
        definitionFields.first().ly.llErrorContainer.visibility = View.GONE
        binding.lyContent.llError.visibility = View.GONE
        binding.lyContent.tieContentText.text?.clear()
        definitionFields.forEach { fieldView ->
            fieldView.ly.tieText.text?.clear()
        }
    }

    private fun chipState(chip: TextView): Boolean {
        return chip.text == getString(R.string.cp_true_text)
    }

    private fun checkChip(chip: TextView) {
        chip.apply {
            when (AppThemeHelper.getSavedTheme(context)) {
                1 -> {
                    checkChipLightTheme(chip)
                }
                2 -> {
                    checkChipDarkTheme(chip)
                }
                else -> {
                    if (AppThemeHelper.isSystemDarkTheme(context)) {
                        checkChipDarkTheme(chip)
                    } else {
                        checkChipLightTheme(chip)
                    }
                }
            }
        }
    }

    private fun checkChipLightTheme(chip: TextView) {
        chip.apply {
            background.setTint(ContextCompat.getColor(context, R.color.green200))
            setTextColor(ContextCompat.getColor(context, R.color.green500))
            text = getString(R.string.cp_true_text)
        }
    }

    private fun checkChipDarkTheme(chip: TextView) {
        chip.apply {
            background.setTint(ContextCompat.getColor(context, R.color.green700))
            setTextColor(ContextCompat.getColor(context, R.color.green50))
            text = getString(R.string.cp_true_text)
        }
    }

    private fun unCheckChip(chip: TextView) {
        chip.apply {
            when (AppThemeHelper.getSavedTheme(context)) {
                1 -> {
                    unCheckLightChip(chip)
                }
                2 -> {
                    unCheckDarkThemeChip(chip)
                }
                else -> {
                    if (AppThemeHelper.isSystemDarkTheme(context)) {
                        unCheckDarkThemeChip(chip)
                    } else {
                        unCheckLightChip(chip)
                    }
                }
            }
        }
    }

    private fun unCheckLightChip(chip: TextView) {
        chip.apply {
            background.setTint(ContextCompat.getColor(context, R.color.red200))
            setTextColor(ContextCompat.getColor(context, R.color.red500))
            text = getString(R.string.cp_false_text)
        }
    }

    private fun unCheckDarkThemeChip(chip: TextView) {
        chip.apply {
            background.setTint(ContextCompat.getColor(context, R.color.red700))
            setTextColor(ContextCompat.getColor(context, R.color.red50))
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
                    if (card != null && action == Constant.UPDATE) {
                        newCardViewModel.definitionFields.value.forEachIndexed { index, model ->
                            if (index < card!!.contentWithDefinitions.definitions.size) {
                                if (card!!.contentWithDefinitions.definitions[index].definitionId == model.definitionId) {
                                    model.definitionImage?.let { image ->
                                        if (image.name != card!!.contentWithDefinitions.definitions[index].definitionImage?.name) {
                                            deleteImageFromInternalStorage(image.name)
                                        }
                                    }
                                    model.definitionAudio?.let { audio ->
                                        if (audio.name != card!!.contentWithDefinitions.definitions[index].definitionAudio?.name) {
                                            deleteAudioFromInternalStorage(audio)
                                        }
                                    }
                                }
                            } else {
                                model.definitionImage?.let { image ->
                                    deleteImageFromInternalStorage(image.name)
                                }
                                model.definitionAudio?.let { audio ->
                                    deleteAudioFromInternalStorage(audio)
                                }
                            }
                        }
                        newCardViewModel.contentField.value.contentImage?.let { image ->
                            if (image.name != card!!.contentWithDefinitions.content.contentImage?.name) {
                                deleteImageFromInternalStorage(image.name)
                            }
                        }
                        newCardViewModel.contentField.value.contentAudio?.let { audio ->
                            if (audio.name != card!!.contentWithDefinitions.content.contentAudio?.name) {
                                deleteAudioFromInternalStorage(audio)
                            }
                        }
                    } else {
                        newCardViewModel.definitionFields.value.forEach { fieldModel ->
                            fieldModel.definitionImage?.let { image ->
                                deleteImageFromInternalStorage(image.name)
                            }
                            fieldModel.definitionAudio?.let { audio ->
                                deleteAudioFromInternalStorage(audio)
                            }
                        }
                        newCardViewModel.contentField.value.contentImage?.let { image ->
                                deleteImageFromInternalStorage(image.name)
                        }
                        newCardViewModel.contentField.value.contentAudio?.let { audio ->
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
        newCardViewModel.initAddCardFields(card = null)
        setCardLanguages()
    }

    private fun onUpdateCard(card: ExternalCardWithContentAndDefinitions) {
        newCardViewModel.initAddCardFields(card = card)

        setCardLanguages(card.card)

    }

    fun displayContentField(content: ContentFieldModel) {
        content.contentText?.let { text ->
            val spannableString = Html.fromHtml(text, FROM_HTML_MODE_LEGACY).trim()
            binding.lyContent.tieContentText.setText(spannableString)
            binding.lyContent.tieContentText.setSelection(spannableString.length)
        }

        binding.lyContent.tieContentText.setOnClickListener {
            updateFormatButtonUi(binding.lyContent.tieContentText)
        }

        binding.lyContent.tieContentText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                enableFormatButton(true)
                onFieldFocused(
                    fieldViewContainer = binding.lyContent.llContentField,
                    v = v,
                    hasFocus = true,
                )
                newCardViewModel.focusToContent()
                if (newCardViewModel.hasDefinitionText()) {
                    enableTranslateButton(true)
                } else {
                    enableTranslateButton(false)
                }
            } else {
                enableFormatButton(false)
                onFieldFocused(
                    fieldViewContainer = binding.lyContent.llContentField,
                    v = v,
                    hasFocus = false,
                )
                v.clearFocus()
                enableTranslateButton(false)
            }
        }
        binding.lyContent.tieContentText.addTextChangedListener { text ->
            updateFormatButtonUi(binding.lyContent.tieContentText)
            val htmlText = Html.toHtml(text, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE).trim()
            newCardViewModel.updateContentText(htmlText)
        }

        if (content.contentImage != null) {
            onSetContentFieldPhoto(content.contentImage!!.bmp!!)
            binding.lyContent.btContentDeleteImage.setOnClickListener {
                if (card != null && action == Constant.UPDATE) {
                    newCardViewModel.deleteContentImageField()
                    onRemoveContentFieldPhoto()
                } else {
                    if (deleteImageFromInternalStorage(content.contentImage!!.name)) {
                        newCardViewModel.deleteContentImageField()
                        onRemoveContentFieldPhoto()
                    }
                }
            }
        } else {
            onRemoveContentFieldPhoto()
        }

        if (content.contentAudio != null) {
            onSetContentFieldAudio(content.contentAudio?.duration!!)
            binding.lyContent.lyContentAudio.btPlay.setOnClickListener {
                playPauseContendAudio()
            }
            binding.lyContent.lyContentAudio.btDelete.setOnClickListener {
                if (card != null && action == Constant.UPDATE) {
                    newCardViewModel.deleteContentAudioField()
                    onRemoveContentFieldAudio()
                } else {
                    if (deleteAudioFromInternalStorage(content.contentAudio!!)) {
                        newCardViewModel.deleteContentAudioField()
                        onRemoveContentFieldAudio()
                    }
                }
            }
        } else {
            onRemoveContentFieldAudio()
        }

    }

    private fun enableTranslateButton(enable: Boolean) {
        binding.btTranslate.isEnabled = enable
        binding.btTranslate.isClickable = enable
    }

    private fun enableFormatButton(enable: Boolean) {
        binding.btFormat.isEnabled = enable
        binding.btFormat.isEnabled = enable
    }

    private fun displayDefinitionFields(fields: List<DefinitionFieldModel>) {
        definitionFields.forEachIndexed { index, fieldView ->
            if (index < fields.size) {
                fieldView.container.visibility = View.VISIBLE
                val actualDefinitionFieldModel = fields[index]

                actualDefinitionFieldModel.definitionText?.let { text ->
                    val spannableString = Html.fromHtml(text, FROM_HTML_MODE_LEGACY).trim()
                    fieldView.ly.tieText.setText(spannableString)
                    fieldView.ly.tieText.setSelection(spannableString.length)
                }

                if (actualDefinitionFieldModel.definitionImage != null) {
                    onSetDefinitionFieldPhoto(
                        fieldView,
                        actualDefinitionFieldModel.definitionImage!!.bmp
                    )
                } else {
                    onRemoveDefinitionFieldPhoto(fieldView)
                }

                if (actualDefinitionFieldModel.definitionAudio != null) {
                    onSetDefinitionFieldAudio(fieldView, actualDefinitionFieldModel.definitionAudio!!.duration)
                } else {
                    onRemoveDefinitionFieldAudio(fieldView)
                }
                fieldView.ly.tieText.addTextChangedListener { text ->
                    updateFormatButtonUi(fieldView.ly.tieText)
                    val htmlText = Html.toHtml(text, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE).trim()
                    newCardViewModel.updateDefinitionText(
                        id = actualDefinitionFieldModel.definitionId,
                        text = htmlText
                    )
                }
                fieldView.ly.tieText.setOnClickListener {
                    updateFormatButtonUi(fieldView.ly.tieText)
                }
                fieldView.ly.tieText.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        enableFormatButton(true)
                        if (newCardViewModel.hasContentText()) {
                            enableTranslateButton(true)
                        } else {
                            enableTranslateButton(false)
                        }
                        onFieldFocused(
                            fieldViewContainer = fieldView.ly.clContainerField,
                            v = v,
                            hasFocus = true,
                        )
                        newCardViewModel.changeFieldFocus(index)
                    } else {
                        enableFormatButton(false)
                        onFieldFocused(
                            fieldViewContainer = fieldView.ly.clContainerField,
                            v = v,
                            hasFocus = false,
                        )
                        v.clearFocus()
                        enableTranslateButton(false)
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
                            if (card == null && action == Constant.ADD) {
                                deleteImageFromInternalStorage(imageName)
                            }
                        }
                        newCardViewModel.getDefinitionFieldAt(index).definitionAudio?.let { audioModel ->
                            if (card == null && action == Constant.ADD) {
                                deleteAudioFromInternalStorage(audioModel)
                            }
                        }
                        newCardViewModel.deleteDefinitionField(actualDefinitionFieldModel.definitionId)

                    }
                }

                fieldView.ly.btDeleteImage.setOnClickListener {
                    if (card != null && action == Constant.UPDATE) {
                        newCardViewModel.deleteDefinitionImageField(actualDefinitionFieldModel.definitionId)
                        onRemoveDefinitionFieldPhoto(fieldView)
                    } else {
                        newCardViewModel.getDefinitionFieldAt(index).definitionImage?.name?.let { imageName ->
                            if (deleteImageFromInternalStorage(imageName)) {
                                newCardViewModel.deleteDefinitionImageField(actualDefinitionFieldModel.definitionId)
                                onRemoveDefinitionFieldPhoto(fieldView)
                            }
                        }
                    }
                }

                fieldView.ly.lyContentAudio.btPlay.setOnClickListener {
                    newCardViewModel.getDefinitionFieldAt(index).definitionAudio?.let { audioModel ->
                        playPauseDefinitionAudio(fieldView, audioModel)
                    }
                }

                fieldView.ly.lyContentAudio.btDelete.setOnClickListener {
                    if (card != null && action == Constant.UPDATE) {
                        newCardViewModel.deleteDefinitionAudioField(actualDefinitionFieldModel.definitionId)
                        onRemoveDefinitionFieldAudio(definitionFields[index])
                    } else {
                        newCardViewModel.getDefinitionFieldAt(index).definitionAudio?.let { audioModel ->
                            if (deleteAudioFromInternalStorage(audioModel)) {
                                newCardViewModel.deleteDefinitionAudioField(actualDefinitionFieldModel.definitionId)
                                onRemoveDefinitionFieldAudio(definitionFields[index])
                            }
                        }
                    }
                }

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
                        val progress = AppMath().normalize(player.getCurrentPosition(), player.getDuration())
                        fieldView.ly.lyContentAudio.lpiAudioProgression.progress = progress
                        delay(100L)
                    }
                }
            }

            player.hasPlayed() && player.isPlaying() -> {
                fieldView.ly.lyContentAudio.btPlay.setIconResource(R.drawable.icon_play)
                player.pause()
            }

            !player.hasPlayed() && !player.isPlaying() -> {
                fieldView.ly.lyContentAudio.btPlay.setIconResource(R.drawable.icon_pause)
                val audioFile = File(context?.filesDir, audioModel.name)
                player.playFile(audioFile)
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(player.getCurrentPosition(), player.getDuration())
                        fieldView.ly.lyContentAudio.lpiAudioProgression.progress = progress
                        delay(100L)
                    }
                }
                player.onCompletion {
                    fieldView.ly.lyContentAudio.btPlay.setIconResource(R.drawable.icon_play)
                }
            }
        }
    }

    private fun setCardLanguages(card: ExternalCard? = null) {
        val contentLanguage = getContentLanguage(card)
        val definitionLanguage = getDefinitionLanguage(card)
        binding.btContentLanguage.text =
            contentLanguage ?: getString(R.string.text_content_language)
        binding.btDefinitionLanguage.text =
            definitionLanguage ?: getString(R.string.text_definition_language)
    }

    private fun getDefinitionLanguage(card: ExternalCard?) = when {
        !card?.cardDefinitionLanguage.isNullOrBlank() -> card.cardDefinitionLanguage
        !deck.cardDefinitionDefaultLanguage.isNullOrBlank() -> deck.cardDefinitionDefaultLanguage
        else -> null
    }

    private fun getContentLanguage(card: ExternalCard?) = when {
        !card?.cardContentLanguage.isNullOrBlank() -> card.cardContentLanguage
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
            showSnackbar(binding.root, binding.dockedToolbar, getString(R.string.message_card_added))
        }
        initCardAdditionPanel()
        return true
    }

    private fun areThereAnOngoingCardCreation(): Boolean {
        when {
            !binding.lyContent.tieContentText.text.isNullOrBlank() || !binding.lyContent.tieContentText.text.isNullOrEmpty() -> return true
            binding.lyContent.llContentContainerAudio.isVisible -> return true
            binding.lyContent.clContentContainerImage.isVisible -> return true
            else -> {
                definitionFields.forEach {
                    if (!it.ly.tieText.text.isNullOrEmpty() || !it.ly.tieText.text.isNullOrBlank() || it.ly.llContainerAudio.isVisible || it.ly.clContainerImage.isVisible) {
                        return true
                    }
                }
            }
        }
        return false
    }


    private fun generateCardOnUpdate(card: ExternalCardWithContentAndDefinitions): CardWithContentAndDefinitions? {
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

        val contentLanguage = getNewContentLanguage()
        val definitionLanguage = getNewDefinitionLanguage()

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
        val addedContentLanguage = binding.btContentLanguage.text
        return if (addedContentLanguage.toString() !in supportedLanguages) {
            deck.cardContentDefaultLanguage
        } else {
            addedContentLanguage.toString()
        }
    }

    private fun getNewDefinitionLanguage(): String? {
        val addedDefinitionLanguage = binding.btDefinitionLanguage.text
        return if (addedDefinitionLanguage.toString() !in supportedLanguages) {
            deck.cardDefinitionDefaultLanguage
        } else {
            addedDefinitionLanguage.toString()
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
        val contentField = newCardViewModel.contentField.value
        val cardContentText = contentField.contentText
        val cardContentImageName = contentField.contentImage?.name
        val cardContentAudioName = contentField.contentAudio?.name
        val cardContentAudioDuration = contentField.contentAudio?.duration
        return if (
            (cardContentText != null && cardContentText.isNotEmpty() && cardContentText.isNotBlank())||
            cardContentImageName != null ||
            cardContentAudioName != null
            ) {
            newCardViewModel.generateCardContent(
                contentId = contentId,
                imageName = cardContentImageName,
                audioName = cardContentAudioName,
                audioDuration = cardContentAudioDuration,
                cardId = cardId,
                deckId = deckId,
                text = cardContentText
            )
        } else {
            binding.lyContent.llError.visibility = View.VISIBLE
            binding.lyContent.viewError.tvErrorMessage.text = getString(R.string.til_error_card_content)
            null
        }
    }

    private fun isDefinitionError(): Boolean {
        var isText = false
        var isAudio = false
        var isImage = false
        var isTrueAnswer = false

        newCardViewModel.definitionFields.value.forEach { field ->
            if (field.definitionText != null && field.definitionText!!.isNotEmpty() && field.definitionText!!.isNotBlank()) {
                isText = true
            }
            if (field.definitionImage != null) {
                isImage = true
            }
            if (field.definitionAudio != null) {
                isAudio = true
            }
            if ((isText || isImage || isAudio) && field.isCorrectDefinition) {
                isTrueAnswer = true
            }
        }
        // TODO: Show errors
        if (!isText && !isImage && !isAudio) {
            definitionFields.first().ly.llErrorContainer.visibility = View.VISIBLE
            definitionFields.first().ly.viewError.tvErrorMessage.text = getString(R.string.til_error_card_definition)
            return true
        }
        if (!isTrueAnswer) {
            definitionFields.first().ly.llErrorContainer.visibility = View.VISIBLE
            definitionFields.first().ly.viewError.tvErrorMessage.text = getString(R.string.cp_error_correct_definition)
            return true
        }

        return false
    }

    private fun getDefinition(
        cardId: String,
        contentId: String,
        deckId: String
    ): List<CardDefinition>? {
        if(isDefinitionError()) return null
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
            checkRecordAudioPermission()
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
                showSnackbar(binding.root, binding.dockedToolbar, e.message.toString())
            }
        }
    }

    private fun checkRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    private fun onTranslateText(
        selectedFieldPosition: Int?,
    ) {
        if (selectedFieldPosition != null) {

            val definitionLanguage = getNewDefinitionLanguage()
            val contentLanguage = getNewContentLanguage()

            if (selectedFieldPosition < 0) {
                val definitionTexts = newCardViewModel.getDefinitionTexts()
                if (definitionTexts != null) {
                    if (definitionTexts.size > 1) {
                        TranslationOptionsDialog.newInstance(definitionTexts)
                            .show(childFragmentManager, TranslationOptionsDialog.TAG)
                        childFragmentManager.setFragmentResultListener(
                            REQUEST_CODE_TRANSLATION_OPTION, this
                        ) { _, bundle ->
                            val result = bundle.getString(TranslationOptionsDialog.TRANSLATION_OPTIONS_BUNDLE_KEY)
                            result?.let { text ->
                                animProgressBar(binding.lyContent.tilContentText)
                                LanguageUtil().startTranslation(
                                    text = text,
                                    definitionLanguage = contentLanguage,
                                    contentLanguage = definitionLanguage,
                                    onStartTranslation = { translationLanguages ->
                                        translate(
                                            fl = translationLanguages.fl,
                                            tl = translationLanguages.tl,
                                            actualField = binding.lyContent.tieContentText,
                                            ly = binding.lyContent.tilContentText,
                                            text = text,
                                            result = { translation ->
                                                if (translation == null) {
                                                    setEditTextEndIconOnClick(binding.lyContent.tilContentText)
                                                } else {
                                                    val spannable = Html.fromHtml(translation, FROM_HTML_MODE_LEGACY).trim()
                                                    binding.lyContent.tieContentText.setText(spannable)
                                                    binding.lyContent.tieContentText.setSelection(spannable.length)
                                                    setEditTextEndIconOnClick(binding.lyContent.tilContentText)
                                                }
                                            }
                                        )
                                    },
                                    onLanguageDetectionLanguageNotSupported = {
                                        showSnackBar(R.string.error_message_language_not_supported)
                                    },
                                )
                            }
                        }
                    } else {
                        animProgressBar(binding.lyContent.tilContentText)
                        LanguageUtil().startTranslation(
                            text = definitionTexts.first(),
                            definitionLanguage = contentLanguage,
                            contentLanguage = definitionLanguage,
                            onStartTranslation = { translationLanguages ->
                                translate(
                                    fl = translationLanguages.fl,
                                    tl = translationLanguages.tl,
                                    actualField = binding.lyContent.tieContentText,
                                    ly = binding.lyContent.tilContentText,
                                    text = definitionTexts.first(),
                                    result = { translation ->
                                        if (translation == null) {
                                            setEditTextEndIconOnClick(binding.lyContent.tilContentText)
                                        } else {
                                            val spannable = Html.fromHtml(translation, FROM_HTML_MODE_LEGACY).trim()
                                            binding.lyContent.tieContentText.setText(spannable)
                                            binding.lyContent.tieContentText.setSelection(spannable.length)
                                            setEditTextEndIconOnClick(binding.lyContent.tilContentText)
                                        }
                                    }
                                )
                            },
                            onLanguageDetectionLanguageNotSupported = {
                                showSnackBar(R.string.error_message_language_not_supported)
                            },
                        )
                    }
                } else {
                    showSnackbar(binding.root, binding.dockedToolbar, R.string.no_definition_found)
                }
            } else {
                val field = newCardViewModel.getDefinitionFieldAt(selectedFieldPosition)
                val fieldView = definitionFields[selectedFieldPosition]
                val text = newCardViewModel.contentField.value.contentText

                animProgressBar(fieldView.ly.tilText)
                LanguageUtil().startTranslation(
                    text = text!!,
                    definitionLanguage = definitionLanguage,
                    contentLanguage = contentLanguage,
                    onStartTranslation = { translationLanguages ->
                        translate(
                            fl = translationLanguages.fl,
                            tl = translationLanguages.tl,
                            actualField = binding.lyContent.tieContentText,
                            ly = binding.lyContent.tilContentText,
                            text = text,
                            result = { translation ->
                                if (translation == null) {
                                    setEditTextEndIconOnClick(fieldView.ly.tilText)
                                } else {
                                    val spannable = Html.fromHtml(translation, FROM_HTML_MODE_LEGACY).trim()
                                    definitionFields[selectedFieldPosition].ly.tieText.setText(spannable)
                                    definitionFields[selectedFieldPosition].ly.tieText.setSelection(spannable.length)
                                    setEditTextEndIconOnClick(fieldView.ly.tilText)
                                }
                            }
                        )
                    },
                    onLanguageDetectionLanguageNotSupported = {
                        showSnackBar(R.string.error_message_language_not_supported)
                    },
                )
            }

        } else {
            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
        }

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
        text: String,
        result: (String?) -> Unit
    ) {
        val languageUtil = LanguageUtil()
        languageUtil.prepareTranslation(
            context = requireContext(),
            fl = fl,
            tl = tl,
            onDownloadingLanguageMode = {
//                result(getString(R.string.message_translation_downloading_language_model))
//                actualField?.setText(getString(R.string.message_translation_downloading_language_model))
            },
            onMissingCardDefinitionLanguage = {
                setEditTextEndIconOnClick(ly)
//                ly?.error = appContext?.getString(R.string.error_message_no_card_definition_language)
                showSnackBar(R.string.error_message_no_card_definition_language)
            },
            onModelDownloadingFailure = {
//                ly?.error = getString(R.string.error_translation_unknown)
                result(null)
                showSnackBar(R.string.error_translation_failed)
            },
            onSuccess = { translatorModel ->
                languageUtil.translate(
                    appTranslator = translatorModel.appTranslator,
                    conditions = translatorModel.condition,
                    text = text,
                    onSuccess = { translation ->
                        result(translation)
//                        actualField?.setText(translation)
//                        setEditTextEndIconOnClick(ly)
                    },
                    onTranslationFailure = { exception ->
//                        ly?.error = exception.toString()
//                        setEditTextEndIconOnClick(ly)
                        result(null)
                        showSnackBar(R.string.error_translation_failed)
                    },
                    onModelDownloadingFailure = {
//                        setEditTextEndIconOnClick(ly)
//                        ly?.error = getString(R.string.error_translation_language_model_not_downloaded)
                        result(null)
                        showSnackBar(R.string.error_translation_language_model_not_downloaded)
                    },
                )
            },
            onNoInternet = {
//                setEditTextEndIconOnClick(ly)
//                ly?.error = getString(R.string.error_translation_no_internet)
                result(null)
                showSnackBar(R.string.error_translation_no_internet)
            },
            onInternetViaCellular = { translatorModel ->
                MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_App_MaterialAlertDialog
                )
                    .setTitle(getString(R.string.title_no_wifi))
                    .setMessage(getString(R.string.message_no_wifi))
                    .setNegativeButton(getString(R.string.option2_no_wifi)) { dialog, _ ->
//                        setEditTextEndIconOnClick(ly)
//                        actualField?.text?.clear()
                        result(null)
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.option1_no_wifi)) { dialog, _ ->
                        languageUtil.translate(
                            appTranslator = translatorModel.appTranslator,
                            conditions = translatorModel.condition,
                            text = text,
                            onSuccess = { translation ->
                                result(translation)
//                                actualField?.setText(translation)
//                                setEditTextEndIconOnClick(ly)
                            },
                            onTranslationFailure = { exception ->
//                                ly?.error = exception.toString()
//                                setEditTextEndIconOnClick(ly)
                                result(null)
                                showSnackBar(R.string.error_translation_failed)
                            },
                            onModelDownloadingFailure = {
//                                setEditTextEndIconOnClick(ly)
//                                ly?.error = getString(R.string.error_translation_language_model_not_downloaded)
                                result(null)
                                showSnackBar(R.string.error_translation_language_model_not_downloaded)
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
        if (newCardViewModel.getDefinitionFieldCount() < 10) {
            newCardViewModel.addDefinitionField(null)
        } else {
            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_max_10_definitions)
        }
    }

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
                        onSetContentFieldAudio(newAudio.duration)
                    } else {
                        newCardViewModel.updateDefinitionAudio(
                            id = newCardViewModel.getDefinitionFieldAt(selectedFieldPosition).definitionId,
                            audio = newAudio
                        )
                        onSetDefinitionFieldAudio(definitionFields[selectedFieldPosition], newAudio.duration)
                    }
                }
            }
        } else {
            showSnackbar(binding.root, binding.dockedToolbar, R.string.error_message_no_field_selected)
        }

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
            appContext!!.deleteFile(audio.name)
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackbar(binding.root, binding.dockedToolbar, R.string.could_not_delete_audio)
            false
        }
    }

    fun onSetDefinitionFieldPhoto(actualField: FieldModel, imageBitmap: Bitmap?) {
        actualField.ly.clContainerImage.visibility = View.VISIBLE
        actualField.ly.imgPhoto.setImageBitmap(imageBitmap)
    }

    fun onSetDefinitionFieldAudio(actualField: FieldModel, duration: String) {
        actualField.ly.lyContentAudio.tvLength.text = duration
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

    fun onSetContentFieldAudio(duration: String) {
        binding.lyContent.lyContentAudio.tvLength.text = duration
        binding.lyContent.llContentContainerAudio.visibility = View.VISIBLE
    }

    private fun setFieldText(text: String, fieldIndex: Int) {
        if (fieldIndex == -1) {
            newCardViewModel.updateContentField(
                updatedContentField = newCardViewModel.contentField.value.copy(contentText = text)
            )
        } else {
            newCardViewModel.updateDefinitionText(
                id = newCardViewModel.getDefinitionFieldAt(fieldIndex).definitionId,
                text = text
            )
            definitionFields[fieldIndex].ly.tieText.setText(text)
        }
    }

}