package com.ssoaharison.recall.card

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.databinding.FragmentCardBinding
import com.ssoaharison.recall.deck.NewDeckDialog
import com.ssoaharison.recall.quiz.quizGame.QuizGameActivity
import com.ssoaharison.recall.util.Constant
import com.ssoaharison.recall.util.LanguageUtil
import com.ssoaharison.recall.util.FlashCardMiniGameRef.QUIZ
import com.ssoaharison.recall.util.ItemLayoutManager.LAYOUT_MANAGER
import com.ssoaharison.recall.util.ItemLayoutManager.LINEAR_LAYOUT_MANAGER
import com.ssoaharison.recall.util.ItemLayoutManager.STAGGERED_GRID_LAYOUT_MANAGER
import com.ssoaharison.recall.util.QuizModeBottomSheet
import com.ssoaharison.recall.util.UiState
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.entities.relations.CardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.ssoaharison.recall.deck.DeckFragment.Companion.REQUEST_CODE
import com.ssoaharison.recall.deck.OnSaveDeckWithCationModel
import com.ssoaharison.recall.helper.playback.AndroidAudioPlayer
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextType.DEFINITION
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.parcelable
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale


class CardFragment :
    Fragment(),
//    MenuProvider,
    TextToSpeech.OnInitListener {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private lateinit var recyclerViewAdapter: CardsRecyclerViewAdapter
    private lateinit var subdeckRecyclerViewAdapter: SubdeckRecyclerViewAdapter
    private lateinit var recyclerViewAdapterDeckPath: RecyclerViewAdapterDeckPath
    private var tts: TextToSpeech? = null
    private var startingQuizJob: Job? = null
    private var displayingCardsJob: Job? = null
    private var displaySubdeckJob: Job? = null
    private val supportedLanguages = LanguageUtil().getSupportedLang()
    private lateinit var arrayAdapterSupportedLanguages: ArrayAdapter<String>
    private lateinit var listPopupWindow: ListPopupWindow
    var deckExportModel: DeckExportModel? = null

//    private lateinit var deckColorPickerAdapter: DeckColorPickerAdapter

//    private lateinit var appThemeName: String

    private val player by lazy {
        AndroidAudioPlayer(requireContext())
    }

    private val cardViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, CardViewModelFactory(repository))[CardViewModel::class.java]
    }

    private var createFile = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null && deckExportModel != null) {
                cardsToTextToUri(uri, deckExportModel?.separator!!)
            }
        }

//    @SuppressLint("RestrictedApi")
//    private var item: ActionMenuItemView? = null
//    val args: CardFragmentArgs by navArgs()
//    private lateinit var deck: ExternalDeck
//    private lateinit var opener: String

    private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        const val TAG = "CardFragment"
        const val REQUEST_CODE_CARD = "0"
        const val REQUEST_CODE_QUIZ_MODE = "300"
        const val REQUEST_EXPORT_DECK_CODE = "400"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        appContext = container?.context
        tts = TextToSpeech(appContext, this)
        return binding.root
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val inflater = super.onGetLayoutInflater(savedInstanceState)
        var contextThemeWrapper: Context? = null
//        deck = args.selectedDeck
        val themePicker = ThemePicker()
        val window = activity?.window
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appThemeName = sharedPref?.getString("themName", "WHITE THEM")
        val appTheme = themePicker.selectTheme(appThemeName)

//        if (!deck.deckColorCode.isNullOrBlank()) {
//            val deckTheme: Int
//            val deckColorSurfaceLow: Int
//            if (appThemeName == DARK_THEME) {
//                deckTheme = themePicker.selectDarkThemeByDeckColorCode(
//                    deck.deckColorCode!!,
//                    themePicker.getDefaultTheme()
//                )
//                deckColorSurfaceLow =
//                    DeckColorCategorySelector().selectDeckDarkColorSurfaceContainerLow(
//                        requireContext(),
//                        deck.deckColorCode
//                    )
//            } else {
//                deckTheme = themePicker.selectThemeByDeckColorCode(
//                    deck.deckColorCode!!,
//                    themePicker.getDefaultTheme()
//                )
//                deckColorSurfaceLow =
//                    DeckColorCategorySelector().selectDeckColorSurfaceContainerLow(
//                        requireContext(),
//                        deck.deckColorCode
//                    )
//            }
//            contextThemeWrapper = ContextThemeWrapper(requireContext(), deckTheme)
//            window?.statusBarColor = deckColorSurfaceLow
//        } else {
//            contextThemeWrapper = ContextThemeWrapper(requireContext(), appTheme!!)
//        }
        contextThemeWrapper = ContextThemeWrapper(requireContext(), appTheme!!)
        return inflater.cloneInContext(contextThemeWrapper)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
//        (activity as AppCompatActivity).setSupportActionBar(binding.cardsTopAppBar)

//        deck = args.selectedDeck
//        opener = args.opener



        staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        linearLayoutManager = LinearLayoutManager(appContext)
        arrayAdapterSupportedLanguages = ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                 cardViewModel.getMainDeck()?.let { mainDeck ->
                     displayData(mainDeck)
                 }
            }
        }

