package com.example.flashcard.quiz.baseFlashCardGame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.databinding.ActivityBaseFlashCardGameBinding
import com.example.flashcard.util.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BaseFlashCardGame : AppCompatActivity() {

    private lateinit var binding: ActivityBaseFlashCardGameBinding
    private val baseGameViewModel: BaseFlashCardGameViewModel by viewModels {
        BaseFlashCardGameViewModelFactory((application as FlashCardApplication).repository)
    }

    private var deckId: Int? = null
    private var cardList: List<ImmutableCard>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseFlashCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckId = intent?.getIntExtra(DECK_ID_KEY, -1)
        populateCard()

        binding.yesButton.setOnClickListener { populateCard() }
        binding.noButton.setOnClickListener { populateCard() }

    }

    private fun populateCard() {
        deckId?.let { deckId ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    baseGameViewModel.getDeckWithCards(deckId)
                    baseGameViewModel.deckWithAllCards.collect { deckWithCardState ->
                        when (deckWithCardState) {
                            is UiState.Loading -> {
                                binding.cardProgressBar.visibility = View.VISIBLE
                            }
                            is UiState.Error -> {
                                binding.cardProgressBar.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.cardProgressBar.visibility = View.GONE
                                cardList = deckWithCardState.data[0].cards.toExternal()
                                baseGameViewModel.getActualCard(cardList!!)
                                baseGameViewModel.actualCard.collect { state ->
                                    when (state) {
                                        is UiState.Loading -> {
                                            binding.cardProgressBar.visibility = View.VISIBLE
                                        }

                                        is UiState.Error -> {
                                            binding.cardProgressBar.visibility = View.GONE

                                        }

                                        is UiState.Success -> {
                                            binding.cardProgressBar.visibility = View.GONE
                                            showCardContent(state.data)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showCardContent(card: ImmutableCard) {
        binding.onCardText.text = card.cardContent
        binding.onCardTextDefinition.text = card.contentDescription
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }
}