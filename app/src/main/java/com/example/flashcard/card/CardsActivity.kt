package com.example.flashcard.card

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.databinding.ActivityCardsBinding
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.relations.DeckWithCards

class CardsActivity : AppCompatActivity(), NewCardDialog.NewDialogListener {

    private lateinit var binding: ActivityCardsBinding
    private var deck: Deck? = null

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
            cardViewModel.getDeckWithCards(it.deckId!!)
            cardViewModel.deckWithAllCards.observe(this, Observer { cardList ->
                cardList?.let { displayCards(cardList[0].cards, cardList[0].deck) }
            })

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
        cardViewModel.insertCard(card)
        val updatedDeck = deck
        updatedDeck?.cardSum = updatedDeck?.cardSum?.plus(1)
        cardViewModel.updateDeck(updatedDeck!!)
    }

    companion object {
        val DECK_KEY = "deckIdKey"
    }
}