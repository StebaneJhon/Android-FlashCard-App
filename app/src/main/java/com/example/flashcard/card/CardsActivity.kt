package com.example.flashcard.card

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.databinding.ActivityCardsBinding
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.util.Async
import kotlinx.coroutines.launch

class CardsActivity : AppCompatActivity(), NewCardDialog.NewDialogListener {

    private lateinit var binding: ActivityCardsBinding
    private var deck: ImmutableDeck? = null

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as FlashCardApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deck = intent?.getParcelableExtra(DECK_KEY)
        deck?.let {
            supportActionBar?.title = deck?.deckName
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    cardViewModel.getDeckWithCards(it.deckId!!)
                    cardViewModel.deckWithAllCards.collect { state ->
                        when (state) {
                            is Async.Loading -> {
                                binding.cardsActivityProgressBar.isVisible = true
                            }
                            is Async.Error -> {
                                binding.cardsActivityProgressBar.isVisible = false
                                Toast.makeText(this@CardsActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                            }
                            is Async.Success -> {
                                binding.cardsActivityProgressBar.isVisible = false
                                displayCards(state.data[0].cards, state.data[0].deck)
                            }
                        }
                    }
                }
            }

            binding.addNewCardBT.setOnClickListener {
                onAddNewCard()
            }
        }
    }

    private fun displayCards(cardList: List<Card>, deck: Deck) {
        val recyclerViewAdapter = CardsRecyclerViewAdapter(cardList, deck)
        binding.cardRecyclerView.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun onAddNewCard() {
        val newCardDialog = NewCardDialog()
        newCardDialog.show(supportFragmentManager, "New Card Dialog")
    }

    override fun getCard(card: Card) {
        card.deckId = deck?.deckId
        cardViewModel.insertCard(card, deck!!)
    }

    companion object {
        val DECK_KEY = "deckIdKey"
    }
}