package com.example.flashcard.card

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
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ThemeUtils
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.databinding.FragmentCardBinding
import com.example.flashcard.deck.NewDeckDialog
import com.example.flashcard.quiz.flashCardGame.FlashCardGameActivity
import com.example.flashcard.quiz.flashCardGameTimed.FlashCardGameTimedActivity
import com.example.flashcard.quiz.matchQuizGame.MatchQuizGameActivity
import com.example.flashcard.quiz.multichoiceQuizGame.MultiChoiceQuizGameActivity
import com.example.flashcard.quiz.quizGame.QuizGameActivity
import com.example.flashcard.quiz.test.TestActivity
import com.example.flashcard.quiz.writingQuizGame.WritingQuizGameActivity
import com.example.flashcard.util.Constant
import com.example.flashcard.util.Constant.MIN_CARD_FOR_MATCHING_QUIZ
import com.example.flashcard.util.Constant.MIN_CARD_FOR_MULTI_CHOICE_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.MATCHING_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.MULTIPLE_CHOICE_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.TEST
import com.example.flashcard.util.FlashCardMiniGameRef.TIMED_FLASH_CARD_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.WRITING_QUIZ
import com.example.flashcard.util.QuizModeBottomSheet
import com.example.flashcard.util.UiState
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale


class CardFragment :
    Fragment(),
    MenuProvider,
    TextToSpeech.OnInitListener
{

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private lateinit var recyclerViewAdapter: CardsRecyclerViewAdapter
    private lateinit var tts: TextToSpeech

    private val cardViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, CardViewModelFactory(repository))[CardViewModel::class.java]
    }

    val args: CardFragmentArgs by navArgs()
    private var deck: ImmutableDeck? = null
    private var opener: String? = null

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

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).setSupportActionBar(binding.cardsTopAppBar)

        deck = args.selectedDeck
        opener = args.opener

        deck?.let { _deck ->
            view.findViewById<MaterialToolbar>(R.id.cardsTopAppBar).apply {
                title = _deck.deckName
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
                        onChooseQuizMode(_deck.deckId)
                    }
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.bt_flash_card_game -> {
                                onStartQuiz { deckWithCards ->
                                    lunchQuiz(deckWithCards, FLASH_CARD_QUIZ)
                                }
                                true
                            }

                            R.id.bt_multiple_choice_quiz -> {
                                onStartQuiz { deckWithCards ->
                                    lunchQuiz(deckWithCards, MULTIPLE_CHOICE_QUIZ)
                                }
                                true
                            }

                            R.id.bt_test_quiz_game -> {
                                onStartQuiz { deckWithCards ->
                                    lunchQuiz(deckWithCards, QUIZ)
                                }
                                true
                            }

                            else -> false
                        }
                    }
                }
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    cardViewModel.getDeckWithCards(_deck.deckId)
                    cardViewModel.deckWithAllCards.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.cardsActivityProgressBar.isVisible = true
                            }

                            is UiState.Error -> {
                                onDeckEmpty()
                            }

                            is UiState.Success -> {
                                displayCards(state.data.cards!!, state.data.deck!!)
                            }
                        }
                    }
                }
            }

            binding.fabAddCard.setOnClickListener {
                onAddNewCard()
            }

            if (opener == NewDeckDialog.TAG) {
                onAddNewCard()
            }

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

    private fun onDeckEmpty() {
        binding.cardsActivityProgressBar.isVisible = false
        binding.cardRecyclerView.isVisible = false
        binding.onNoDeckTextError.isVisible = true
    }

    private fun onChooseQuizMode(deckId: String) {
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cardViewModel.getDeckWithCards(deck?.deckId!!)
                cardViewModel.deckWithAllCards.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.cardsActivityProgressBar.isVisible = true
                        }

                        is UiState.Error -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            onStartingQuizError(state.errorMessage)
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

            TIMED_FLASH_CARD_QUIZ -> {
                val intent = Intent(appContext, FlashCardGameTimedActivity::class.java)
                intent.putExtra(FlashCardGameTimedActivity.DECK_ID_KEY, deckWithCards)
                startActivity(intent)
            }

            MULTIPLE_CHOICE_QUIZ -> {
                if (deckWithCards.cards?.size!! > MIN_CARD_FOR_MULTI_CHOICE_QUIZ) {
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
                val intent = Intent(appContext, TestActivity::class.java)
                intent.putExtra(TestActivity.DECK_ID_KEY, deckWithCards)
                startActivity(intent)
            }

        }
    }

    private fun displayCards(cardList: List<ImmutableCard?>, deck: ImmutableDeck) {
        val pref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appTheme = pref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
        binding.cardsActivityProgressBar.isVisible = false
        binding.onNoDeckTextError.isVisible = false
        binding.cardRecyclerView.isVisible = true
        recyclerViewAdapter = appContext?.let { it ->
            CardsRecyclerViewAdapter(
                it,
                appTheme,
                cardList,
                deck,
                cardViewModel.getBoxLevels()!!,
                { selectedCard ->
                    onEditCard(selectedCard!!)
                },
                { selectedCard ->
                    cardViewModel.deleteCard(selectedCard, deck)
                },
                { text ->
                    if (tts.isSpeaking) {
                        stopReading(text)
                    } else {
                        readText(text, deck.deckFirstLanguage!!)
                    }
                }) { text ->
                if (tts.isSpeaking) {
                    stopReading(text)
                } else {
                    readText(text, deck.deckSecondLanguage!!)
                }
            }
        }!!
        val gridLayoutManager = GridLayoutManager(appContext, 2, GridLayoutManager.VERTICAL, false)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return getSpanSize(cardList, position)
            }
        }

        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.cardRecyclerView.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = staggeredGridLayoutManager
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

        val text = textAndView.text
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

        tts.language = Locale.forLanguageTag(
            TextToSpeechHelper().getLanguageCodeForTextToSpeech(language)!!
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts.speak(text, TextToSpeech.QUEUE_ADD, params, "UniqueID")

        tts.setOnUtteranceProgressListener(speechListener)

    }

    private fun stopReading(textAndView: TextClickedModel) {
        val view = textAndView.view
        val textColor = MaterialColors.getColor(
            appContext!!,
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            Color.GRAY
        )
        view.setTextColor(textColor)
        tts.stop()
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

        val newCardDialog = NewCardDialog(null, deck!!, Constant.ADD)
        newCardDialog.show(childFragmentManager, "New Card Dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CARD,
            this
        ) { requestQuey, bundle ->
            val result = bundle.parcelable<ImmutableCard>(NewCardDialog.SAVE_CARDS_BUNDLE_KEY)
            result?.let { it ->
                cardViewModel.insertCards(it, deck!!)
            }
        }
    }

    private fun onEditCard(card: ImmutableCard) {
        val newCardDialog = NewCardDialog(card, deck!!, Constant.UPDATE)
        newCardDialog.show(childFragmentManager, "New Card Dialog")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_CARD,
            this
        ) { requestQuey, bundle ->
            val result = bundle.parcelable<ImmutableCard>(NewCardDialog.EDIT_CARD_BUNDLE_KEY)
            result?.let { it ->
                cardViewModel.updateCard(it.first())
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.deck_fragment_menu, menu)
        val search = menu.findItem(R.id.search_deck_menu)
        val searchView = search?.actionView as SearchView

        val searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button) as ImageView
        searchIcon.setColorFilter(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface
            ), PorterDuff.Mode.SRC_IN
        )

        val searchIconClose =
            searchView.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        searchIconClose.setColorFilter(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface
            ), PorterDuff.Mode.SRC_IN
        )

        val searchIconMag =
            searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
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
                    searchCard(p0)
                    binding.cardsActivityProgressBar.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    searchCard(p0)
                    binding.cardsActivityProgressBar.visibility = View.GONE
                }
                return true
            }
        })
    }

    private fun searchCard(query: String) {
        val searchQuery = "%$query%"
        lifecycleScope.launch {
            deck?.let { cardDeck ->
                cardViewModel.searchCard(searchQuery, cardDeck.deckId)
                    .observe(this@CardFragment) { cardList ->
                        cardList?.let { displayCards(it, cardDeck) }
                    }
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return true
    }

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): ArrayList<T>? =
        when {
            Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
            else -> @Suppress("DEPRECATION") getParcelableArrayList<T>(key)
        }

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                tts.setSpeechRate(1.0f)
            }
            else -> {
                Toast.makeText(appContext, getString(R.string.error_read), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }

}