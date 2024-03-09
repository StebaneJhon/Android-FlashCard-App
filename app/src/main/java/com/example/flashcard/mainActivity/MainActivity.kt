package com.example.flashcard.mainActivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.Card
import com.example.flashcard.databinding.ActivityMainBinding
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.card.CardViewModel
import com.example.flashcard.card.CardViewModelFactory
import com.example.flashcard.card.NewCardDialog
import com.example.flashcard.deck.DeckViewModel
import com.example.flashcard.deck.DeckViewModelFactory
import com.example.flashcard.deck.NewDeckDialog
import com.example.flashcard.util.Constant
import com.example.flashcard.util.SpaceRepetitionAlgorithmHelper
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity(), NewDeckDialog.NewDialogListener,
    NewCardDialog.NewDialogListener {

    private lateinit var binding: ActivityMainBinding

    private val deckViewModel: DeckViewModel by viewModels {
        DeckViewModelFactory((application as FlashCardApplication).repository)
    }

    private val cardViewModel: CardViewModel by viewModels {
        CardViewModelFactory((application as FlashCardApplication).repository)
    }

    private val activityViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory((application as FlashCardApplication).repository)
    }

    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private val SETTINGS_CODE = 12334

    val spaceRepetitionHelper = SpaceRepetitionAlgorithmHelper()

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navController = findNavController(R.id.fragmentContainerView)
        binding.mainActivityBNV.setupWithNavController(navController)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.getAllCards()
                activityViewModel.allCards.collect { state ->
                    when (state) {
                        is UiState.Loading -> {

                        }
                        is UiState.Error -> {
                            Log.i(TAG, state.errorMessage)
                        }
                        is UiState.Success -> {
                            updateCardsStatus(state.data)
                        }
                    }
                }
            }
        }

    }

    private fun updateCardsStatus(data: List<ImmutableCard>) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                data.forEach { card ->
                    updateCardStatus(card)
                }
            }
        }
    }

    fun updateCardStatus(card: ImmutableCard) {
        //val today = today()
        val isCardForgotten = spaceRepetitionHelper.isForgotten(card)
        if (isCardForgotten) {
            val newStatus = spaceRepetitionHelper.status(card, false)
            val nextRevision = spaceRepetitionHelper.nextRevisionDate(card, false)
            val newCard = Card(
                card.cardId,
                card.cardContent,
                card.contentDescription,
                card.cardDefinition,
                card.valueDefinition,
                card.deckId,
                card.backgroundImg,
                card.isFavorite,
                card.revisionTime,
                card.missedTime,
                card.creationDate,
                card.lastRevisionDate,
                newStatus,
                nextRevision
            )
            activityViewModel.updateCard(newCard)
        }
    }

    /*
    private fun today(): String {
        val today = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.format(today)
    }
     */

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

    override fun getCard(card: Card, action: String, deck: ImmutableDeck) {
        if (action == Constant.ADD) {
            card.deckId = deck.deckId
            cardViewModel.insertCard(card, deck)
        } else {
            cardViewModel.updateCard(card)
        }
    }
}