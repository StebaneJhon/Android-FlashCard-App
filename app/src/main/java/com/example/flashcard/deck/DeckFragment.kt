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
                onStartQuiz(deck.deckId!!)
            }) {
                navigateTo(it, TAG)
            }
            val recyclerView = binding.deckRecycleView
            recyclerView.apply {
                adapter = recyclerViewAdapter
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager( appContext)
            }
        }
    }

    private fun navigateTo(data: ImmutableDeck, opener: String) {
        val action = DeckFragmentDirections.navigateToCardFragment(data, opener)
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

    private fun onAddNewDeck() {
        val newDeckDialog = NewDeckDialog(null)
        newDeckDialog.show(childFragmentManager, "New Deck Dialog")
        childFragmentManager.setFragmentResultListener(REQUEST_CODE, this) { requestQuey, bundle ->
            val result = bundle.parcelable<Deck>(NewDeckDialog.SAVE_DECK_BUNDLE_KEY)
            result?.let { it ->
                deckViewModel.insertDeck(it)
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

//    override fun getDeckAndAddCards(deck: Deck, opener: String) {
//        deckViewModel.insertDeck(deck)
//        val externalDeck = deckViewModel.getDeckByName(deck.deckName!!)
//        navigateTo(externalDeck, opener)
//    }

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

    private inline fun <reified T: Parcelable> Bundle.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable<T>(key)
    }

}