package com.example.flashcard.card

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.Model.toLocal
import com.example.flashcard.databinding.ActivityCardsBinding
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.settings.SettingsActivity
import com.example.flashcard.util.UiState
import com.example.flashcard.util.Constant
import kotlinx.coroutines.launch

class CardsActivity : AppCompatActivity(), NewCardDialog.NewDialogListener, SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityCardsBinding
    private var deck: ImmutableDeck? = null
    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as FlashCardApplication).repository)
    }

    private val SETTINGS_CODE = 12334

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = getThem(appTheme)
        setTheme(themRef)

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
                            is UiState.Loading -> {
                                binding.cardsActivityProgressBar.isVisible = true
                            }
                            is UiState.Error -> {
                                binding.cardsActivityProgressBar.isVisible = false
                                Toast.makeText(this@CardsActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                            }
                            is UiState.Success -> {
                                binding.cardsActivityProgressBar.isVisible = false
                                displayCards(state.data[0].cards, state.data[0].deck.toExternal())
                            }
                        }
                    }
                }
            }

            binding.addNewCardBT.setOnClickListener {
                onAddNewCard(null)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.deck_activity_menu, menu)
        val search = menu?.findItem(R.id.search_deck_menu)
        val searchView = search?.actionView as SearchView

        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings_button_menu -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, SETTINGS_CODE)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CODE) {
            this.recreate()
        }
    }

    private fun displayCards(cardList: List<Card>, deck: ImmutableDeck) {
        val recyclerViewAdapter = CardsRecyclerViewAdapter(
            cardList,
            deck,
            {
                onFullScreen(it, deck)
            },
            {
                onAddNewCard(it)
            },
            {
                cardViewModel.deleteCard(it, deck)
            })
        binding.cardRecyclerView.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun onAddNewCard(card: Card?) {
        val newCardDialog = NewCardDialog(card)
        newCardDialog.show(supportFragmentManager, "New Card Dialog")
    }

    private fun onFullScreen(card: Card, deck: ImmutableDeck) {
        FullScreenCardDialog(card, deck)
            .show(supportFragmentManager, "Full Screen Card")
    }

    override fun getCard(card: Card, action: String) {
        if (action == Constant.ADD) {
            card.deckId = deck?.deckId
            cardViewModel.insertCard(card, deck!!)
        } else {
            cardViewModel.updateCard(card)
        }
    }

    private fun getThem(themeName: String?): Int {
        return when (themeName) {
            "DARK THEME" -> R.style.DarkTheme_FlashCard
            else -> R.style.Theme_FlashCard
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        binding.cardsActivityProgressBar.visibility = View.VISIBLE
        if (query != null) {
            searchDeck(query)
            binding.cardsActivityProgressBar.visibility = View.GONE
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchDeck(newText)
            binding.cardsActivityProgressBar.visibility = View.GONE
        }
        return true
    }

    private fun searchDeck(query: String) {
        val searchQuery = "%$query%"
        deck?.let { cardDeck ->
            cardViewModel.searchCard(searchQuery, cardDeck.deckId!!).observe(this) { cardList ->
                cardList?.let { displayCards(it.toLocal(), cardDeck) }
            }
        }

    }

    companion object {
        val DECK_KEY = "deckIdKey"
    }

}