//        view.findViewById<MaterialToolbar>(R.id.cardsTopAppBar).apply {
//            title = deck.deckName
//            setNavigationOnClickListener {
//                findNavController().navigate(
//                    R.id.deckFragment,
//                    null,
//                    NavOptions.Builder()
//                        .setPopUpTo(R.id.cardFragment, true)
//                        .build()
//                )
//            }
//
//            binding.bottomAppBar.apply {
//                setNavigationOnClickListener {
//                    onChooseQuizMode(deck)
//                }
//                setOnMenuItemClickListener { menuItem ->
//                    when (menuItem.itemId) {
////                        R.id.bt_flash_card_game -> {
////                            onStartQuiz { deckWithCards ->
////                                lunchQuiz(deckWithCards, FLASH_CARD_QUIZ)
////                            }
////                            true
////                        }
//
//                        R.id.bt_quiz -> {
//                            onStartQuiz { deckWithCards ->
//                                lunchQuiz(deckWithCards, QUIZ)
//                            }
//                            true
//                        }
//
////                        R.id.bt_test -> {
////                            onStartQuiz { deckWithCards ->
////                                lunchQuiz(deckWithCards, TEST)
////                            }
////                            true
////                        }
//
//                        else -> false
//                    }
//                }
//            }
//        }

//        initDeckColorPicker()
//
//        binding.btDeckDetails.setOnClickListener {
//            showDeckDetails()
//        }
//
//        binding.btExport.setOnClickListener {
//            showExportDeckDialog()
//
//        }

//        if (opener == NewDeckDialog.TAG) {
//            onAddNewCard()
//        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
//                    lifecycleScope.launch {
//                        repeatOnLifecycle(Lifecycle.State.STARTED) {
//                            cardViewModel.getMainDeck()?.let { mainDeck ->
//                                displayData(mainDeck)
//                            }
//                        }
//                    }
                    onNavigateBack()

//                    findNavController().navigate(
//                        R.id.deckFragment,
//                        null,
//                        NavOptions.Builder()
//                            .setPopUpTo(R.id.cardFragment, true)
//                            .build()
//                    )
                }
            })

        binding.fabAddShowActions.setOnClickListener {
            onAction(!binding.btAddSubdeck.isVisible)
        }

    }

    private fun onNavigateBack() {
        val path = cardViewModel.deckPath()
        if (path.size > 1) {
            displayData(path[1])
        } else {
            Toast.makeText(
                appContext,
                getString(R.string.error_message_can_not_go_further),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onAction(areActionsShown: Boolean) {
        binding.btAddSubdeck.isVisible = areActionsShown
        binding.btAddCard.isVisible = areActionsShown
        if (areActionsShown) {
            binding.fabAddShowActions.setImageResource(R.drawable.icon_exit)
        } else {
            binding.fabAddShowActions.setImageResource(R.drawable.icon_add)
        }
    }

//    private fun displayData() {
//        displayAllCards()
//        displaySubdecks()
//    }

    private fun displayData(deck: ExternalDeck) {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getDeckPath(deck) { path ->
                    // TODO: Populate deck path
                    recyclerViewAdapterDeckPath = RecyclerViewAdapterDeckPath(path) { pathTogo ->
                        displayData(pathTogo)
                    }
                    binding.rvDeckPath.apply {
                        adapter = recyclerViewAdapterDeckPath
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(appContext, RecyclerView.HORIZONTAL, true)
                    }
                }
            }
        }

        displayAllCards(deck.deckId)
        displaySubdecks(deck)

        binding.cardsTopAppBar.apply {
            title = null
            setNavigationOnClickListener {

//                lifecycleScope.launch {
//                    repeatOnLifecycle(Lifecycle.State.STARTED) {
//                        cardViewModel.getMainDeck()?.let { mainDeck ->
//                            displayData(mainDeck)
//                        }
//                    }
//                }

//                onNavigateBack()
                activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()

//                findNavController().navigate(
//                    R.id.deckFragment,
//                    null,
//                    NavOptions.Builder()
//                        .setPopUpTo(R.id.cardFragment, true)
//                        .build()
//                )
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.search_deck_menu -> {
                        SearchDialog(deck).show(
                            childFragmentManager,
                            "Search dialog"
                        )
                        true
                    }
                    else -> false
                }
            }
        }
        binding.bottomAppBar.apply {
            setNavigationOnClickListener {
                onChooseQuizMode(deck)
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
//                        R.id.bt_flash_card_game -> {
//                            onStartQuiz { deckWithCards ->
//                                lunchQuiz(deckWithCards, FLASH_CARD_QUIZ)
//                            }
//                            true
//                        }

                    R.id.bt_quiz -> {
                        onStartQuiz(deck) { deckWithCards ->
                            lunchQuiz(deckWithCards, QUIZ)
                        }
                        true
                    }

//                        R.id.bt_test -> {
//                            onStartQuiz { deckWithCards ->
//                                lunchQuiz(deckWithCards, TEST)
//                            }
//                            true
//                        }

                    else -> false
                }
            }
        }

