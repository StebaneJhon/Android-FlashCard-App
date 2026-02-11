package com.soaharisonstebane.mneme.card

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.button.MaterialButton
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.databinding.FragmentCardBinding
import com.soaharisonstebane.mneme.quiz.quizGame.QuizGameActivity
import com.soaharisonstebane.mneme.util.Constant
import com.soaharisonstebane.mneme.helper.LanguageUtil
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.QUIZ
import com.soaharisonstebane.mneme.util.ItemLayoutManager.LAYOUT_MANAGER
import com.soaharisonstebane.mneme.util.QuizModeBottomSheet
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.soaharisonstebane.mneme.backend.entities.Deck
import com.soaharisonstebane.mneme.backend.entities.relations.CardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalDeck
import com.soaharisonstebane.mneme.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.soaharisonstebane.mneme.helper.AppMath
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.playback.AndroidAudioPlayer
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import com.soaharisonstebane.mneme.util.CardSortOptions.SORT_CARD_BY_CREATION_DATE
import com.soaharisonstebane.mneme.util.CardSortOptions.SORT_PREF
import com.soaharisonstebane.mneme.util.TextType.CONTENT
import com.soaharisonstebane.mneme.util.TextType.DEFINITION
import com.soaharisonstebane.mneme.util.ThemePicker
import com.soaharisonstebane.mneme.helper.cardToText
import com.soaharisonstebane.mneme.helper.parcelable
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import androidx.core.content.edit
import androidx.recyclerview.widget.ConcatAdapter
import com.soaharisonstebane.mneme.backend.models.toLocal
import com.soaharisonstebane.mneme.helper.CardOnlySpacingDecoration
import com.soaharisonstebane.mneme.quiz.flashCardGame.FlashCardGameActivity
import com.soaharisonstebane.mneme.util.DeckRef.DECK_SORT_BY_CREATION_DATE
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.soaharisonstebane.mneme.util.ItemLayoutManager.GRID
import com.soaharisonstebane.mneme.util.ItemLayoutManager.LINEAR


