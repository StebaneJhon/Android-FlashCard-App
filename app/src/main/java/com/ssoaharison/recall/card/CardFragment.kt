package com.ssoaharison.recall.card

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.databinding.FragmentCardBinding
import com.ssoaharison.recall.deck.NewDeckDialog
import com.ssoaharison.recall.quiz.flashCardGame.FlashCardGameActivity
import com.ssoaharison.recall.quiz.matchQuizGame.MatchQuizGameActivity
import com.ssoaharison.recall.quiz.multichoiceQuizGame.MultiChoiceQuizGameActivity
import com.ssoaharison.recall.quiz.quizGame.QuizGameActivity
import com.ssoaharison.recall.quiz.test.TestActivity
import com.ssoaharison.recall.quiz.writingQuizGame.WritingQuizGameActivity
import com.ssoaharison.recall.util.Constant
import com.ssoaharison.recall.util.Constant.MIN_CARD_FOR_MATCHING_QUIZ
import com.ssoaharison.recall.util.Constant.MIN_CARD_FOR_MULTI_CHOICE_QUIZ
import com.ssoaharison.recall.util.LanguageUtil
import com.ssoaharison.recall.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.MATCHING_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.MULTIPLE_CHOICE_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.TEST
import com.ssoaharison.recall.util.FlashCardMiniGameRef.WRITING_QUIZ
import com.ssoaharison.recall.util.ItemLayoutManager.LAYOUT_MANAGER
import com.ssoaharison.recall.util.ItemLayoutManager.LINEAR_LAYOUT_MANAGER
import com.ssoaharison.recall.util.ItemLayoutManager.STAGGERED_GRID_LAYOUT_MANAGER
import com.ssoaharison.recall.util.QuizModeBottomSheet
import com.ssoaharison.recall.util.UiState
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssoaharison.recall.util.Constant.MIN_CARD_FOR_TEST
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale


class CardFragment :
    Fragment(),
    MenuProvider,
    TextToSpeech.OnInitListener {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private lateinit var recyclerViewAdapter: CardsRecyclerViewAdapter
    private var tts: TextToSpeech? = null
    private var startingQuizJob: Job? = null
    private var displayingCardsJob: Job? = null

    private val cardViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, CardViewModelFactory(repository))[CardViewModel::class.java]
    }

    @SuppressLint("RestrictedApi")
    private var item: ActionMenuItemView? = null

    val args: CardFragmentArgs by navArgs()
    private lateinit var deck: ImmutableDeck
    private lateinit var opener: String

    private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        const val TAG = "CardFragment"
        const val REQUEST_CODE_CARD = "0"
        const val REQUEST_CODE_QUIZ_MODE = "300"
        const val MIN_DEFINITION_LENGTH_FOR_SPAM_1 = 200
        const val MIN_CONTENT_LENGTH_FOR_SPAM_1 = 100
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).setSupportActionBar(binding.cardsTopAppBar)

        deck = args.selectedDeck
        opener = args.opener

        staggeredGridLayoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        linearLayoutManager = LinearLayoutManager(appContext)

        view.findViewById<MaterialToolbar>(R.id.cardsTopAppBar).apply {
            title = deck.deckName
            setNavigationOnClickListener {
                findNavController().navigate(
                    R.id.deckFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.cardFragment, true)
                        .build()
                )
            }

            binding.bottomAppBar.apply {
                setNavigationOnClickListener {
                    onChooseQuizMode()
                }
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.bt_flash_card_game -> {
                            onStartQuiz { deckWithCards ->
                                lunchQuiz(deckWithCards, FLASH_CARD_QUIZ)
                            }
                            true
                        }

                        R.id.bt_quiz -> {
                            onStartQuiz { deckWithCards ->
                                lunchQuiz(deckWithCards, QUIZ)
                            }
                            true
                        }

                        R.id.bt_test -> {
                            onStartQuiz { deckWithCards ->
                                lunchQuiz(deckWithCards, TEST)
                            }
                            true
                        }

                        else -> false
                    }
                }
            }
        }
        displayAllCards()

        binding.fabAddCard.setOnClickListener {
            onAddNewCard()
        }

        if (opener == NewDeckDialog.TAG) {
            onAddNewCard()
        }



        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(
                        R.id.deckFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.cardFragment, true)
                            .build()
                    )
                }
            })

    }

    private fun displayAllCards() {
        displayingCardsJob?.cancel()
        displayingCardsJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getDeckWithCards(deck.deckId)
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
                            populateRecyclerView(state.data.cards!!, state.data.deck!!)
                        }
                    }
                }
            }
        }
    }

    private fun onDeckEmpty() {
        binding.cardsActivityProgressBar.isVisible = false
        binding.cardRecyclerView.isVisible = false
        binding.tvNoCardFound.isVisible = false
        binding.onNoCardTextError.isVisible = true
    }

    private fun onChooseQuizMode() {
        val quizMode = QuizModeBottomSheet()
        quizMode.show(childFragmentManager, "Quiz Mode")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_QUIZ_MODE,
            this
        ) { _, bundle ->
            val result = bundle.getString(QuizModeBottomSheet.START_QUIZ_BUNDLE_KEY)
            result?.let {
                onStartQuiz { deckWithCards ->
                    lunchQuiz(deckWithCards, it)
                }
            }
        }
    }

    private fun onStartQuiz(start: (ImmutableDeckWithCards) -> Unit) {
        startingQuizJob?.cancel()
        startingQuizJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getDeckWithCards(deck.deckId)
                cardViewModel.deckWithAllCards.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.cardsActivityProgressBar.isVisible = true
                        }

                        is UiState.Error -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            onStartingQuizError(getString(R.string.error_message_empty_deck))
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

    private fun lunchQuiz(deckWithCards: ImmutableDeckWithCards, quizMode: String) {
        when (quizMode) {
            FLASH_CARD_QUIZ -> {
                val intent = Intent(appContext, FlashCardGameActivity::class.java)
                intent.putExtra(FlashCardGameActivity.DECK_ID_KEY, deckWithCards)
                startActivity(intent)
            }

            MULTIPLE_CHOICE_QUIZ -> {
                if (deckWithCards.cards?.size!! >= MIN_CARD_FOR_MULTI_CHOICE_QUIZ) {
                    val intent = Intent(appContext, MultiChoiceQuizGameActivity::class.java)
                    intent.putExtra(MultiChoiceQuizGameActivity.DECK_ID_KEY, deckWithCards)
                    startActivity(intent)
                } else {
                    onStartingQuizError(
                        getString(
                            R.string.error_message_starting_quiz,
                            "$MIN_CARD_FOR_MULTI_CHOICE_QUIZ"
                        )
                    )
                }
            }

            WRITING_QUIZ -> {
                val intent = Intent(appContext, WritingQuizGameActivity::class.java)
                intent.putExtra(WritingQuizGameActivity.DECK_ID_KEY, deckWithCards)
                startActivity(intent)
            }

            MATCHING_QUIZ -> {
                if (deckWithCards.cards?.size!! >= MIN_CARD_FOR_MATCHING_QUIZ) {
                    val intent = Intent(appContext, MatchQuizGameActivity::class.java)
                    intent.putExtra(MatchQuizGameActivity.DECK_ID_KEY, deckWithCards)
                    startActivity(intent)
                } else {
                    onStartingQuizError(
                        getString(
                            R.string.error_message_starting_quiz,
                            "$MIN_CARD_FOR_MATCHING_QUIZ"
                        )
                    )
                }
            }

            QUIZ -> {
                val intent = Intent(appContext, QuizGameActivity::class.java)
                intent.putExtra(QuizGameActivity.DECK_ID_KEY, deckWithCards)
                startActivity(intent)
            }

            TEST -> {
                if (deckWithCards.cards?.size!! >= MIN_CARD_FOR_TEST) {
                    val intent = Intent(appContext, TestActivity::class.java)
                    intent.putExtra(TestActivity.DECK_ID_KEY, deckWithCards)
                    startActivity(intent)
                } else {
                    onStartingQuizError(
                        getString(
                            R.string.error_message_starting_quiz,
                            "$MIN_CARD_FOR_TEST"
                        )
                    )
                }

            }

        }
    }

    @SuppressLint("RestrictedApi")
    private fun populateRecyclerView(cardList: List<ImmutableCard?>, deck: ImmutableDeck) {
        item = binding.cardsTopAppBar.findViewById(R.id.view_deck_menu)
        val pref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appTheme = pref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
        binding.cardsActivityProgressBar.isVisible = false
        binding.onNoCardTextError.isVisible = false
        binding.tvNoCardFound.isVisible = false
        binding.cardRecyclerView.isVisible = true
        recyclerViewAdapter = appContext?.let { it ->
            CardsRecyclerViewAdapter(
                it,
                appTheme,
                deck,
                cardList,
                cardViewModel.getBoxLevels()!!,
                { selectedCard ->
                    onEditCard(selectedCard!!)
                },
                { selectedCard ->
                    cardViewModel.deleteCard(selectedCard)
                },
                { text ->
                    if (tts?.isSpeaking == true) {
                        stopReading(text)
                    } else {
                        readText(text, text.text.language)
                    }
                }) { text ->
                if (tts?.isSpeaking == true) {
                    stopReading(text)
                } else {
                    readText(text, text.text.language)
                }
            }
        }!!
        val gridLayoutManager = GridLayoutManager(appContext, 2, GridLayoutManager.VERTICAL, false)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return getSpanSize(cardList, position)
            }
        }

        binding.cardRecyclerView.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager =
                if (this@CardFragment.getLayoutManager() == STAGGERED_GRID_LAYOUT_MANAGER) {
                    item?.setIcon(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_grid_view
                        )
                    )
                    staggeredGridLayoutManager
                } else {
                    item?.setIcon(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_view_agenda
                        )
                    )
                    linearLayoutManager
                }
        }

    }

    private fun onStartingQuizError(errorText: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(getString(R.string.error_title_starting_quiz))
            .setMessage(errorText)
            .setNegativeButton(R.string.bt_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.bt_add_card)) { dialog, _ ->
                onAddNewCard()
                dialog.dismiss()
            }
            .show()
    }

    private fun readText(textAndView: TextClickedModel, language: String) {

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
        val view = textAndView.view
        val textColor = MaterialColors.getColor(
            appContext!!,
            com.google.android.material.R.attr.colorOnSurface,
            Color.GRAY
        )
        view.setTextColor(textColor)
        tts?.stop()
    }

    private fun getSpanSize(
        cardList: List<ImmutableCard?>,
        cardPosition: Int,
    ): Int {
        val contentSize = cardList[cardPosition]?.cardContent?.content?.length ?: 0
        val definitionSize = getMaxDefinitionLength(cardList[cardPosition]?.cardDefinition)


        if (
            definitionSize > MIN_DEFINITION_LENGTH_FOR_SPAM_1 && cardPosition.minus(1) % 2 != 0 ||
            contentSize > MIN_CONTENT_LENGTH_FOR_SPAM_1 && cardPosition.minus(1) % 2 != 0
        ) {
            return 2
        }

        return 1
    }


    private fun getMaxDefinitionLength(definitions: List<CardDefinition>?): Int {
        definitions?.let {
            var maxLength = 0
            it.forEach { defin ->
                val actualLength = defin.definition.length
                if (actualLength > maxLength) {
                    maxLength = actualLength
                }
            }
            return maxLength
        }
        return 0
    }

    private fun onAddNewCard() {

        val newCardDialog = NewCardDialog(null, deck, Constant.ADD)
        newCardDialog.show(childFragmentManager, "New Card Dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CARD,
            this
        ) { requestKey, bundle ->
            val result = bundle.parcelable<ImmutableCard>(NewCardDialog.SAVE_CARDS_BUNDLE_KEY)
            result?.let {
                cardViewModel.insertCards(it, deck)
            }
        }
    }

    private fun onEditCard(card: ImmutableCard) {
        val newCardDialog = NewCardDialog(card, deck, Constant.UPDATE)
        newCardDialog.show(childFragmentManager, "New Card Dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CARD,
            this
        ) { _, bundle ->
            val result = bundle.parcelable<ImmutableCard>(NewCardDialog.EDIT_CARD_BUNDLE_KEY)
            result?.let {
                cardViewModel.updateCard(it.first())
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.deck_fragment_menu, menu)
        val search = menu.findItem(R.id.search_deck_menu)
        val searchView = search?.actionView as SearchView

        val searchIcon: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_button)
        searchIcon.setColorFilter(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface
            ), PorterDuff.Mode.SRC_IN
        )

        val searchIconClose: ImageView =
            searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        searchIconClose.setColorFilter(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface
            ), PorterDuff.Mode.SRC_IN
        )

        val searchIconMag: ImageView =
            searchView.findViewById(androidx.appcompat.R.id.search_go_btn)
        searchIconMag.setColorFilter(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface
            ), PorterDuff.Mode.SRC_IN
        )

        val topAppBarEditText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        topAppBarEditText.apply {
            setTextColor(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
            setHintTextColor(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorOnSurfaceVariant
                )
            )
            hint = getText(R.string.hint_deck_search_field)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                binding.cardsActivityProgressBar.visibility = View.VISIBLE
                if (p0 != null) {
                    searchCard(p0, deck.deckId)
                    binding.cardsActivityProgressBar.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    searchCard(p0, deck.deckId)
                    binding.cardsActivityProgressBar.visibility = View.GONE
                }
                return true
            }


        })
        searchView.setOnCloseListener{
            displayAllCards()
            true
        }
    }

    private fun searchCard(query: String, deckId: String) {
        val searchQuery = "%$query%"
        if (searchQuery.isBlank() || searchQuery.isEmpty()) {
            displayAllCards()
        } else {
            displayingCardsJob?.cancel()
            displayingCardsJob = lifecycleScope.launch {
                cardViewModel.searchCard(searchQuery, deckId)
                    .observe(this@CardFragment) { cardList ->
                        if (cardList.isNullOrEmpty()) {
                            onCardNotFound()
                        } else {
                            populateRecyclerView(cardList.toList(), deck)
                        }
                    }
            }
        }
    }

    private fun onCardNotFound() {
        binding.cardsActivityProgressBar.isVisible = false
        binding.cardRecyclerView.isVisible = false
        binding.onNoCardTextError.isVisible = false
        binding.tvNoCardFound.isVisible = true
    }

    @SuppressLint("RestrictedApi")
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.settings_deck_menu -> {
                findNavController().navigate(R.id.action_cardFragment_to_settingsFragment)
                true
            }

            R.id.view_deck_menu -> {
                if (item == null) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_message_change_view_card),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (binding.cardRecyclerView.layoutManager == staggeredGridLayoutManager) {
                        changeCardLayoutManager(LINEAR_LAYOUT_MANAGER)
                        binding.cardRecyclerView.layoutManager = linearLayoutManager
                        item?.setIcon(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.icon_view_agenda
                            )
                        )
                    } else {
                        changeCardLayoutManager(STAGGERED_GRID_LAYOUT_MANAGER)
                        binding.cardRecyclerView.layoutManager = staggeredGridLayoutManager
                        item?.setIcon(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.icon_grid_view
                            )
                        )
                    }
                }
                true
            }

            else -> true
        }
    }

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

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): ArrayList<T>? =
        when {
            Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
            else -> @Suppress("DEPRECATION") getParcelableArrayList<T>(key)
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