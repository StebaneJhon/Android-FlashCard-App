package com.example.flashcard.quiz.baseFlashCardGame

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.text.BoringLayout
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.Deck
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityBaseFlashCardGameBinding
import com.example.flashcard.util.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.parcelize.parcelableCreator

class BaseFlashCardGame : AppCompatActivity() {

    private lateinit var binding: ActivityBaseFlashCardGameBinding
    private val baseGameViewModel: BaseFlashCardGameViewModel by viewModels {
        BaseFlashCardGameViewModelFactory((application as FlashCardApplication).repository)
    }

    private var deckWithCards: DeckWithCards? = null
    private var cardList: List<ImmutableCard>? = null
    private var deck: ImmutableDeck? = null
    private var cardFliped: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFlashCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            cardList = it.cards.toExternal()
            deck = it.deck.toExternal()
            lifecycleScope.launch {
                displayNextCard(cardList!!, null)
            }

            binding.yesButton.setOnClickListener {
                lifecycleScope.launch {
                    displayNextCard(cardList!!, true)
                }
            }
            binding.noButton.setOnClickListener {
                lifecycleScope.launch {
                    displayNextCard(cardList!!, false)
                }
            }
            binding.card.setOnClickListener {
                lifecycleScope.launch {
                    flipCard()
                }
            }
        }

    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    private suspend fun displayNextCard(cardList: List<ImmutableCard>, isActualCardKnown: Boolean?) {
        if (isActualCardKnown == true) {
            baseGameViewModel.onCardKnown()
        } else if (isActualCardKnown == false) {
            baseGameViewModel.onCardUnknown(cardList)
        }

        cardFliped = false
        baseGameViewModel.getActualCard(cardList)
        baseGameViewModel.actualCard.collect { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.cardProgressBar.visibility = View.VISIBLE
                }

                is UiState.Error -> {
                    binding.cardProgressBar.visibility = View.GONE
                    binding.onCardText.visibility = View.GONE
                    binding.onCardTextDefinition.text = state.errorMessage
                }

                is UiState.Success -> {
                    binding.cardProgressBar.visibility = View.GONE
                    val card = state.data
                    binding.onCardText.visibility = View.VISIBLE
                    binding.onCardText.text = card.cardContent
                    binding.onCardTextDefinition.text = card.contentDescription
                    binding.languageHint.text = deck?.deckFirstLanguage
                }
            }
        }
    }

    private suspend fun flipCard() {
        baseGameViewModel.actualCard.collect { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.cardProgressBar.visibility = View.VISIBLE
                }

                is UiState.Error -> {
                    binding.cardProgressBar.visibility = View.GONE
                    binding.onCardText.visibility = View.GONE
                    binding.languageHint.visibility = View.GONE
                    binding.onCardTextDefinition.text = state.errorMessage
                }

                is UiState.Success -> {
                    binding.cardProgressBar.visibility = View.GONE
                    binding.onCardText.visibility = View.VISIBLE
                    binding.languageHint.visibility = View.VISIBLE
                    val card = state.data
                    if (cardFliped) {
                        binding.onCardText.text = card.cardContent
                        binding.onCardTextDefinition.text = card.contentDescription
                        binding.languageHint.text = deck?.deckFirstLanguage
                        cardFliped = false
                    } else {
                        binding.onCardText.text = card.cardDefinition
                        binding.onCardTextDefinition.text = card.valueDefinition
                        binding.languageHint.text = deck?.deckSecondLanguage
                        cardFliped = true
                    }
                }
            }
        }

    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
        const val DECK_ERROR_KEY = "Deck_error_key"
    }
}