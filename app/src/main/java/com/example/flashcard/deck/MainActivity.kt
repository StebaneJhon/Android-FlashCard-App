package com.example.flashcard.deck

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.card.CardsActivity
import com.example.flashcard.databinding.ActivityMainBinding
import com.example.flashcard.backend.entities.Deck

class MainActivity : AppCompatActivity(), NewDeckDialog.NewDialogListener,
    SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding

    private val deckViewModel: DeckViewModel by viewModels {
        DeckViewModelFactory((application as FlashCardApplication).repository)
    }

    private lateinit var recyclerViewAdapter: DecksRecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            title = getString(R.string.deck_activity_title)
        }

        binding.mainActivityProgressBar.visibility = View.VISIBLE
        deckViewModel.allDecks.observe(this, Observer { deckList ->
            deckList?.let {
                if (it.isEmpty()) {
                    binding.mainActivityProgressBar.visibility = View.GONE
                    binding.onNoDeckTextHint.visibility = View.VISIBLE
                } else {
                    displayDecks(deckList)
                    binding.mainActivityProgressBar.visibility = View.GONE
                    binding.onNoDeckTextHint.visibility = View.GONE
                }
            }
        })

        binding.addNewDeckButton.setOnClickListener { onAddNewDeck() }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.deck_activity_menu, menu)
        val search = menu?.findItem(R.id.search_deck_menu)
        val searchView = search?.actionView as SearchView

        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)

        return true
    }

    private fun displayDecks(listOfDecks: List<Deck>) {
        recyclerViewAdapter = DecksRecyclerViewAdapter(listOfDecks, this) {
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
}