//        binding.fabAddCard.setOnClickListener {
//            onAddNewCard()
//        }

        binding.btAddCard.setOnClickListener {
            onAddNewCard(deck)
        }

        binding.btAddSubdeck.setOnClickListener {
            onAddNewDeck(deck)
        }

    }

//    private fun showExportDeckDialog() {
//        val exportDeckDialog = ExportDeckDialog()
//        exportDeckDialog.show(childFragmentManager, "Export deck dialog")
//        childFragmentManager.setFragmentResultListener(
//            REQUEST_EXPORT_DECK_CODE,
//            this
//        ) { _, bundle ->
//            deckExportModel = bundle.parcelable<DeckExportModel>(ExportDeckDialog.EXPORT_DECK_BUNDLE_KEY)
//            createFile.launch("${deck.deckName}${deckExportModel?.format}")
//        }
//    }

//    private fun showDeckDetails() {
//        binding.tilDeckName.isVisible = !binding.tilDeckName.isVisible
//        binding.tvDeckColorPickerTitle.isVisible = !binding.tvDeckColorPickerTitle.isVisible
//        binding.rvDeckColorPicker.isVisible = !binding.rvDeckColorPicker.isVisible
//        binding.btExport.isVisible = !binding.btExport.isVisible
//        if (binding.tilDeckName.isVisible) {
//            binding.btDeckDetails.setIconResource(R.drawable.icon_expand_less)
//            lifecycleScope.launch {
//                delay(50)
//                cardViewModel.colorSelectionList.collect { listOfColors ->
//                    displayColorPicker(listOfColors)
//                }
//            }
//        } else {
//            binding.btDeckDetails.setIconResource(R.drawable.icon_expand_more)
//        }
//    }

//    private fun initDeckColorPicker() {
//        val deckColorCategorySelector = DeckColorCategorySelector()
//        val deckColors = deckColorCategorySelector.getColors()
//        cardViewModel.initColorSelection(deckColors, deck.deckColorCode)
//    }
//
//    private fun displayColorPicker(colorList: List<ColorModel>) {
//        deckColorPickerAdapter = DeckColorPickerAdapter(
//            requireContext(),
//            colorList
//        ) { selectedColor ->
//            cardViewModel.selectColor(selectedColor.id)
//            deckColorPickerAdapter.notifyDataSetChanged()
//            val newDeck = deck.copy(deckColorCode = selectedColor.id)
//            cardViewModel.updateDeck(newDeck.toLocal())
//        }
//        binding.rvDeckColorPicker.apply {
//            adapter = deckColorPickerAdapter
//            layoutManager = GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false)
//            setHasFixedSize(true)
//        }
//
//    }

//    private fun bindDeckDetailsPanel(deck: ExternalDeck) {
//        binding.tvCardSum.text = "${deck.cardCount}"
//        binding.tvUnknownCardSum.text = "${deck.unKnownCardCount}"
//        binding.tvKnownCardSum.text = "${deck.knownCardCount}"
//        binding.btContentLanguage.apply {
//            text = if (deck.cardContentDefaultLanguage.isNullOrBlank()) context.getString(R.string.text_content_language) else deck.cardContentDefaultLanguage
//            setOnClickListener {
//                onDeckLanguageClicked(binding.rlContainerContentLanguage) { selectedLanguage ->
//                    cardViewModel.updateDefaultCardContentLanguage(
//                        deck.deckId,
//                        selectedLanguage
//                    )
//                }
//            }
//        }
//        binding.rlContainerContentLanguage.setOnClickListener {
//            onDeckLanguageClicked(binding.rlContainerContentLanguage) { selectedLanguage ->
//                cardViewModel.updateDefaultCardContentLanguage(
//                    deck.deckId,
//                    selectedLanguage
//                )
//            }
//        }
//        binding.btDefinitionLanguage.apply {
//            text = if (deck.cardDefinitionDefaultLanguage.isNullOrBlank()) context.getString(R.string.text_definition_language) else deck.cardDefinitionDefaultLanguage
//            setOnClickListener {
//                onDeckLanguageClicked(binding.rlContainerDefinitionLanguage) { selectedLanguage ->
//                    cardViewModel.updateDefaultCardDefinitionLanguage(
//                        deck.deckId,
//                        selectedLanguage
//                    )
//                }
//            }
//        }
//        binding.rlContainerDefinitionLanguage.setOnClickListener {
//            onDeckLanguageClicked(binding.rlContainerDefinitionLanguage) { selectedLanguage ->
//                cardViewModel.updateDefaultCardDefinitionLanguage(
//                    deck.deckId,
//                    selectedLanguage
//                )
//            }
//        }
//    }

