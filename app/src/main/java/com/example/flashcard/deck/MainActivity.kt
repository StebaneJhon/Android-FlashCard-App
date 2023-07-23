package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.card.CardsActivity
import com.example.flashcard.databinding.ActivityMainBinding
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.quiz.baseFlashCardGame.BaseFlashCardGame
import com.example.flashcard.settings.SettingsActivity
import com.example.flashcard.util.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NewDeckDialog.NewDialogListener,
    SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding

    private val deckViewModel: DeckViewModel by viewModels {
        DeckViewModelFactory((application as FlashCardApplication).repository)
    }

    private lateinit var recyclerViewAdapter: DecksRecyclerViewAdapter
    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private val SETTINGS_CODE = 12334

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = getThem(appTheme)
        setTheme(themRef)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            title = getString(R.string.deck_activity_title)
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
                                binding.mainActivityProgressBar.isVisible = false
                                Toast.makeText(this@MainActivity, it.errorMessage, Toast.LENGTH_SHORT).show()
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

    private fun displayDecks(listOfDecks: List<ImmutableDeck>) {
        recyclerViewAdapter = DecksRecyclerViewAdapter(listOfDecks, this, {
            onAddNewDeck(it)
            Toast.makeText(this, "Edit ${it.deckName}", Toast.LENGTH_LONG).show()

        }, {deck ->
            onDeleteDeck(deck)
        }, { deck ->
            onStartQuiz(deck.deckId!!)
        }) {
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

    private fun onDeleteDeck(deck: ImmutableDeck) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Deck?")
            .setMessage("The deck an all cards in it will be deleted")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                deckViewModel.deleteDeck(deck)
                dialog.dismiss()
                Toast.makeText(this, "Delete ${deck.deckName}", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun onAddNewDeck(deck: ImmutableDeck?) {
        val newDeckDialog = NewDeckDialog(deck)
        newDeckDialog.show(supportFragmentManager, "New Deck Dialog")
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

    private fun getThem(themeName: String?): Int {
        return when (themeName) {
            "DARK THEME" -> R.style.DarkTheme_FlashCard
            "PURPLE THEME" -> R.style.PurpleTheme_Flashcard
            else -> R.style.Theme_FlashCard
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun onStartQuiz(deckId: Int) {
        val viewGroup = binding.mainActivityRoot
        val dialogBinding = layoutInflater.inflate(R.layout.quiz_mode_fragment, viewGroup, false)
        val quizModeDialog = Dialog(this)

        quizModeDialog.apply {
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
                                intent.putExtra(BaseFlashCardGame.DECK_ERROR_KEY, state.errorMessage)
                            }
                            is UiState.Success -> {
                                binding.mainActivityProgressBar.visibility = View.GONE
                                val intent = Intent(this@MainActivity, BaseFlashCardGame::class.java)
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
}