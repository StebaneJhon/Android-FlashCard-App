package com.example.flashcard.deck

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.card.CardsActivity
import com.example.flashcard.databinding.ActivityMainBinding
import com.example.flashcard.entities.Deck

class MainActivity : AppCompatActivity(), NewDeckDialog.NewDialogListener {

    private lateinit var binding: ActivityMainBinding

    private val deckViewModel: DeckViewModel by viewModels {
        DeckViewModelFactory((application as FlashCardApplication).repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        deckViewModel.allDecks.observe(this, Observer { deckList ->
            deckList?.let { displayDecks(deckList) }
        })

        binding.addNewDeckButton.setOnClickListener { onAddNewDeck() }

    }

    private fun displayDecks(listOfDecks: List<Deck>) {
        val recyclerViewAdapter = DecksRecyclerViewAdapter(listOfDecks) {
            val intent = Intent(this, CardsActivity::class.java)
            intent.putExtra(CardsActivity.DECK_KEY, it)
            startActivity(intent)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.deckRecycleView)
        recyclerView?.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun onAddNewDeck() {
        val newDeckDialog = NewDeckDialog()
        newDeckDialog.show(supportFragmentManager, "New Deck Dialog")
    }

    override fun getDeck(deck: Deck) {
        deckViewModel.insertDeck(deck)
    }
}