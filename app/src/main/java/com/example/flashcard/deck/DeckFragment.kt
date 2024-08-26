package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.databinding.FragmentDeckBinding
import com.example.flashcard.quiz.flashCardGame.FlashCardGameActivity
import com.example.flashcard.quiz.flashCardGameTimed.FlashCardGameTimedActivity
import com.example.flashcard.quiz.matchQuizGame.MatchQuizGameActivity
import com.example.flashcard.quiz.multichoiceQuizGame.MultiChoiceQuizGameActivity
import com.example.flashcard.quiz.testQuizGame.TestQuizGameActivity
import com.example.flashcard.quiz.testQuizGame.TestQuizGameAdapter
import com.example.flashcard.quiz.writingQuizGame.WritingQuizGameActivity
import com.example.flashcard.util.UiState
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class DeckFragment : Fragment(), NewDeckDialog.NewDialogListener, MenuProvider {

    private var _binding: FragmentDeckBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null

    private val deckViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, DeckViewModelFactory( repository ))[DeckViewModel::class.java]
    }

    private lateinit var recyclerViewAdapter: DecksRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeckBinding.inflate(inflater, container, false)
        appContext = container?.context
        activity?.findViewById<BottomNavigationView>(R.id.mainActivityBNV)?.isVisible = true
        return binding.root
    }

    @SuppressLint("DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).setSupportActionBar(binding.deckTopAppBar)

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


        binding.addNewDeckButton.setOnClickListener { onAddNewDeck(null) }
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
                onAddNewDeck(it)
            }, {deck ->
                onDeleteDeck(deck)
            }, { deck ->
                onStartQuiz(deck.deckId!!)
            }) {
                navigateTo(it)
            }
            val recyclerView = binding.deckRecycleView
            recyclerView.apply {
                adapter = recyclerViewAdapter
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager( appContext)
            }
        }
    }

    private fun navigateTo(data: ImmutableDeck) {
        val action = DeckFragmentDirections.navigateToCardFragment(data)
        findNavController().navigate(action, NavOptions.Builder().setPopUpTo(R.id.deckFragment, true).build())

    }

    private fun onDeleteDeck(deck: ImmutableDeck) {
        appContext?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Delete Deck?")
                .setMessage("The deck an all cards in it will be deleted")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Delete") { dialog, _ ->
                    deckViewModel.deleteDeck(deck)
                    dialog.dismiss()
                    Toast.makeText(it, "Delete ${deck.deckName}", Toast.LENGTH_LONG).show()
                }
                .show()
        }
    }

    private fun onAddNewDeck(deck: ImmutableDeck?) {
        val newDeckDialog = NewDeckDialog(deck)
        newDeckDialog.show(parentFragmentManager, "New Deck Dialog")
    }

    override fun getDeck(deck: Deck, action: String) {
        if (action == "Add") {
            deckViewModel.insertDeck(deck)
        } else {
            deckViewModel.updateDeck(deck)
        }

    }

    private fun searchDeck(query: String) {
        val searchQuery = "%$query%"
        deckViewModel.searchDeck(searchQuery).observe(this) { deckList ->
            deckList?.let { displayDecks(it) }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun onStartQuiz(deckId: Int) {
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
        multiChoiceQuizGameBT.setOnClickListener {
            startMultiChoiceQuizGame(deckId, quizModeDialog)
        }

        val btWritingQuizGame: Button = dialogBinding.findViewById(R.id.bt_writing_quiz_game)
        btWritingQuizGame.setOnClickListener {
            startWritingQuizGame(deckId, quizModeDialog)
        }

        val btMatchingQuizGame: Button = dialogBinding.findViewById(R.id.bt_matching_quiz_game)
        btMatchingQuizGame.setOnClickListener {
            startMatchingQuizGame(deckId, quizModeDialog)
        }

        val btFlashCard: Button = dialogBinding.findViewById(R.id.bt_flash_card_game)
        btFlashCard.setOnClickListener {
            startFlashCardGame(deckId, quizModeDialog)
        }

        val btFlashCardTimed: Button = dialogBinding.findViewById(R.id.bt_flash_card_game_timed)
        btFlashCardTimed.setOnClickListener {
            startFlashCardGameTimed(deckId, quizModeDialog)
        }

        val btTestQuizGame: Button = dialogBinding.findViewById(R.id.bt_test_quiz_game)
        btTestQuizGame.setOnClickListener {
            startTestQuizGame(deckId, quizModeDialog)
        }
    }

    private fun startFlashCardGameTimed(deckId: Int, quizModeDialog: Dialog?) {
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
                            val intent = Intent(
                                activity?.applicationContext!!,
                                FlashCardGameTimedActivity::class.java
                            )
                            intent.putExtra(FlashCardGameTimedActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            quizModeDialog?.dismiss()
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startFlashCardGame(deckId: Int, quizModeDialog: Dialog?) {
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
                            val intent = Intent(
                                activity?.applicationContext!!,
                                FlashCardGameActivity::class.java
                            )
                            intent.putExtra(FlashCardGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            quizModeDialog?.dismiss()
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }



    private fun startMultiChoiceQuizGame(deckId: Int, quizModeDialog: Dialog?) {
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
                            val intent = Intent(
                                activity?.applicationContext!!,
                                MultiChoiceQuizGameActivity::class.java
                            )
                            intent.putExtra(MultiChoiceQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            quizModeDialog?.dismiss()
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startWritingQuizGame(deckId: Int, quizModeDialog: Dialog?) {
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
                            val intent = Intent(
                                activity?.applicationContext!!,
                                WritingQuizGameActivity::class.java
                            )
                            intent.putExtra(WritingQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            quizModeDialog?.dismiss()
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startMatchingQuizGame(deckId: Int, quizModeDialog: Dialog?) {
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
                            val intent = Intent(
                                activity?.applicationContext!!,
                                MatchQuizGameActivity::class.java
                            )
                            intent.putExtra(MatchQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            quizModeDialog?.dismiss()
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun startTestQuizGame(deckId: Int, quizModeDialog: Dialog?) {
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
                            val intent = Intent(
                                activity?.applicationContext!!,
                                TestQuizGameActivity::class.java
                            )
                            intent.putExtra(TestQuizGameActivity.DECK_ID_KEY, state.data)
                            startActivity(intent)
                            quizModeDialog?.dismiss()
                            this@launch.cancel()
                            this.cancel()
                        }
                    }
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
}