//    private fun displayAllCards() {
//        displayingCardsJob?.cancel()
//        displayingCardsJob = lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                cardViewModel.getDeckWithCards(deck.deckId)
//                cardViewModel.deckWithAllCards.collect { state ->
//                    when (state) {
//                        is UiState.Loading -> {
//                            binding.tvNoCardFound.visibility = View.GONE
//                            binding.cardsActivityProgressBar.isVisible = true
//                        }
//
//                        is UiState.Error -> {
//                            onDeckEmpty()
//                        }
//
//                        is UiState.Success -> {
//                            deck = state.data.deck
//                            populateRecyclerView(state.data.cards, state.data.deck)
//                            bindDeckDetailsPanel(state.data.deck)
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun displayAllCards(deckId: String) {
        displayingCardsJob?.cancel()
        displayingCardsJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getDeckWithCards(deckId, appContext!!)
                cardViewModel.deckWithAllCards.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.tvNoCardFound.visibility = View.GONE
                            binding.cardsActivityProgressBar.isVisible = true
                        }

                        is UiState.Error -> {
                            onDeckEmpty()
                        }

                        is UiState.Success -> {
//                            deck = state.data.deck
                            populateRecyclerView(state.data.cards, state.data.deck)
//                            bindDeckDetailsPanel(state.data.deck)
                        }
                    }
                }
            }
        }
    }