class CardFragment :
    Fragment(),
    TextToSpeech.OnInitListener {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private lateinit var recyclerViewAdapterDeckPath: RecyclerViewAdapterDeckPath
    private lateinit var deckListHeaderAdapter: DeckListHeaderAdapter
    private lateinit var cardListHeaderAdapter: CardListHeaderAdapter

    private lateinit var concatAdapter: ConcatAdapter

    private var tts: TextToSpeech? = null
    private var startingQuizJob: Job? = null
    private var displaySubdeckJob: Job? = null
    private val supportedLanguages = LanguageUtil().getSupportedLang()
    private lateinit var arrayAdapterSupportedLanguages: ArrayAdapter<String>
    private lateinit var listPopupWindow: ListPopupWindow
    var deckExportModel: DeckExportModel? = null
    var lastPlayedAudioFile: AudioModel? = null
    var lastPlayedAudioViw: LinearLayout? = null

    private val player by lazy {
        AndroidAudioPlayer(requireContext())
    }

    private val cardViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, CardViewModelFactory(repository))[CardViewModel::class.java]
    }

    val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }


    private var createFile = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null && deckExportModel != null) {
            cardsToTextToUri(
                cardViewModel.getActualDeck().deckId,
                uri,
                deckExportModel?.separator!!,
                deckExportModel?.includeSubdecks!!
            )
        }
    }

    private lateinit var sortCardsBottomSheetDialog: SortCardsBottomSheetDialog
    private lateinit var sortDecksBottomSheetDialog: SortDecksBottomSheetDialog
    private lateinit var currentDeckDetailsBottomSheetDialog: CurrentDeckDetailsBottomSheetDialog

    companion object {
        const val TAG = "CardFragment"
        const val REQUEST_CODE = "0"
        const val REQUEST_CODE_CARD = "0"
        const val REQUEST_CODE_QUIZ_MODE = "300"
        const val REQUEST_EXPORT_DECK_CODE = "400"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewTheme = deckPathViewModel.getViewTheme()
        val contextThemeWrapper = ContextThemeWrapper(requireContext(), viewTheme)
        val themeInflater = inflater.cloneInContext(contextThemeWrapper)
        _binding = FragmentCardBinding.inflate(themeInflater, container, false)
        appContext = container?.context
        tts = TextToSpeech(appContext, this)
        cardViewModel.updateCardViewMode(getLayoutManager())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arrayAdapterSupportedLanguages = ArrayAdapter(requireContext(), R.layout.dropdown_item, supportedLanguages)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                deckPathViewModel.currentDeck.collect { deck ->
                    displayData(deck)
                }
            }
        }

        binding.btStartQuiz.setOnClickListener {
            onStartQuiz(cardViewModel.getActualDeck()) { deckWithCards ->
                lunchQuiz(deckWithCards, QUIZ)
            }
        }

        binding.btStartFlashCards.setOnClickListener {
            onStartQuiz(cardViewModel.getActualDeck()) { deckWithCads ->
                lunchQuiz(deckWithCads, FLASH_CARD_QUIZ)
            }
        }

        binding.btMore.setOnClickListener {
            onChooseQuizMode(cardViewModel.getActualDeck())
        }


        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onNavigateBack()
                }
            })

        binding.fabAddShowActions.setOnClickListener {
            onAction(true)
        }

        binding.fabAddHideActions.setOnClickListener {
            onAction(false)
        }

        binding.containerAddSubdeckAndCard.setOnClickListener {
            onAction(false)
        }

        binding.searchView
            .editText
            .addTextChangedListener { text ->
                if (text.isNullOrBlank() || text.isEmpty()) {
                    // TODO: Initial state
                } else {
                    val query = "%$text%"
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            delay(500)
                            cardViewModel.searchCard(query, requireContext())
                            cardViewModel.searchDeck(query)
                            cardViewModel.searchResultUiState.collect { state ->
                                binding.inSearchResult.searchCardProgress.isVisible =
                                    state.isLoading == true
                                binding.inSearchResult.tvNoItemFound.isVisible = state.isError
                                val cardsAdapter = populateRecyclerView(
                                    state.foundCards,
                                    cardViewModel.getActualDeck(),
                                    GRID
                                )
                                val decksAdapter = populateSubdecksRecyclerView(
                                    cardViewModel.getActualDeck(),
                                    state.foundDecks
                                )
                                val searchViewConcatAdapter = ConcatAdapter()
                                if (state.isDeck) {
                                    searchViewConcatAdapter.addAdapter(
                                        SearchResultHeaderAdapter(
                                            getString(R.string.tv_deck_text)
                                        )
                                    )
                                    searchViewConcatAdapter.addAdapter(decksAdapter)
                                }
                                if (state.isCard) {
                                    searchViewConcatAdapter.addAdapter(
                                        SearchResultHeaderAdapter(
                                            getString(R.string.tv_cards_text)
                                        )
                                    )
                                    searchViewConcatAdapter.addAdapter(cardsAdapter)
                                }

                                val uiLayoutManager = StaggeredGridLayoutManager(
                                    2,
                                    StaggeredGridLayoutManager.VERTICAL
                                )
                                uiLayoutManager.gapStrategy =
                                    StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

                                val spacingPx = (16 * resources.displayMetrics.density).toInt()
                                val decoration = CardOnlySpacingDecoration(
                                    spacingPx,
                                    searchViewConcatAdapter,
                                    cardsAdapter,
                                    GRID
                                )

                                binding.inSearchResult.rvFoundItem.apply {
                                    visibility = View.VISIBLE
                                    layoutManager = uiLayoutManager
                                    adapter = searchViewConcatAdapter
                                    setHasFixedSize(true)
                                    addItemDecoration(decoration)
                                }
                            }
                        }
                    }
                }
            }

    }

    private fun onNavigateBack() {
        val path = cardViewModel.deckPath()
        if (path.size > 1) {
            deckPathViewModel.setCurrentDeck(path[1])
            switchTheme()
        } else {
            Toast.makeText(
                appContext,
                getString(R.string.error_message_can_not_go_further),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onAction(areActionsShown: Boolean) {
//        val window = requireActivity().window
//        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
//        windowInsetsController.isAppearanceLightStatusBars = !areActionsShown
        binding.containerAddSubdeckAndCard.isVisible = areActionsShown
    }

    private fun displayData(deck: ExternalDeck) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getDeckPath(deck) { path ->
                    recyclerViewAdapterDeckPath = RecyclerViewAdapterDeckPath(path) { pathTogo ->
                        deckPathViewModel.setCurrentDeck(pathTogo)
                        switchTheme()
                    }
                    binding.rvDeckPath.apply {
                        adapter = recyclerViewAdapterDeckPath
                        setHasFixedSize(true)
                        layoutManager =
                            LinearLayoutManager(appContext, RecyclerView.HORIZONTAL, true)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.fetchDecks(deck.deckId, getDeckSortPref())
                cardViewModel.fetchCards(deck.deckId, requireContext(), getSortPref())
                cardViewModel.uiState.collect { state ->

                    binding.containerNoCardAndDeck.isVisible = state.isError
                    binding.cardsActivityProgressBar.isVisible = state.isLoading == true

                    val cardAdapter = populateRecyclerView(state.cards, deck, state.cardsViewMode)
                    val decksAdapter = populateSubdecksRecyclerView(deck, state.decks)

                    concatAdapter = ConcatAdapter()

                    if (state.isDeck) {
                        deckListHeaderAdapter = DeckListHeaderAdapter {
                            sortDecksBottomSheetDialog =
                                SortDecksBottomSheetDialog.newInstance(getDeckSortPref())
                            sortDecksBottomSheetDialog.show(
                                childFragmentManager,
                                "Sort Decks Bottom Sheet"
                            )
                            childFragmentManager.setFragmentResultListener(
                                SortDecksBottomSheetDialog.SORT_DECK_PREF_REQUEST_CODE,
                                this@CardFragment,
                            ) { _, bundle ->
                                val data =
                                    bundle.getString(SortDecksBottomSheetDialog.SORT_DECK_PREF_BUNDLE_KEY)
                                data?.let { sortPref ->
                                    changeDeckSortPref(sortPref)
                                    cardViewModel.fetchDecks(deck.deckId, getDeckSortPref())
                                }
                            }

                        }
                        concatAdapter.apply {
                            addAdapter(deckListHeaderAdapter)
                            addAdapter(decksAdapter)
                        }
                    }

                    if (state.isCard) {
                        cardListHeaderAdapter = CardListHeaderAdapter(
                            viewMode = state.cardsViewMode,
                            sortCards = {
                                sortCardsBottomSheetDialog =
                                    SortCardsBottomSheetDialog.newInstance(getSortPref())
                                sortCardsBottomSheetDialog.show(
                                    childFragmentManager,
                                    "Sort Cards Bottom Sheet"
                                )
                                childFragmentManager.setFragmentResultListener(
                                    SortCardsBottomSheetDialog.SORT_CARD_PREF_REQUEST_CODE,
                                    this@CardFragment
                                ) { _, bundle ->
                                    val sortPref =
                                        bundle.getString(SortCardsBottomSheetDialog.SORT_CARD_PREF_BUNDLE_KEY)
                                    sortPref?.let { sort ->
                                        changeCardSortPref(sort)
                                        cardViewModel.fetchCards(
                                            deck.deckId,
                                            requireContext(),
                                            getSortPref()
                                        )
                                    }
                                }
                            },
                            updateViewMode = {
                                val isGridView = getLayoutManager() == GRID
                                val newCardViewMode = if (isGridView) LINEAR else GRID
                                changeCardLayoutManager(newCardViewMode)
                                cardViewModel.updateCardViewMode(newCardViewMode)
                            }
                        )
                        concatAdapter.apply {
                            addAdapter(cardListHeaderAdapter)
                            addAdapter(cardAdapter)
                        }
                    }

                    val uiLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    uiLayoutManager.gapStrategy =
                        StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

                    val spacingPx = (16 * resources.displayMetrics.density).toInt()
                    val decoration = CardOnlySpacingDecoration(
                        spacingPx,
                        concatAdapter,
                        cardAdapter,
                        state.cardsViewMode
                    )

                    binding.recyclerView.apply {
                        visibility = View.VISIBLE
                        layoutManager = uiLayoutManager
                        adapter = concatAdapter
                        setHasFixedSize(true)
                        addItemDecoration(decoration)
                    }
                }
            }

        }

        binding.searchBar.apply {
            title = null
            setNavigationOnClickListener {
                activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.mb_export_deck -> {
                        showExportDeckDialog()
                        true
                    }

                    R.id.mb_deck_details -> {
                        showCurrentDeckDetails()
                        true
                    }

                    else -> false
                }
            }
        }

        binding.btAddCard.setOnClickListener {
            onAddNewCard(deck)
        }

        binding.btAddSubdeck.setOnClickListener {
            onAddNewDeck(deck)
        }

    }

    private fun showCurrentDeckDetails() {
        currentDeckDetailsBottomSheetDialog =
            CurrentDeckDetailsBottomSheetDialog.newInstance(cardViewModel.getActualDeck())
        currentDeckDetailsBottomSheetDialog.show(
            childFragmentManager,
            "Current Deck Details Bottom Sheet"
        )
        childFragmentManager.setFragmentResultListener(
            CurrentDeckDetailsBottomSheetDialog.CURRENT_DECK_DETAILS_REQUEST_CODE,
            this
        ) { _, bundle ->
            val data =
                bundle.parcelable<ExternalDeck>(CurrentDeckDetailsBottomSheetDialog.CURRENT_DECK_DETAILS_BUNDLE_KEY)
            data?.let { updatedDeck ->
                cardViewModel.updateDeck(updatedDeck.toLocal())
                deckPathViewModel.setCurrentDeck(updatedDeck)
                switchTheme()

            }
        }
    }

    private fun showExportDeckDialog() {
        val exportDeckDialog = ExportDeckDialog()
        exportDeckDialog.show(childFragmentManager, "Export deck dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_EXPORT_DECK_CODE,
            this
        ) { _, bundle ->
            deckExportModel =
                bundle.parcelable<DeckExportModel>(ExportDeckDialog.EXPORT_DECK_BUNDLE_KEY)
            createFile.launch("${cardViewModel.getActualDeck().deckName}${deckExportModel?.format}")
        }
    }


    private fun populateSubdecksRecyclerView(
        deck: ExternalDeck,
        subdecks: List<ExternalDeck>
    ): SubdeckRecyclerViewAdapter {
        return SubdeckRecyclerViewAdapter(
            subdecks,
            appContext!!,
            {
                onEditDeck(parentDeck = deck, deckToEdit = it)
            }, {
                onDeleteSubdeck(it)
            }, { deck ->
                onChooseQuizMode(deck)
            }, {
                deckPathViewModel.setCurrentDeck(it)
                switchTheme()
            }
        )
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
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE,
            this
        ) { _, bundle ->
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
            val result =
                bundle.parcelable<OnSaveDeckWithCationModel>(NewDeckDialog.SAVE_DECK_BUNDLE_KEY)
            result?.let {
                cardViewModel.insertSubdeck(it.deck)
            }
        }
    }

    private fun onDeleteSubdeck(deck: ExternalDeck) {
        if (deck.cardCount > 0) {
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
            Toast.makeText(requireContext(), "Delete ${deck.deckName}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onChooseQuizMode(deck: ExternalDeck) {
        val quizMode = QuizModeBottomSheet()
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

    private fun onStartQuiz(
        deck: ExternalDeck,
        start: (ExternalDeckWithCardsAndContentAndDefinitions) -> Unit
    ) {
        startingQuizJob?.cancel()
        startingQuizJob = lifecycleScope.launch {
            binding.cardsActivityProgressBar.isVisible = true
            val deckAndCards = cardViewModel.getDeckWithCardsOnStartQuiz(deck.deckId)
            binding.cardsActivityProgressBar.isVisible = false
            if (deckAndCards.cards.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message_empty_deck),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                start(deckAndCards)
            }
            this@launch.cancel()
            this.cancel()
        }

    }

    private fun lunchQuiz(
        deckWithCards: ExternalDeckWithCardsAndContentAndDefinitions,
        quizMode: String
    ) {
        when (quizMode) {
            FLASH_CARD_QUIZ -> {
                val intent = Intent(appContext, FlashCardGameActivity::class.java)
                intent.putExtra(FlashCardGameActivity.DECK_ID_KEY, deckWithCards)
                startActivity(intent)
            }
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

        }
    }

    @SuppressLint("RestrictedApi")
    private fun populateRecyclerView(
        cardList: List<ExternalCardWithContentAndDefinitions>,
        deck: ExternalDeck,
        cardViewMode: String,
    ): CardsRecyclerViewAdapter {
        val pref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appTheme = pref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
        return CardsRecyclerViewAdapter(
            appContext!!,
            appTheme,
            deck,
            cardList,
            cardViewModel.getBoxLevels(),
            cardViewMode,
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
                        type = text.type
                    )
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
                if (lastPlayedAudioViw != view || lastPlayedAudioFile != audio) {
                    lifecycleScope.launch {
                        lastPlayedAudioViw?.findViewById<MaterialButton>(R.id.bt_play)
                            ?.setIconResource(R.drawable.icon_play)
                        lastPlayedAudioViw?.findViewById<LinearProgressIndicator>(R.id.lpi_audio_progression)?.progress =
                            0
                        lastPlayedAudioViw = null
                        lastPlayedAudioFile = null
                        player.stop()
                        delay(100L)
                        val newProgressIndicator: LinearProgressIndicator =
                            view.findViewById(R.id.lpi_audio_progression)
                        playPauseAudio(view, newProgressIndicator, audio)
                        lastPlayedAudioFile = audio
                        lastPlayedAudioViw = view
                    }
                } else {
                    val newProgressIndicator: LinearProgressIndicator =
                        view.findViewById(R.id.lpi_audio_progression)
                    playPauseAudio(view, newProgressIndicator, audio)
                }
            })

    }

    private fun playPauseAudio(
        view: LinearLayout,
        newProgressIndicator: LinearProgressIndicator,
        audio: AudioModel
    ) {
        when {
            player.hasPlayed() && !player.isPlaying() -> {
                // Resume audio
                view.findViewById<MaterialButton>(R.id.bt_play)
                    .setIconResource(R.drawable.icon_pause)
                player.play()
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(
                            player.getCurrentPosition(),
                            player.getDuration()
                        )
                        newProgressIndicator.progress = progress
                        delay(100L)
                    }
                }
            }

            player.hasPlayed() && player.isPlaying() -> {
                // Pause audio
                view.findViewById<MaterialButton>(R.id.bt_play)
                    .setIconResource(R.drawable.icon_play)
                player.pause()
            }

            !player.hasPlayed() && !player.isPlaying() -> {
                // Play audio
                view.findViewById<MaterialButton>(R.id.bt_play)
                    .setIconResource(R.drawable.icon_pause)
                val audioFile = File(context?.filesDir, audio.name)
                player.playFile(audioFile)
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(
                            player.getCurrentPosition(),
                            player.getDuration()
                        )
                        newProgressIndicator.progress = progress
                        delay(100L)
                    }
                }
                player.onCompletion {
                    lastPlayedAudioFile = null
                    lastPlayedAudioViw = null
                    view.findViewById<MaterialButton>(R.id.bt_play)
                        .setIconResource(R.drawable.icon_play)
                }
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
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle(getString(R.string.error_title_starting_quiz))
            .setMessage(errorText)
            .setNegativeButton(R.string.bt_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.bt_add_card, deck.deckName)) { dialog, _ ->
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
            com.google.android.material.R.attr.colorSurfaceContainerHighest,
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
            val result =
                bundle.parcelable<CardWithContentAndDefinitions>(NewCardDialog.EDIT_CARD_BUNDLE_KEY)
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

    private fun getLayoutManager(): String {
        val sharedPreferences =
            requireActivity().getSharedPreferences("cardViewPref", Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAYOUT_MANAGER, LINEAR)
            ?: LINEAR
    }

    private fun changeCardLayoutManager(which: String) {
        val sharedPreferences =
            requireActivity().getSharedPreferences("cardViewPref", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString(LAYOUT_MANAGER, which)
        }
    }

    private fun getSortPref(): String {
        val sharedPreferences =
            requireActivity().getSharedPreferences("cardViewPref", Context.MODE_PRIVATE)
        return sharedPreferences.getString(SORT_PREF, SORT_CARD_BY_CREATION_DATE)
            ?: SORT_CARD_BY_CREATION_DATE
    }

    private fun changeCardSortPref(sortPref: String) {
        val sharedPreferences =
            requireActivity().getSharedPreferences("cardViewPref", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString(SORT_PREF, sortPref)
        }
    }

    private fun changeDeckSortPref(sortPref: String) {
        val deckViewPref =
            requireActivity().getSharedPreferences("deckViewPref", Context.MODE_PRIVATE)
        deckViewPref.edit {
            putString(SORT_PREF, sortPref)
        }
    }

    private fun getDeckSortPref(): String {
        val deckViewPref =
            requireActivity().getSharedPreferences("deckViewPref", Context.MODE_PRIVATE)
        return deckViewPref.getString(SORT_PREF, DECK_SORT_BY_CREATION_DATE)
            ?: DECK_SORT_BY_CREATION_DATE
    }

    private fun cardsToTextToUri(
        deckId: String,
        uri: Uri,
        separator: String,
        includeSubdecks: Boolean
    ) {
        lifecycleScope.launch {
            val stringBuilder = StringBuilder()
            try {
                appContext?.contentResolver?.openFileDescriptor(uri, "w")?.use { fd ->
                    FileOutputStream(fd.fileDescriptor).use { outputStream ->

                        val cards = if (includeSubdecks) {
                            cardViewModel.getDeckAndSubdecksCards(deckId)
                        } else {
                            cardViewModel.getCards(deckId, requireContext())
                        }
                        cards.forEach { card ->
                            val newLine = cardToText(card!!, separator)
                            stringBuilder.append(newLine)
                        }
                        outputStream.write(stringBuilder.toString().toByteArray())
                    }
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.message_cards_exported_successfully),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message_file_not_found),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_message_cards_exported_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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

    private fun switchTheme() {
        val navController = findNavController()
        navController.navigate(
            navController.currentDestination!!.id,
            null,
            NavOptions.Builder()
                .setPopUpTo(navController.currentDestination!!.id, true)
                .build()
        )
    }

}