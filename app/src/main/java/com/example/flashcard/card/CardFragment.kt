package com.example.flashcard.card

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.databinding.FragmentCardBinding
import com.example.flashcard.quiz.baseFlashCardGame.BaseFlashCardGame
import com.example.flashcard.util.Constant
import com.example.flashcard.util.UiState
import com.example.flashcard.util.cardBackgroundConst
import kotlinx.coroutines.launch

class CardFragment : Fragment(), NewCardDialog.NewDialogListener {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null

    private val cardViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, CardViewModelFactory(repository)).get(
            CardViewModel::class.java
        )
    }

    val args: CardFragmentArgs by navArgs()
    private var deck: ImmutableDeck? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        appContext = container?.context
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deck = args.selectedDeck
        deck?.let {_deck ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    cardViewModel.getDeckWithCards(_deck.deckId!!)
                    cardViewModel.deckWithAllCards.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.cardsActivityProgressBar.isVisible = true
                            }
                            is UiState.Error -> {
                                binding.cardsActivityProgressBar.isVisible = false
                                Toast.makeText(appContext, state.errorMessage, Toast.LENGTH_LONG).show()
                            }
                            is UiState.Success -> {
                                binding.cardsActivityProgressBar.isVisible = false
                                displayCards(state.data.cards, state.data.deck.toExternal())
                            }
                        }
                    }
                }
            }

            binding.addNewCardBT.setOnClickListener {
                onAddNewCard(null)
            }

            binding.startQuizBT.setOnClickListener {
                _deck.deckId?.let { it1 -> onStartQuiz(it1) }
            }
        }
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

        val flashCardQuiz: Button = dialogBinding.findViewById(R.id.flashCardQuizButton)
        flashCardQuiz.setOnClickListener {
            onStartBaseFlashCardGame(deckId)
        }
    }

    private fun onStartBaseFlashCardGame(deckId: Int) {
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
                            val intent = Intent(appContext, BaseFlashCardGame::class.java)
                            val a = state.data
                            intent.putExtra(BaseFlashCardGame.DECK_ID_KEY, state.data)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun displayCards(cardList: List<Card>, deck: ImmutableDeck) {
        val recyclerViewAdapter = appContext?.let {
            CardsRecyclerViewAdapter(
                it,
                cardList,
                deck,
                { selectedCard ->
                    onFullScreen(selectedCard, deck)
                },
                { selectedCard ->
                    onAddNewCard(selectedCard)
                },
                {selectedCard ->
                    cardViewModel.deleteCard(selectedCard, deck)
                })
        }
        binding.cardRecyclerView.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun onAddNewCard(card: Card?) {
        val newCardDialog = deck?.let { NewCardDialog(card, it) }
         newCardDialog?.show(parentFragmentManager, "New Deck Dialog")
    }

    private fun onFullScreen(card: Card, deck: ImmutableDeck) {
        FullScreenCardDialog(card, deck).show(parentFragmentManager, "Full Screen Card")
    }

    override fun getCard(card: Card, action: String, deck: ImmutableDeck) {
        if (action == Constant.ADD) {
            card.deckId = deck.deckId
            cardViewModel.insertCard(card, deck)
        } else {
            cardViewModel.updateCard(card)
        }
    }
}