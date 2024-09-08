package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.example.flashcard.quiz.testQuizGame.TestQuizGameActivity
import com.example.flashcard.quiz.writingQuizGame.WritingQuizGameActivity
import com.example.flashcard.util.Constant.MIN_CARD_FOR_MATCHING_QUIZ
import com.example.flashcard.util.Constant.MIN_CARD_FOR_MULTI_CHOICE_QUIZ
import com.example.flashcard.util.DeckAdditionAction.ADD_DECK_FORWARD_TO_CARD_ADDITION
import com.example.flashcard.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.MATCHING_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.MULTIPLE_CHOICE_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.TIMED_FLASH_CARD_QUIZ
import com.example.flashcard.util.FlashCardMiniGameRef.WRITING_QUIZ
import com.example.flashcard.util.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class DeckFragment : Fragment(), MenuProvider {

    private var _binding: FragmentDeckBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null

    private val deckViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, DeckViewModelFactory( repository ))[DeckViewModel::class.java]
    }

    private lateinit var recyclerViewAdapter: DecksRecyclerViewAdapter

    companion object {
        const val TAG = "DeckFragment"
        const val REQUEST_CODE = "0"
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

        binding.deckTopAppBar.setNavigationOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()
        }

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
                                displayDecks(it.data)
                            }
                        }
                    }

            }
        }


        binding.addNewDeckButton.setOnClickListener { onAddNewDeck() }
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
        if (appContext != null) {
            recyclerViewAdapter = DecksRecyclerViewAdapter(listOfDecks, appContext!!, {
                onEditDeck(it)
            }, {deck ->
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
            .setNegativeButton(R.string.bt_cancel) {dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.bt_add_card)) {dialog, _ ->
                navigateTo(deck, NewDeckDialog.TAG)
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateTo(data: ImmutableDeck, opener: String) {
        val action = DeckFragmentDirections.navigateToCardFragment(data, opener)
        findNavController().navigate(action, NavOptions.Builder().setPopUpTo(R.id.deckFragment, true).build())
    }

    private fun onDeleteDeck(deck: ImmutableDeck) {
        appContext?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.dialog_title_delete_deck))
                .setMessage(getString(R.string.dialog_message_delete_deck, deck.cardSum.toString()))
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
    }

    private fun onAddNewDeck() {
        val newDeckDialog = NewDeckDialog(null)
        newDeckDialog.show(childFragmentManager, "New Deck Dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { requestQuey, bundle ->
            val result = bundle.parcelable<OnSaveDeckWithCationModel>(NewDeckDialog.SAVE_DECK_BUNDLE_KEY)
            result?.let { it ->
                deckViewModel.insertDeck(it.deck)
                if(it.action == ADD_DECK_FORWARD_TO_CARD_ADDITION) {
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
        val viewGroup = binding.mainActivityRoot
        val dialogBinding = layoutInflater.inflate(R.layout.quiz_mode_fragment, viewGroup, false)
        val quizModeDialog = appContext?.let { Dialog(it) }

        quizModeDialog?.apply {
            setContentView(dialogBinding)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

        val multiChoiceQuizGameBT: Button = dialogBinding.findViewById(R.id.multiChoiceQuizButton)
        val btWritingQuizGame: Button = dialogBinding.findViewById(R.id.bt_writing_quiz_game)
        val btMatchingQuizGame: Button = dialogBinding.findViewById(R.id.bt_matching_quiz_game)
        val btFlashCard: Button = dialogBinding.findViewById(R.id.bt_flash_card_game)
        val btFlashCardTimed: Button = dialogBinding.findViewById(R.id.bt_flash_card_game_timed)
        val btTestQuizGame: Button = dialogBinding.findViewById(R.id.bt_test_quiz_game)

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
                            binding.mainActivityProgressBar.visibility = View.GONE
                            btFlashCard.setOnClickListener {
                                lunchQuiz(state.data, FLASH_CARD_QUIZ)
                                quizModeDialog?.dismiss()
                            }
                            btFlashCardTimed.setOnClickListener {
                                lunchQuiz(state.data, TIMED_FLASH_CARD_QUIZ)
                                quizModeDialog?.dismiss()
                            }
                            multiChoiceQuizGameBT.setOnClickListener {
                                lunchQuiz(state.data, MULTIPLE_CHOICE_QUIZ)
                                quizModeDialog?.dismiss()
                            }
                            btWritingQuizGame.setOnClickListener {
                                lunchQuiz(state.data, WRITING_QUIZ)
                                quizModeDialog?.dismiss()
                            }
                            btMatchingQuizGame.setOnClickListener {
                                lunchQuiz(state.data, MATCHING_QUIZ)
                                quizModeDialog?.dismiss()
                            }
                            btTestQuizGame.setOnClickListener {
                                lunchQuiz(state.data, QUIZ)
                                quizModeDialog?.dismiss()
                            }
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
                    onStartingQuizError(getString(R.string.error_message_starting_quiz,"$MIN_CARD_FOR_MULTI_CHOICE_QUIZ"), deckWithCards.deck!!)
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
                    onStartingQuizError(getString(R.string.error_message_starting_quiz, "$MIN_CARD_FOR_MATCHING_QUIZ"), deckWithCards.deck!!)
                }
            }
            QUIZ -> {
                val intent = Intent(appContext, TestQuizGameActivity::class.java)
                intent.putExtra(TestQuizGameActivity.DECK_ID_KEY, deckWithCards)
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
        searchIcon.setColorFilter( ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface), PorterDuff.Mode.SRC_IN)

        val searchIconClose: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        searchIconClose.setColorFilter( ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface), PorterDuff.Mode.SRC_IN)

        val searchIconMag: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_go_btn)
        searchIconMag.setColorFilter( ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface), PorterDuff.Mode.SRC_IN)

        val topAppBarEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        topAppBarEditText.apply {
            setTextColor(ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurface))
            setHintTextColor(ThemeUtils.getThemeAttrColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant))
            hint = getText(R.string.hint_card_search_field)
        }

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
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

    private inline fun <reified T: Parcelable> Bundle.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable<T>(key)
    }

}