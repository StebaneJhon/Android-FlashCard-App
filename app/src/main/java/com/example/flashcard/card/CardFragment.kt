package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.CardDefinition
import com.example.flashcard.databinding.FragmentCardBinding
import com.example.flashcard.quiz.flashCardGame.FlashCardGameActivity
import com.example.flashcard.quiz.flashCardGameTimed.FlashCardGameTimedActivity
import com.example.flashcard.quiz.matchQuizGame.MatchQuizGameActivity
import com.example.flashcard.quiz.multichoiceQuizGame.MultiChoiceQuizGameActivity
import com.example.flashcard.quiz.testQuizGame.TestQuizGameActivity
import com.example.flashcard.quiz.writingQuizGame.WritingQuizGameActivity
import com.example.flashcard.util.Constant
import com.example.flashcard.util.UiState
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale


class CardFragment : Fragment(), NewCardDialog.NewDialogListener, MenuProvider {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private lateinit var recyclerViewAdapter: CardsRecyclerViewAdapter
    lateinit var tts: TextToSpeech

    private val cardViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, CardViewModelFactory(repository))[CardViewModel::class.java]
    }

    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }

    private var actionClicked = false

    val args: CardFragmentArgs by navArgs()
    private var deck: ImmutableDeck? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        appContext = container?.context
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).setSupportActionBar(binding.cardsTopAppBar)

        deck = args.selectedDeck
        deck?.let {_deck ->
            view.findViewById<MaterialToolbar>(R.id.cardsTopAppBar).apply {
                title = _deck.deckName
                setNavigationOnClickListener {
                    findNavController().navigate(
                        R.id.deckFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.cardFragment, true)
                            .build())
                }
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    cardViewModel.getDeckWithCards(_deck.deckId!!)
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

            binding.btAction.setOnClickListener {
                onBtActonClicked()
            }

            binding.addNewCardBT.setOnClickListener {
                onAddNewCard(null)
            }

            binding.startQuizBT.setOnClickListener {
                _deck.deckId?.let { it1 -> onStartQuiz(it1) }
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
                            .build())
                }
            })
    }

    private fun onBtActonClicked() {
        setActionsVisibility(actionClicked)
        setActionsAnimation(actionClicked)
        actionClicked = !actionClicked
    }

    private fun setActionsVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.addNewCardBT.visibility = View.VISIBLE
            binding.startQuizBT.visibility = View.VISIBLE
        } else {
            binding.addNewCardBT.visibility = View.GONE
            binding.startQuizBT.visibility = View.GONE
        }
    }

    private fun setActionsAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.addNewCardBT.startAnimation(fromBottom)
            binding.startQuizBT.startAnimation(fromBottom)
        } else {
            binding.addNewCardBT.startAnimation(toBottom)
            binding.startQuizBT.startAnimation(toBottom)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.addNewCardBT.isClickable = true
            binding.startQuizBT.isClickable = true
        } else {
            binding.addNewCardBT.isClickable = false
            binding.startQuizBT.isClickable = false
        }
    }



    private fun onDeckEmpty() {
        binding.cardsActivityProgressBar.isVisible = false
        binding.cardRecyclerView.isVisible = false
        binding.onNoDeckTextError.isVisible = true
    }

    @SuppressLint("MissingInflatedId")
    private fun onStartQuiz(deckId: Int) {
        val viewGroup = binding.cardsActivityRoot
        val dialogBinding = layoutInflater.inflate(R.layout.quiz_mode_fragment, viewGroup, false)
        val quizModeDialog = appContext?.let { Dialog(it) }

        quizModeDialog?.apply {
            setContentView(dialogBinding)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

        val btFlashCard: Button = dialogBinding.findViewById(R.id.bt_flash_card_game)
        btFlashCard.setOnClickListener {
            startFlashCardGame()
            quizModeDialog?.dismiss()
        }

        val btFlashCardTimed: Button = dialogBinding.findViewById(R.id.bt_flash_card_game_timed)
        btFlashCardTimed.setOnClickListener {
            startFlashCardGameTimed()
            quizModeDialog?.dismiss()
        }

        val multiChoiceQuizButton: Button = dialogBinding.findViewById(R.id.multiChoiceQuizButton)
        multiChoiceQuizButton.setOnClickListener {
            startMultiChoiceQuizGame()
            quizModeDialog?.dismiss()
        }

        val writingQuizGameButton: Button = dialogBinding.findViewById(R.id.bt_writing_quiz_game)
        writingQuizGameButton.setOnClickListener {
            startWritingQuizGame()
            quizModeDialog?.dismiss()
        }

        val matchingQuizGameButton: Button = dialogBinding.findViewById(R.id.bt_matching_quiz_game)
        matchingQuizGameButton.setOnClickListener {
            startMatchingQuizGame()
            quizModeDialog?.dismiss()
        }

        val btTestQuizGame: Button = dialogBinding.findViewById(R.id.bt_test_quiz_game)
        btTestQuizGame.setOnClickListener {
            startTestQuizGame()
            quizModeDialog?.dismiss()
        }
    }

    private fun startTestQuizGame() {
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
                            Toast.makeText(
                                appContext,
                                state.errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is UiState.Success -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            val intent = Intent(appContext, TestQuizGameActivity::class.java)
                            intent.putExtra(TestQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startFlashCardGame() {
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
                            Toast.makeText(
                                appContext,
                                state.errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is UiState.Success -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            val intent = Intent(appContext, FlashCardGameActivity::class.java)
                            intent.putExtra(FlashCardGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startFlashCardGameTimed() {
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
                            Toast.makeText(
                                appContext,
                                state.errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is UiState.Success -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            val intent = Intent(appContext, FlashCardGameTimedActivity::class.java)
                            intent.putExtra(FlashCardGameTimedActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startMultiChoiceQuizGame() {
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
                            Toast.makeText(
                                appContext,
                                state.errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is UiState.Success -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            val intent = Intent(appContext, MultiChoiceQuizGameActivity::class.java)
                            intent.putExtra(MultiChoiceQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startWritingQuizGame() {
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
                            Toast.makeText(
                                appContext,
                                state.errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is UiState.Success -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            val intent = Intent(appContext, WritingQuizGameActivity::class.java)
                            intent.putExtra(WritingQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startMatchingQuizGame() {
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
                            Toast.makeText(
                                appContext,
                                state.errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is UiState.Success -> {
                            binding.cardsActivityProgressBar.isVisible = false
                            val intent = Intent(appContext, MatchQuizGameActivity::class.java)
                            intent.putExtra(MatchQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun displayCards(cardList: List<ImmutableCard?>, deck: ImmutableDeck) {
        binding.cardsActivityProgressBar.isVisible = false
        binding.onNoDeckTextError.isVisible = false
        binding.cardRecyclerView.isVisible = true
        recyclerViewAdapter = appContext?.let { it ->
            CardsRecyclerViewAdapter(
                it,
                cardList,
                deck,
                cardViewModel.getBoxLevels()!!,
                { selectedCard ->
                    onFullScreen(selectedCard, deck)
                },
                { selectedCard ->
                    onAddNewCard(selectedCard)
                },
                {selectedCard ->
                    cardViewModel.deleteCard(selectedCard, deck)
                },
                {text ->
                    text?.let {t ->
                        //val a = Locale.getAvailableLocales()
                        readText(t, deck.deckFirstLanguage!!)
                    }
                },
                {text ->
                    text?.let {t ->
                        //val a = Locale.getAvailableLocales()
                        readText(t, deck.deckSecondLanguage!!)
                    }

                })
        }!!

        val gridLayoutManager = GridLayoutManager(appContext, 2, GridLayoutManager.VERTICAL, false)
        gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return getSpanSize(cardList, position, 12, 16)
            }

        }

        binding.cardRecyclerView.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
        }
    }

    private fun readText(textAndView: TextClickedModel, language: String) {

        val text = textAndView.text
        val view = textAndView.view
        val textColor = view.textColors
        val onReadColor = MaterialColors.getColor(appContext!!, androidx.appcompat.R.attr.colorPrimary, Color.GRAY)

        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                view.setTextColor(onReadColor)
            }

            override fun onDone(utteranceId: String?) {
                view.setTextColor(textColor)
            }

            override fun onError(utteranceId: String?) {
                TODO("Not yet implemented")
            }
        }

        val params = Bundle()

        tts = TextToSpeech(appContext ,  TextToSpeech.OnInitListener {
            when (it) {
                TextToSpeech.SUCCESS -> {
                    if (tts.isSpeaking) {
                        tts.stop()
                        tts.shutdown()
                    } else {
                        tts.language = Locale.forLanguageTag(TextToSpeechHelper().getLanguageCodeForTextToSpeech(language)!!)
                        tts.setSpeechRate(1.0f)
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
                        tts.speak(text, TextToSpeech.QUEUE_ADD, params, "UniqueID")
                    }
                }
                 else -> {
                     Toast.makeText(appContext, getString(R.string.error_read), Toast.LENGTH_LONG).show()
                 }
            }
        })

        tts.setOnUtteranceProgressListener(speechListener)

    }

    private fun getSpanSize(
        cardList: List<ImmutableCard?>,
        cardPosition: Int,
        minDefinitionLengthForSpam1: Int,
        minContentLengthForSpam1: Int,
    ): Int {
        val contentSize = cardList[cardPosition]?.cardContent?.content?.length ?: 0
        val definitionSize = getMaxDefinitionLength(cardList[cardPosition]?.cardDefinition)
        if (contentSize > minContentLengthForSpam1 || definitionSize > minDefinitionLengthForSpam1) {
            return 2
        }
        return 1
    }

    private fun getMaxDefinitionLength(definitions: List<CardDefinition>?): Int {
        definitions?.let {
            var maxLength = 0
            it.forEach { defin ->
                val actualLength = defin.definition?.length ?: 0
                if (actualLength > maxLength) {
                    maxLength = actualLength
                }
            }
            return maxLength
        }
        return 0
    }

    private fun onAddNewCard(card: ImmutableCard?) {
        val newCardDialog = deck?.let { NewCardDialog(card, it) }
         newCardDialog?.show(parentFragmentManager, "New Deck Dialog")
    }

    private fun onFullScreen(card: ImmutableCard?, deck: ImmutableDeck) {
        FullScreenCardDialog(card, deck).show(parentFragmentManager, "Full Screen Card")
    }

    override fun getCard(card: ImmutableCard, action: String, deck: ImmutableDeck) {
        if (action == Constant.ADD) {
            //card.deckId = deck.deckId
            cardViewModel.insertCard(card, deck)
        } else {
            cardViewModel.updateCard(card)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.deck_fragment_menu, menu)
        val search = menu.findItem(R.id.search_deck_menu)
        val searchView = search?.actionView as SearchView

        val searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button) as ImageView
        searchIcon.setColorFilter( ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface), PorterDuff.Mode.SRC_IN)

        val searchIconClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        searchIconClose.setColorFilter( ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface), PorterDuff.Mode.SRC_IN)

        val searchIconMag = searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
        searchIconMag.setColorFilter( ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface), PorterDuff.Mode.SRC_IN)

        val topAppBarEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        topAppBarEditText.apply {
            setTextColor(ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface))
            setHintTextColor(ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant))
            hint = getText(R.string.hint_deck_search_field)
        }

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                binding.cardsActivityProgressBar.visibility = View.VISIBLE
                if (p0 != null) {
                    searchDeck(p0)
                    binding.cardsActivityProgressBar.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    searchDeck(p0)
                    binding.cardsActivityProgressBar.visibility = View.GONE
                }
                return true
            }
        })
    }

    private fun searchDeck(query: String) {
        val searchQuery = "%$query%"
        lifecycleScope.launch {
            deck?.let { cardDeck ->
                cardViewModel.searchCard(searchQuery, cardDeck.deckId!!).observe(this@CardFragment) { cardList ->
                    cardList?.let { displayCards(it, cardDeck) }
                }
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return true
    }
}