//    private fun displaySubdecks() {
//        displaySubdeckJob?.cancel()
//        displaySubdeckJob = lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                cardViewModel.getSubdecks(deck.deckId)
//                cardViewModel.subdecks.collect { state ->
//                    when(state) {
//                        is UiState.Error -> {
//                            // TODO: On no deck
//                            binding.subdeckRecyclerView.isVisible = false
//                        }
//                        is UiState.Loading -> {
//                            // TODO: On loading
//                            val b = 2
//                        }
//                        is UiState.Success -> {
//                            populateSubdecksRecyclerView(state.data)
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun displaySubdecks(deck: ExternalDeck) {
        displaySubdeckJob?.cancel()
        displaySubdeckJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getSubdecks(deck.deckId)
                cardViewModel.subdecks.collect { state ->
                    when(state) {
                        is UiState.Error -> {
                            // TODO: On no deck
                            binding.subdeckRecyclerView.isVisible = false
                        }
                        is UiState.Loading -> {
                            // TODO: On loading
                            val b = 2
                        }
                        is UiState.Success -> {
                            populateSubdecksRecyclerView(deck = deck, subdecks = state.data)
                        }
                    }
                }
            }
        }
    }

    private fun populateSubdecksRecyclerView(
        deck: ExternalDeck,
        subdecks: List<ExternalDeck>
    ) {
        subdeckRecyclerViewAdapter = SubdeckRecyclerViewAdapter(subdecks, appContext!!, "WHITE THEM", {
            onEditDeck(parentDeck = deck, deckToEdit = it)
        }, {
            onDeleteSubdeck(it)
        }, {
            // TODO: Start Quiz
        }) {
//            deck = it
//            displayData()
            displayData(it)
        }
        binding.subdeckRecyclerView.apply {
            isVisible = true
            adapter = subdeckRecyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(appContext)
        }
    }

    private fun onEditDeck(
        parentDeck: ExternalDeck,
        deckToEdit: ExternalDeck?
    ) {
        val newDeckDialog = NewDeckDialog(
            deckToEdit = deckToEdit,
            parentDeckId = parentDeck.deckId,
            appTheme = getAppTheme()
        )
        newDeckDialog.show(childFragmentManager, "Edit Deck Dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { requestQuey, bundle ->
            val result = bundle.parcelable<Deck>(NewDeckDialog.EDIT_DECK_BUNDLE_KEY)
            result?.let {
                cardViewModel.updateDeck(it)
            }
        }
    }

    private fun onAddNewDeck(parentDeck: ExternalDeck) {
        onAction(false)
        val newDeckDialog = NewDeckDialog(
            deckToEdit = null,
            parentDeckId = parentDeck.deckId,
            appTheme = getAppTheme()
        )
        newDeckDialog.show(childFragmentManager, "New Deck Dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { _, bundle ->
            val result = bundle.parcelable<OnSaveDeckWithCationModel>(NewDeckDialog.SAVE_DECK_BUNDLE_KEY)
            result?.let {
                cardViewModel.insertSubdeck(it.deck)
            }
        }
    }

    private fun onDeleteSubdeck(deck: ExternalDeck) {
        if (deck.cardCount!! > 0) {
            appContext?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle(getString(R.string.dialog_title_delete_deck))
                    .setMessage(
                        if (deck.cardCount > 1) {
                            getString(
                                R.string.dialog_message_delete_deck,
                                deck.cardCount.toString(),
                                "s"
                            )
                        } else {
                            getString(
                                R.string.dialog_message_delete_deck,
                                deck.cardCount.toString(),
                                ""
                            )
                        }
                    )
                    .setNegativeButton(getString(R.string.bt_text_cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.bt_text_delete)) { dialog, _ ->
                        cardViewModel.deleteSubdeck(deck)
                        dialog.dismiss()
                        Toast.makeText(it, "Delete ${deck.deckName}", Toast.LENGTH_LONG).show()
                    }
                    .show()
            }
        } else {
            cardViewModel.deleteSubdeck(deck)
            Toast.makeText(requireContext(), "Delete ${deck.deckName}", Toast.LENGTH_LONG).show()
        }
    }

    private fun onDeckEmpty() {
        binding.cardsActivityProgressBar.isVisible = false
        binding.cardRecyclerView.isVisible = false
        binding.tvNoCardFound.isVisible = false
        binding.onNoCardTextError.isVisible = true
    }

    private fun onChooseQuizMode(deck: ExternalDeck) {
        val quizMode = QuizModeBottomSheet(deck)
        quizMode.show(childFragmentManager, "Quiz Mode")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_QUIZ_MODE,
            this
        ) { _, bundle ->
            val result = bundle.getString(QuizModeBottomSheet.START_QUIZ_BUNDLE_KEY)
            result?.let {
                onStartQuiz(deck) { deckWithCards ->
                    lunchQuiz(deckWithCards, it)
                }
            }
        }
    }

    private fun onStartQuiz(deck: ExternalDeck, start: (ExternalDeckWithCardsAndContentAndDefinitions) -> Unit) {
        startingQuizJob?.cancel()
        startingQuizJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getDeckWithCards(deck.deckId, appContext!!)
                cardViewModel.deckWithAllCards.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.cardsActivityProgressBar.isVisible = true
                        }

                        is UiState.Error -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            onStartingQuizError(deck,getString(R.string.error_message_empty_deck))
                            this@launch.cancel()
                            this.cancel()
                        }

                        is UiState.Success -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            start(state.data)
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }

    }

    private fun lunchQuiz(deckWithCards: ExternalDeckWithCardsAndContentAndDefinitions, quizMode: String) {
        when (quizMode) {
//            FLASH_CARD_QUIZ -> {
//                val intent = Intent(appContext, FlashCardGameActivity::class.java)
//                intent.putExtra(FlashCardGameActivity.DECK_ID_KEY, deckWithCards)
//                startActivity(intent)
//            }
//
//            MULTIPLE_CHOICE_QUIZ -> {
//                if (deckWithCards.cards?.size!! >= MIN_CARD_FOR_MULTI_CHOICE_QUIZ) {
//                    val intent = Intent(appContext, MultiChoiceQuizGameActivity::class.java)
//                    intent.putExtra(MultiChoiceQuizGameActivity.DECK_ID_KEY, deckWithCards)
//                    startActivity(intent)
//                } else {
//                    onStartingQuizError(
//                        getString(
//                            R.string.error_message_starting_quiz,
//                            "$MIN_CARD_FOR_MULTI_CHOICE_QUIZ"
//                        )
//                    )
//                }
//            }
//
//            WRITING_QUIZ -> {
//                val intent = Intent(appContext, WritingQuizGameActivity::class.java)
//                intent.putExtra(WritingQuizGameActivity.DECK_ID_KEY, deckWithCards)
//                startActivity(intent)
//            }
//
//            MATCHING_QUIZ -> {
//                if (deckWithCards.cards?.size!! >= MIN_CARD_FOR_MATCHING_QUIZ) {
//                    val intent = Intent(appContext, MatchQuizGameActivity::class.java)
//                    intent.putExtra(MatchQuizGameActivity.DECK_ID_KEY, deckWithCards)
//                    startActivity(intent)
//                } else {
//                    onStartingQuizError(
//                        getString(
//                            R.string.error_message_starting_quiz,
//                            "$MIN_CARD_FOR_MATCHING_QUIZ"
//                        )
//                    )
//                }
//            }

            QUIZ -> {
                val intent = Intent(appContext, QuizGameActivity::class.java)
                intent.putExtra(QuizGameActivity.DECK_ID_KEY, deckWithCards)
                startActivity(intent)
            }

//            TEST -> {
//                if (deckWithCards.cards?.size!! >= MIN_CARD_FOR_TEST) {
//                    val intent = Intent(appContext, TestActivity::class.java)
//                    intent.putExtra(TestActivity.DECK_ID_KEY, deckWithCards)
//                    startActivity(intent)
//                } else {
//                    onStartingQuizError(
//                        getString(
//                            R.string.error_message_starting_quiz,
//                            "$MIN_CARD_FOR_TEST"
//                        )
//                    )
//                }
//            }
        }
    }

