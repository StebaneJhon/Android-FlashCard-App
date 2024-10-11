package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ThemeUtils
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
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.databinding.FragmentDeckBinding
import com.example.flashcard.quiz.flashCardGame.FlashCardGameActivity
import com.example.flashcard.quiz.flashCardGameTimed.FlashCardGameTimedActivity
import com.example.flashcard.quiz.matchQuizGame.MatchQuizGameActivity
import com.example.flashcard.quiz.multichoiceQuizGame.MultiChoiceQuizGameActivity
import com.example.flashcard.quiz.quizGame.QuizGameActivity
import com.example.flashcard.quiz.test.TestActivity
import com.example.flashcard.quiz.writingQuizGame.WritingQuizGameActivity
import com.example.flashcard.util.Constant.MIN_CARD_FOR_MATCHING_QUIZ
import com.example.flashcard.util.Constant.MIN_CARD_FOR_MULTI_CHOICE_QUIZ
import com.example.flashcard.util.DeckAdditionAction.ADD_DECK_FORWARD_TO_CARD_ADDITION
import com.example.flashcard.util.DeckRef.DECK_SORT_ALPHABETICALLY
import com.example.flashcard.util.DeckRef.DECK_SORT_BY_CARD_SUM
import com.example.flashcard.util.DeckRef.DECK_SORT_BY_CREATION_DATE
import com.example.flashcard.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.MATCHING_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.MULTIPLE_CHOICE_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.TEST
import com.example.flashcard.util.FlashCardMiniGameRef.TIMED_FLASH_CARD_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.WRITING_QUIZ
import com.example.flashcard.util.QuizModeBottomSheet
import com.example.flashcard.util.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class DeckFragment :
    Fragment(),
    MenuProvider {

    private var _binding: FragmentDeckBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null

    private val deckViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        val openTriviaRepository =
            (requireActivity().application as FlashCardApplication).openTriviaRepository
        ViewModelProvider(
            this,
            DeckViewModelFactory(repository, openTriviaRepository)
        )[DeckViewModel::class.java]
    }

    private lateinit var recyclerViewAdapter: DecksRecyclerViewAdapter

    var deckSharedPref: SharedPreferences? = null
    var deckSharedPrefEditor: SharedPreferences.Editor? = null

    companion object {
        const val TAG = "DeckFragment"
        const val REQUEST_CODE = "0"
        const val REQUEST_CODE_QUIZ_MODE = "300"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeckBinding.inflate(inflater, container, false)
        appContext = container?.context
        return binding.root
    }

    @SuppressLint("DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).setSupportActionBar(binding.deckTopAppBar)

        deckSharedPref = activity?.getSharedPreferences("deckSharedpref", Context.MODE_PRIVATE)
        deckSharedPrefEditor = deckSharedPref?.edit()

        binding.deckTopAppBar.setNavigationOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                deckViewModel.getAllDecks()
                deckViewModel.allDecks
                    .collect { it ->
                        when (it) {
                            is UiState.Loading -> {
                                binding.mainActivityProgressBar.isVisible = true
                            }

                            is UiState.Error -> {
                                onNoDeckError()
                            }

                            is UiState.Success -> {
                                displayDecks(it.data)
                            }
                        }
                    }

            }
        }

        binding.addNewDeckButton.setOnClickListener { onAddNewDeck() }

        binding.bottomAppBarDeck.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bt_sort_deck -> {
                    val item: View = binding.bottomAppBarDeck.findViewById(R.id.bt_sort_deck)
                    showMenu(item, R.menu.menu_filter_deck)
                    true
                }

                R.id.bt_upload_deck_with_cards -> {
                    showTriviaQuestionUploader()
                    true
                }

                else -> false
            }
        }

    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.bt_filter_alphabetically -> {
                    setDeckSort(DECK_SORT_ALPHABETICALLY)
                    activity?.recreate()
                    true
                }

                R.id.bt_filter_by_card_sum -> {
                    setDeckSort(DECK_SORT_BY_CARD_SUM)
                    activity?.recreate()
                    true
                }

                R.id.bt_filter_by_creation_date -> {
                    setDeckSort(DECK_SORT_BY_CREATION_DATE)
                    activity?.recreate()
                    true
                }

                else -> {
                    false
                }
            }
        }
        popup.show()
    }

    private fun sortDeckBy(filter: String, decks: List<ImmutableDeck>): List<ImmutableDeck> {
        return when (filter) {
            DECK_SORT_ALPHABETICALLY -> {
                decks.sortedBy { d -> d.deckName?.lowercase() }
            }

            DECK_SORT_BY_CARD_SUM -> {
                decks.sortedBy { d -> d.cardSum }
            }

            else -> {
                decks
            }
        }
    }

    private fun setDeckSort(filter: String) {
        deckSharedPrefEditor?.apply {
            putString("sort", filter)
            apply()
        }
    }

    private fun showTriviaQuestionUploader() {
        val newDeckDialog = UploadOpenTriviaQuizDialog()
        newDeckDialog.show(childFragmentManager, "upload open trivia quiz dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { requstQuery, bundle ->
            val result =
                bundle.parcelable<OpenTriviaQuizModel>(UploadOpenTriviaQuizDialog.OPEN_TRIVIA_QUIZ_MODEL_BUNDLE_KEY)
            lifecycleScope.launch {
                deckViewModel.getOpenTriviaQuestions(
                    result?.number!!,
                    result.category,
                    result.difficulty,
                    result.type
                )
                deckViewModel.openTriviaResponse.collect { response ->
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
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }

                        is UiState.Loading -> {
                            binding.mainActivityProgressBar.isVisible = true
                        }

                        is UiState.Success -> {
                            deckViewModel.insertOpenTriviaQuestions(
                                result.deckName,
                                result.difficulty,
                                response.data.results
                            )
                            binding.mainActivityProgressBar.visibility = View.GONE
                        }
                    }
                }
            }

        }
    }


    private fun onNoDeckError() {
        binding.mainActivityProgressBar.visibility = View.GONE
        binding.deckRecycleView.visibility = View.GONE
        binding.onNoDeckTextError.visibility = View.VISIBLE
    }

    private fun displayDecks(listOfDecks: List<ImmutableDeck>) {
        binding.mainActivityProgressBar.visibility = View.GONE
        binding.deckRecycleView.visibility = View.VISIBLE
        binding.onNoDeckTextError.visibility = View.GONE
        val sortedListOfDeck =
            sortDeckBy(deckSharedPref?.getString("sort", DECK_SORT_BY_CREATION_DATE)!!, listOfDecks)
        if (appContext != null) {
            recyclerViewAdapter = DecksRecyclerViewAdapter(sortedListOfDeck, appContext!!, {
                onEditDeck(it)
            }, { deck ->
                onDeleteDeck(deck)
            }, { deck ->
                onStartQuiz(deck.deckId)
            }) {
                navigateTo(it, TAG)
            }
            val recyclerView = binding.deckRecycleView
            recyclerView.apply {
                adapter = recyclerViewAdapter
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(appContext)
            }
        }
    }

    private fun onStartingQuizError(errorText: String, deck: ImmutableDeck) {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(getString(R.string.error_title_starting_quiz))
            .setMessage(errorText)
            .setNegativeButton(R.string.bt_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.bt_add_card)) { dialog, _ ->
                navigateTo(deck, NewDeckDialog.TAG)
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateTo(data: ImmutableDeck, opener: String) {
        val action = DeckFragmentDirections.navigateToCardFragment(data, opener)
        findNavController().navigate(
            action,
            NavOptions.Builder().setPopUpTo(R.id.deckFragment, true).build()
        )
    }

    private fun onDeleteDeck(deck: ImmutableDeck) {
        if (deck.cardSum!! > 0) {
            appContext?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle(getString(R.string.dialog_title_delete_deck))
                    .setMessage(
                        if (deck.cardSum > 1) {
                            getString(R.string.dialog_message_delete_deck, deck.cardSum.toString(), "s")
                        } else {
                            getString(R.string.dialog_message_delete_deck, deck.cardSum.toString(), "")
                        }
                    )
                    .setNegativeButton(getString(R.string.bt_text_cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.bt_text_delete)) { dialog, _ ->
                        deckViewModel.deleteDeck(deck)
                        dialog.dismiss()
                        Toast.makeText(it, "Delete ${deck.deckName}", Toast.LENGTH_LONG).show()
                    }
                    .show()
            }
        } else {
            deckViewModel.deleteDeck(deck)
            Toast.makeText(requireContext(), "Delete ${deck.deckName}", Toast.LENGTH_LONG).show()
        }
    }

    private fun onAddNewDeck() {
        val newDeckDialog = NewDeckDialog(null)
        newDeckDialog.show(childFragmentManager, "New Deck Dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { requestQuey, bundle ->
            val result =
                bundle.parcelable<OnSaveDeckWithCationModel>(NewDeckDialog.SAVE_DECK_BUNDLE_KEY)
            result?.let { it ->
                deckViewModel.insertDeck(it.deck)
                if (it.action == ADD_DECK_FORWARD_TO_CARD_ADDITION) {
                    navigateTo(it.deck.toExternal(), NewDeckDialog.TAG)
                }
            }
        }
    }

    private fun onEditDeck(deck: ImmutableDeck?) {
        val newDeckDialog = NewDeckDialog(deck)
        newDeckDialog.show(childFragmentManager, "Edit Deck Dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { requestQuey, bundle ->
            val result = bundle.parcelable<Deck>(NewDeckDialog.EDIT_DECK_BUNDLE_KEY)
            result?.let { it ->
                deckViewModel.updateDeck(it)
            }
        }
    }

    private fun searchDeck(query: String) {
        val searchQuery = "%$query%"
        deckViewModel.searchDeck(searchQuery).observe(this) { deckList ->
            deckList?.let { displayDecks(it) }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun onStartQuiz(deckId: String) {
        val quizMode = QuizModeBottomSheet()
        quizMode.show(childFragmentManager, "Quiz Mode")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_QUIZ_MODE,
            this
        ) { _, bundle ->
            val result = bundle.getString(QuizModeBottomSheet.START_QUIZ_BUNDLE_KEY)
            result?.let { qm ->
                startQuiz(deckId) { deckWithCards ->
                    lunchQuiz(deckWithCards, qm)
                }
            }
        }
    }

    private fun startQuiz(deckId: String, start: (ImmutableDeckWithCards) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                deckViewModel.getDeckWithCards(deckId)
                deckViewModel.deckWithAllCards.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.mainActivityProgressBar.visibility = View.VISIBLE
                        }

                        is UiState.Error -> {
                            binding.mainActivityProgressBar.visibility = View.GONE
                        }

                        is UiState.Success -> {
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
                        ), deckWithCards.deck!!
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
                        ), deckWithCards.deck!!
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ResourceType")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.deck_fragment_menu, menu)
        val search = menu.findItem(R.id.search_deck_menu)
        val searchView: SearchView = search.actionView as SearchView

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
            hint = getText(R.string.hint_card_search_field)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.mainActivityProgressBar.visibility = View.VISIBLE
                if (query != null) {
                    searchDeck(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchDeck(newText)
                }
                return true
            }

        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return true
    }

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable<T>(key)
    }

}