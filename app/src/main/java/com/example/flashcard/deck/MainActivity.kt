package com.example.flashcard.deck

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.databinding.ActivityMainBinding
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.card.CardViewModel
import com.example.flashcard.card.CardViewModelFactory
import com.example.flashcard.card.NewCardDialog
import com.example.flashcard.util.Constant

class MainActivity : AppCompatActivity(), NewDeckDialog.NewDialogListener,
    NewCardDialog.NewDialogListener {

    private lateinit var binding: ActivityMainBinding

    private val deckViewModel: DeckViewModel by viewModels {
        DeckViewModelFactory((application as FlashCardApplication).repository)
    }

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as FlashCardApplication).repository)
    }
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

        val navController = findNavController(R.id.fragmentContainerView)
        binding.mainActivityBNV.setupWithNavController(navController)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CODE) {
            this.recreate()
        }
    }

    override fun getDeck(deck: Deck, action: String) {
        if (action == "Add") {
            deckViewModel.insertDeck(deck)
        } else {
            deckViewModel.updateDeck(deck)
        }

    }
    private fun getThem(themeName: String?): Int {
        return when (themeName) {
            "DARK THEME" -> R.style.DarkTheme_FlashCard
            "PURPLE THEME" -> R.style.PurpleTheme_Flashcard
            else -> R.style.Theme_FlashCard
        }
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