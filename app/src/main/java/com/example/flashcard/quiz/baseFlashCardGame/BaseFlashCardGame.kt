package com.example.flashcard.quiz.baseFlashCardGame


import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityBaseFlashCardGameBinding
import com.example.flashcard.deck.MainActivity
import com.example.flashcard.util.CardBackgroundSelector
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.UiState
import kotlinx.coroutines.launch

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
            startFlashCard(cardList!!)
        }

    }

    private fun startFlashCard(cardList: List<ImmutableCard>) {
        lifecycleScope.launch {
            displayNextCard(cardList)
        }

        binding.yesButton.setOnClickListener {
            baseGameViewModel.onCardKnown()
            lifecycleScope.launch {
                displayNextCard(cardList)
            }
        }
        binding.noButton.setOnClickListener {
            baseGameViewModel.onCardUnknown(cardList)
            lifecycleScope.launch {
                displayNextCard(cardList)
            }
        }
        binding.cardRoot.setOnClickListener {
            lifecycleScope.launch {
                flipCard()
            }
        }
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    private suspend fun displayNextCard(
        cardList: List<ImmutableCard>
    ) {
        binding.feedbackCard.visibility = View.GONE
        binding.wordCard.visibility = View.VISIBLE

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
                    onQuizComplete(baseGameViewModel.getKnownCardSum(cardList), baseGameViewModel.getUnknownCards(), deck!!)
                }

                is UiState.Success -> {
                    val card = state.data
                    initCard(card)
                }
            }
        }
    }

    private fun initCard(card: ImmutableCard) {
        binding.cardProgressBar.visibility = View.GONE
        binding.onCardText.visibility = View.VISIBLE
        binding.onCardText.text = card.cardContent
        binding.onCardTextDefinition.text = card.contentDescription
        binding.languageHint.text = deck?.deckFirstLanguage

        val background = card.backgroundImg?.let {
            CardBackgroundSelector().selectPattern(it)
        } ?: R.drawable.abstract_surface_textures
        binding.cardBackgroundImg.setImageResource(background)

        val deckColorCode = deck?.deckColorCode?.let {
            DeckColorCategorySelector().selectColor(it)
        } ?: R.color.red700
        binding.cardRoot.setCardBackgroundColor(ContextCompat.getColor(this@BaseFlashCardGame, deckColorCode))
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

    private fun onQuizComplete(
        knownCardSum: Int,
        missedCard: List<ImmutableCard>,
        deck: ImmutableDeck
    ) {
        binding.feedbackCard.visibility = View.VISIBLE
        binding.wordCard.visibility = View.GONE

        binding.knownCardsTV.text = getString(R.string.known_cards_text, knownCardSum.toString())
        binding.missedCardTV.text = getString(R.string.missed_cards_text, missedCard.size.toString())

        if (missedCard.isEmpty()) {
            binding.reviseMissedCardBT.visibility = View.GONE
        } else {
            binding.reviseMissedCardBT.visibility = View.VISIBLE
        }

        binding.reviseMissedCardBT.setOnClickListener {
            val newCards = baseGameViewModel.getUnknownCards()
            baseGameViewModel.initFlashCard()
            startFlashCard(newCards)
        }

        binding.restartFlashCardBT.setOnClickListener {
            baseGameViewModel.initFlashCard()
            deckWithCards = intent?.parcelable(DECK_ID_KEY)
            deckWithCards?.let {
                cardList = it.cards.toExternal()
                startFlashCard(cardList!!)
            }
        }

        binding.backToDeckBT.setOnClickListener {
            startActivity(Intent(this@BaseFlashCardGame, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
        const val DECK_ERROR_KEY = "Deck_error_key"
    }
}