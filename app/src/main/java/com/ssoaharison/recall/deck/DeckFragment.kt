package com.ssoaharison.recall.deck

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.models.toExternal
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.databinding.FragmentDeckBinding
import com.ssoaharison.recall.quiz.flashCardGame.FlashCardGameActivity
import com.ssoaharison.recall.quiz.matchQuizGame.MatchQuizGameActivity
import com.ssoaharison.recall.quiz.multichoiceQuizGame.MultiChoiceQuizGameActivity
import com.ssoaharison.recall.quiz.quizGame.QuizGameActivity
import com.ssoaharison.recall.quiz.test.TestActivity
import com.ssoaharison.recall.quiz.writingQuizGame.WritingQuizGameActivity
import com.ssoaharison.recall.util.Constant.MIN_CARD_FOR_MATCHING_QUIZ
import com.ssoaharison.recall.util.Constant.MIN_CARD_FOR_MULTI_CHOICE_QUIZ
import com.ssoaharison.recall.util.DeckAdditionAction.ADD_DECK_FORWARD_TO_CARD_ADDITION
import com.ssoaharison.recall.util.DeckRef.DECK_SORT_ALPHABETICALLY
import com.ssoaharison.recall.util.DeckRef.DECK_SORT_BY_CARD_SUM
import com.ssoaharison.recall.util.DeckRef.DECK_SORT_BY_CREATION_DATE
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssoaharison.recall.util.Constant.MIN_CARD_FOR_TEST
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.ssoaharison.recall.util.parcelable
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class DeckFragment :
    Fragment(),
    MenuProvider {

    private var _binding: FragmentDeckBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private var startQuizJob: Job? = null

    private val deckViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(
            this,
            DeckViewModelFactory(repository)
        )[DeckViewModel::class.java]
    }

    private lateinit var recyclerViewAdapter: DecksRecyclerViewAdapter

    private var deckSharedPref: SharedPreferences? = null
    private var deckSharedPrefEditor: SharedPreferences.Editor? = null

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

        val window = activity?.window
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val deckColorSurfaceLow = DeckColorCategorySelector().selectDeckColorSurfaceContainerLow(requireContext(), null)
        window?.statusBarColor = deckColorSurfaceLow

        deckSharedPref = activity?.getSharedPreferences("deckSharedpref", Context.MODE_PRIVATE)
        deckSharedPrefEditor = deckSharedPref?.edit()

        binding.deckTopAppBar.setNavigationOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()
        }

        binding.addNewDeckButton.setOnClickListener { onAddNewDeck() }

        displayAllDecks()

    }

    private fun displayAllDecks() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                deckViewModel.getAllDecks()
                deckViewModel.allDecks
                    .collect {
                        when (it) {
                            is UiState.Loading -> {
                                binding.mainActivityProgressBar.isVisible = true
                            }

                            is UiState.Error -> {
                                onNoDeckError()
                            }

                            is UiState.Success -> {
                                populateRecyclerView(it.data)
                            }
                        }
                    }

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

    private fun onNoDeckError() {
        binding.mainActivityProgressBar.visibility = View.GONE
        binding.deckRecycleView.visibility = View.GONE
        binding.tvNoDeckFound.visibility = View.GONE
        binding.onNoDeckTextError.visibility = View.VISIBLE
    }

    @SuppressLint("RestrictedApi")
    private fun populateRecyclerView(listOfDecks: List<ImmutableDeck>) {
        binding.mainActivityProgressBar.visibility = View.GONE
        binding.deckRecycleView.visibility = View.VISIBLE
        binding.onNoDeckTextError.visibility = View.GONE
        binding.tvNoDeckFound.visibility = View.GONE
        val sortedListOfDeck =
            sortDeckBy(deckSharedPref?.getString("sort", DECK_SORT_BY_CREATION_DATE)!!, listOfDecks)
        val sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appThemeName = sharedPref?.getString("themName", "WHITE THEM")
        if (appContext != null) {
            recyclerViewAdapter = DecksRecyclerViewAdapter(sortedListOfDeck, appContext!!, appThemeName, {
                onEditDeck(it)
            }, { deck ->
                onDeleteDeck(deck)
            }, { deck ->
                onStartQuiz(deck)
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
                            getString(
                                R.string.dialog_message_delete_deck,
                                deck.cardSum.toString(),
                                "s"
                            )
                        } else {
                            getString(
                                R.string.dialog_message_delete_deck,
                                deck.cardSum.toString(),
                                ""
                            )
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
            val result = bundle.parcelable<OnSaveDeckWithCationModel>(NewDeckDialog.SAVE_DECK_BUNDLE_KEY)
            result?.let {
                deckViewModel.insertDeck(it.deck)
                if (it.action == ADD_DECK_FORWARD_TO_CARD_ADDITION) {
                    navigateTo(it.deck.toExternal(0, 0, 0), NewDeckDialog.TAG)
                }
            }
        }
    }

    private fun onEditDeck(deck: ImmutableDeck?) {
        val newDeckDialog = NewDeckDialog(deck)
        newDeckDialog.show(childFragmentManager, "Edit Deck Dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { requestQuey, bundle ->
            val result = bundle.parcelable<Deck>(NewDeckDialog.EDIT_DECK_BUNDLE_KEY)
            result?.let {
                deckViewModel.updateDeck(it)
            }
        }
    }

    private fun searchDeck(query: String) {
        val searchQuery = "%$query%"
        if (searchQuery.isBlank() || searchQuery.isEmpty()) {
            displayAllDecks()
        } else {
            displaySearchFoundDeck(searchQuery)
        }
    }

    private fun displaySearchFoundDeck(searchQuery: String) {
        deckViewModel.searchDeck(searchQuery).observe(this) { deckList ->
            if (deckList.isNullOrEmpty()) {
                errorOnDeckNotFound()
            } else {
                populateRecyclerView(deckList.toList())
            }
        }
    }

    private fun errorOnDeckNotFound() {
        binding.mainActivityProgressBar.visibility = View.GONE
        binding.deckRecycleView.visibility = View.GONE
        binding.onNoDeckTextError.visibility = View.GONE
        binding.tvNoDeckFound.visibility = View.VISIBLE
    }

    @SuppressLint("MissingInflatedId")
    private fun onStartQuiz(deck: ImmutableDeck) {
        val quizMode = QuizModeBottomSheet(deck)
        quizMode.show(childFragmentManager, "Quiz Mode")
        childFragmentManager.setFragmentResultListener(
            REQUEST_CODE_QUIZ_MODE,
            this
        ) { _, bundle ->
            val result = bundle.getString(QuizModeBottomSheet.START_QUIZ_BUNDLE_KEY)
            result?.let { qm ->
                startQuiz(deck) { deckWithCards ->
                    lunchQuiz(deckWithCards, qm)
                }
            }
        }
    }

    private fun startQuiz(deck: ImmutableDeck, start: (ImmutableDeckWithCards) -> Unit) {
        startQuizJob?.cancel()
        startQuizJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                deckViewModel.getDeckWithCards(deck.deckId)
                deckViewModel.deckWithAllCards.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.mainActivityProgressBar.visibility = View.VISIBLE
                        }

                        is UiState.Error -> {
                            binding.mainActivityProgressBar.visibility = View.GONE
                            onStartingQuizError(getString(R.string.error_message_empty_deck), deck)
                            this@launch.cancel()
                            this.cancel()
                        }

                        is UiState.Success -> {
                            binding.mainActivityProgressBar.visibility = View.GONE
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
                if (deckWithCards.cards?.size!! >= MIN_CARD_FOR_TEST) {
                    val intent = Intent(appContext, TestActivity::class.java)
                    intent.putExtra(TestActivity.DECK_ID_KEY, deckWithCards)
                    startActivity(intent)
                } else {
                    onStartingQuizError(
                        getString(
                            R.string.error_message_starting_quiz,
                            "$MIN_CARD_FOR_TEST"
                        ),
                        deckWithCards.deck!!
                    )
                }

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
        searchView.setOnCloseListener {
            displayAllDecks()
            true
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.settings_deck_menu -> {
                findNavController().navigate(R.id.action_deckFragment_to_settingsFragment)
                true
            }
            R.id.sort_deck_menu -> {
                val sortButton: ActionMenuItemView = binding.deckTopAppBar.findViewById(R.id.sort_deck_menu)
                showMenu(sortButton, R.menu.menu_filter_deck)
                true
            }
            else -> true
        }
    }

}