//    @SuppressLint("RestrictedApi")
//    private fun populateRecyclerView(cardList: List<ExternalCardWithContentAndDefinitions>, deck: ExternalDeck) {
//        item = binding.cardsTopAppBar.findViewById(R.id.view_deck_menu)
//        val pref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
//        val appTheme = pref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
//        binding.cardsActivityProgressBar.isVisible = false
//        binding.onNoCardTextError.isVisible = false
//        binding.tvNoCardFound.isVisible = false
//        binding.cardRecyclerView.isVisible = true
//        recyclerViewAdapter = CardsRecyclerViewAdapter(
//            appContext!!,
//            appTheme,
//            deck,
//            cardList,
//            cardViewModel.getBoxLevels()!!,
//            { selectedCard ->
//                onEditCard(selectedCard!!)
//            },
//            { selectedCard ->
//                cardViewModel.deleteCard(selectedCard!!)
//            },
//            { text ->
//                if (tts?.isSpeaking == true) {
//                    stopReading(text)
//                } else {
//                    onStartReadingText(text, text.text.language, text.type)
//                }
//            },
//            { text ->
//                if (tts?.isSpeaking == true) {
//                    stopReading(text)
//                } else {
//                    onStartReadingText(text, text.text.language, text.type)
//                }
//            })
//
//        binding.cardRecyclerView.apply {
//            adapter = recyclerViewAdapter
//            setHasFixedSize(true)
//            layoutManager =
//                if (this@CardFragment.getLayoutManager() == STAGGERED_GRID_LAYOUT_MANAGER) {
//                    item?.setIcon(
//                        ContextCompat.getDrawable(
//                            requireContext(),
//                            R.drawable.icon_grid_view
//                        )
//                    )
//                    staggeredGridLayoutManager
//                } else {
//                    item?.setIcon(
//                        ContextCompat.getDrawable(
//                            requireContext(),
//                            R.drawable.icon_view_agenda
//                        )
//                    )
//                    linearLayoutManager
//                }
//        }
//
//    }

    @SuppressLint("RestrictedApi")
    private fun populateRecyclerView(cardList: List<ExternalCardWithContentAndDefinitions>, deck: ExternalDeck) {
//        item = binding.cardsTopAppBar.findViewById(R.id.view_deck_menu)
        val pref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appTheme = pref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
        binding.cardsActivityProgressBar.isVisible = false
        binding.onNoCardTextError.isVisible = false
        binding.tvNoCardFound.isVisible = false
        binding.cardRecyclerView.isVisible = true
        recyclerViewAdapter = CardsRecyclerViewAdapter(
            appContext!!,
            appTheme,
            deck,
            cardList,
            cardViewModel.getBoxLevels()!!,
            { selectedCard ->
                onEditCard(deck = deck, card = selectedCard)
            },
            { selectedCard ->
                cardViewModel.deleteCard(selectedCard, appContext!!)
            },
            { text ->
                if (tts?.isSpeaking == true) {
                    stopReading(text)
                } else {
                    onStartReadingText(
                        deck = deck,
                        textAndView = text,
                        language = text.text.language,
                        type = text.type)
                }
            },
            { text ->
                if (tts?.isSpeaking == true) {
                    stopReading(text)
                } else {
                    onStartReadingText(
                        deck = deck,
                        textAndView = text,
                        language = text.text.language,
                        type = text.type
                    )
                }
            }, { audio, view ->
                lifecycleScope.launch {

                    when {
                        player.hasPlayed() &&
                    }

                    if (!player.isPlaying()) {
                        // TODO: Update
                        val audioFile = File(context?.filesDir, audio.name)
                        player.playFile(audioFile)
                    } else {
                        player.stop()
                    }
                }
            })

        binding.cardRecyclerView.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager =
                if (this@CardFragment.getLayoutManager() == STAGGERED_GRID_LAYOUT_MANAGER) {
//                    item?.setIcon(
//                        ContextCompat.getDrawable(
//                            requireContext(),
//                            R.drawable.icon_grid_view
//                        )
//                    )
                    staggeredGridLayoutManager
                } else {
//                    item?.setIcon(
//                        ContextCompat.getDrawable(
//                            requireContext(),
//                            R.drawable.icon_view_agenda
//                        )
//                    )
                    linearLayoutManager
                }
        }

    }

    private fun onDeckLanguageClicked(
        anchor: RelativeLayout,
        setLanguage: (String) -> Unit
    ) {
        listPopupWindow = ListPopupWindow(appContext!!, null)
        listPopupWindow.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.filter_spinner_dropdown_background,
                requireActivity().theme
            )
        )
        listPopupWindow.apply {
            anchorView = anchor
            setAdapter(arrayAdapterSupportedLanguages)
            setOnItemClickListener { _, _, position, _ ->
                setLanguage(supportedLanguages[position])
                dismiss()
            }
            show()
        }
    }

    private fun onStartingQuizError(
        deck: ExternalDeck,
        errorText: String
    ) {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(getString(R.string.error_title_starting_quiz))
            .setMessage(errorText)
            .setNegativeButton(R.string.bt_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.bt_add_card)) { dialog, _ ->
                onAddNewCard(deck)
                dialog.dismiss()
            }
            .show()
    }

    private fun onStartReadingText(
        deck: ExternalDeck,
        textAndView: TextClickedModel,
        language: String?,
        type: String
    ) {
        if (language.isNullOrBlank()) {
            val languageUtil = LanguageUtil()
            languageUtil.detectLanguage(
                text = textAndView.text.toString(),
                onError = { showSnackBar(R.string.error_message_error_while_detecting_language) },
                onLanguageUnIdentified = { showSnackBar(R.string.error_message_can_not_identify_language) },
                onLanguageNotSupported = { showSnackBar(R.string.error_message_language_not_supported) },
                onSuccess = { detectedLanguage ->
                    when (type) {
                        CONTENT -> cardViewModel.updateCardContentLanguage(
                            deck.deckId,
                            detectedLanguage
                        )

                        DEFINITION -> cardViewModel.updateCardDefinitionLanguage(
                            deck.deckId,
                            detectedLanguage
                        )
                    }
                    readeText(textAndView, detectedLanguage)
                }
            )
        } else {
            readeText(textAndView, language)
        }

    }

    private fun showSnackBar(
        @StringRes messageRes: Int
    ) {
        Snackbar.make(
            binding.cardsActivityRoot,
            getString(messageRes),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun readeText(
        textAndView: TextClickedModel,
        language: String
    ) {
        val text = textAndView.text.text
        val view = textAndView.view
        val textColor = view.textColors
        val onReadColor = MaterialColors.getColor(
            appContext!!,
            androidx.appcompat.R.attr.colorPrimary,
            Color.GRAY
        )

        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                view.setTextColor(onReadColor)
            }

            override fun onDone(utteranceId: String?) {
                view.setTextColor(textColor)
            }

            override fun onError(utteranceId: String?) {
            }
        }

        val params = Bundle()

        tts?.language = Locale.forLanguageTag(
            LanguageUtil().getLanguageCodeForTextToSpeech(language)!!
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts?.speak(text, TextToSpeech.QUEUE_ADD, params, "UniqueID")

        tts?.setOnUtteranceProgressListener(speechListener)
    }

    private fun stopReading(textAndView: TextClickedModel) {
        textAndView.view.setTextColor(requireContext().getColor(textAndView.textColor))
        tts?.stop()
    }

    private fun onAddNewCard(deck: ExternalDeck) {
        onAction(false)
        val newCardDialog = NewCardDialog(null, deck, Constant.ADD)
        newCardDialog.show(childFragmentManager, "New Card Dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CARD,
            this
        ) { _, bundle ->
            val result = bundle.getInt(NewCardDialog.SAVE_CARDS_BUNDLE_KEY)
            when {
                result > 1 -> {
                    Toast.makeText(
                        appContext,
                        getString(R.string.message_new_cards_added, "$result"),
                        Toast.LENGTH_LONG
                    ).show()
                }

                result == 1 -> {
                    Toast.makeText(
                        appContext,
                        getString(R.string.message_new_card_added),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun onEditCard(
        deck: ExternalDeck,
        card: ExternalCardWithContentAndDefinitions
    ) {
        val newCardDialog = NewCardDialog(card, deck, Constant.UPDATE)
        newCardDialog.show(childFragmentManager, "New Card Dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CARD,
            this
        ) { _, bundle ->
            val result = bundle.parcelable<CardWithContentAndDefinitions>(NewCardDialog.EDIT_CARD_BUNDLE_KEY)
            result?.let {
                cardViewModel.updateCard(it)
                Toast.makeText(
                    appContext,
                    getString(R.string.message_card_updated),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

//    @SuppressLint("RestrictedApi")
//    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//        menuInflater.inflate(R.menu.card_fragment_menu, menu)
//        val search = menu.findItem(R.id.search_deck_menu)
//        val searchView = search?.actionView as SearchView
//
//        val searchIcon: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_button)
//        searchIcon.setColorFilter(
//            ThemeUtils.getThemeAttrColor(
//                requireContext(),
//                com.google.android.material.R.attr.colorOnSurface
//            ), PorterDuff.Mode.SRC_IN
//        )
//
//        val searchIconClose: ImageView =
//            searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
//        searchIconClose.setColorFilter(
//            ThemeUtils.getThemeAttrColor(
//                requireContext(),
//                com.google.android.material.R.attr.colorOnSurface
//            ), PorterDuff.Mode.SRC_IN
//        )
//
//        val searchIconMag: ImageView =
//            searchView.findViewById(androidx.appcompat.R.id.search_go_btn)
//        searchIconMag.setColorFilter(
//            ThemeUtils.getThemeAttrColor(
//                requireContext(),
//                com.google.android.material.R.attr.colorOnSurface
//            ), PorterDuff.Mode.SRC_IN
//        )
//
//        val topAppBarEditText =
//            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
//        topAppBarEditText.apply {
//            setTextColor(
//                ThemeUtils.getThemeAttrColor(
//                    requireContext(),
//                    com.google.android.material.R.attr.colorOnSurface
//                )
//            )
//            setHintTextColor(
//                ThemeUtils.getThemeAttrColor(
//                    requireContext(),
//                    com.google.android.material.R.attr.colorOnSurfaceVariant
//                )
//            )
//            hint = getText(R.string.hint_deck_search_field)
//        }
//
//        searchView.apply {
//            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(p0: String?): Boolean {
//                    binding.cardsActivityProgressBar.visibility = View.VISIBLE
//                    if (p0 != null) {
//                        //TODO: Searching
////                        searchCard(p0, deck.deckId)
////                        binding.cardsActivityProgressBar.visibility = View.GONE
//                        Toast.makeText(appContext, "Searching in development", Toast.LENGTH_LONG).show()
//                    }
//                    return true
//                }
//
//                override fun onQueryTextChange(p0: String?): Boolean {
//                    if (p0 != null) {
//                        //TODO: Searching
////                        searchCard(p0, deck.deckId)
////                        binding.cardsActivityProgressBar.visibility = View.GONE
//                        Toast.makeText(appContext, "Searching in development", Toast.LENGTH_LONG).show()
//                    }
//                    return true
//                }
//
//
//            })
//            searchView.setOnCloseListener {
//                displayAllCards()
//                true
//            }
//        }
//
//    }
//
//    private fun searchCard(query: String, deckId: String) {
//        val searchQuery = "%$query%"
//        if (searchQuery.isBlank() || searchQuery.isEmpty()) {
//            displayAllCards()
//        } else {
//            displayingCardsJob?.cancel()
//            displayingCardsJob = lifecycleScope.launch {
//                cardViewModel.searchCard(searchQuery, deckId)
//                    .observe(this@CardFragment) { cardList ->
//                        if (cardList.isNullOrEmpty()) {
//                            onCardNotFound()
//                        } else {
//                            populateRecyclerView(cardList.toList(), deck)
//                        }
//                    }
//            }
//        }
//    }

    private fun onCardNotFound() {
        binding.cardsActivityProgressBar.isVisible = false
        binding.cardRecyclerView.isVisible = false
        binding.onNoCardTextError.isVisible = false
        binding.tvNoCardFound.isVisible = true
    }

//    @SuppressLint("RestrictedApi")
//    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//        return when (menuItem.itemId) {
//            R.id.mb_export_deck -> {
////                showExportDeckDialog()
//                true
//            }
//
//            R.id.view_deck_menu -> {
//                if (item == null) {
//                    Toast.makeText(
//                        requireContext(),
//                        getString(R.string.error_message_change_view_card),
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
//                    if (binding.cardRecyclerView.layoutManager == staggeredGridLayoutManager) {
//                        changeCardLayoutManager(LINEAR_LAYOUT_MANAGER)
//                        binding.cardRecyclerView.layoutManager = linearLayoutManager
//                        item?.setIcon(
//                            ContextCompat.getDrawable(
//                                requireContext(),
//                                R.drawable.icon_view_agenda
//                            )
//                        )
//                    } else {
//                        changeCardLayoutManager(STAGGERED_GRID_LAYOUT_MANAGER)
//                        binding.cardRecyclerView.layoutManager = staggeredGridLayoutManager
//                        item?.setIcon(
//                            ContextCompat.getDrawable(
//                                requireContext(),
//                                R.drawable.icon_grid_view
//                            )
//                        )
//                    }
//                }
//                true
//            }
//
//            else -> true
//        }
//    }

    private fun getLayoutManager(): String {
        val sharedPreferences =
            requireActivity().getSharedPreferences("cardLayoutManager", Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAYOUT_MANAGER, STAGGERED_GRID_LAYOUT_MANAGER)
            ?: STAGGERED_GRID_LAYOUT_MANAGER
    }

    private fun changeCardLayoutManager(which: String) {
        val sharedPreferences =
            requireActivity().getSharedPreferences("cardLayoutManager", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LAYOUT_MANAGER, which)
        editor.apply()
    }

    private fun cardsToTextToUri(uri: Uri, separator: String) {
//        lifecycleScope.launch {
//            val stringBuilder = StringBuilder()
//            try {
//                appContext?.contentResolver?.openFileDescriptor(uri, "w")?.use { fd ->
//                    FileOutputStream(fd.fileDescriptor).use { outputStream ->
//                        val cards = cardViewModel.getCards(deck.deckId)
//                        cards.forEach { card ->
//                            val newLine = cardToText(card!!, separator)
//                            stringBuilder.append(newLine)
//                        }
//                        outputStream.write(stringBuilder.toString().toByteArray())
//                    }
//                    Toast.makeText(requireContext(), getString(R.string.message_cards_exported_successfully), Toast.LENGTH_LONG).show()
//                }
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//                Toast.makeText(requireContext(), getString(R.string.error_message_file_not_found), Toast.LENGTH_LONG).show()
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Toast.makeText(requireContext(), getString(R.string.error_message_cards_exported_failed), Toast.LENGTH_LONG).show()
//            }
//        }
    }

    private fun getAppTheme(): String {
        val themePicker = ThemePicker()
        val sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        return sharedPref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
    }

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                tts?.setSpeechRate(1.0f)
            }

            else -> {
                Toast.makeText(appContext, getString(R.string.error_read), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.let {
            it.stop()
            it.shutdown()
        }
    }

}