package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.card.CardsActivity
import com.example.flashcard.databinding.FragmentDeckBinding
import com.example.flashcard.quiz.baseFlashCardGame.BaseFlashCardGame
import com.example.flashcard.util.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.NavigableSet

class DeckFragment : Fragment(), NewDeckDialog.NewDialogListener, SearchView.OnQueryTextListener {

    private var _binding: FragmentDeckBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null

    private val deckViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, DeckViewModelFactory( repository )).get(
            DeckViewModel::class.java
        )
    }

    private lateinit var recyclerViewAdapter: DecksRecyclerViewAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeckBinding.inflate(inflater, container, false)
        appContext = container?.context
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                                binding.mainActivityProgressBar.isVisible = false
                                Toast.makeText(activity?.applicationContext!!, it.errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            is UiState.Success -> {
                                binding.mainActivityProgressBar.isVisible = false
                                displayDecks(it.data)
                            }
                        }
                    }

            }
        }

        binding.addNewDeckButton.setOnClickListener { onAddNewDeck(null) }
    }

    private fun displayDecks(listOfDecks: List<ImmutableDeck>) {
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
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
        }
    }

    private fun navigateTo(data: ImmutableDeck) {
        val action = DeckFragmentDirections.navigateToCardFragment(data)
        Navigation.findNavController(binding.root).navigate(action)

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

        val flashCardQuiz: Button = dialogBinding.findViewById(R.id.flashCardQuizButton)
        flashCardQuiz.setOnClickListener {
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
                                //intent.putExtra(BaseFlashCardGame.DECK_ERROR_KEY, state.errorMessage)
                            }
                            is UiState.Success -> {
                                binding.mainActivityProgressBar.visibility = View.GONE
                                val intent = Intent(activity?.applicationContext!!, BaseFlashCardGame::class.java)
                                val a = state.data
                                intent.putExtra(BaseFlashCardGame.DECK_ID_KEY, state.data)
                                startActivity(intent)
                            }
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
}