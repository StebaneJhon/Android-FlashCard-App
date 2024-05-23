package com.example.flashcard.quiz.testQuizGame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.databinding.ActivityTestQuizGameBinding
import com.example.flashcard.util.CardType.FLASHCARD
import com.example.flashcard.util.CardType.ONE_OR_MULTI_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class TestQuizGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestQuizGameBinding
    private val viewModel: TestQuizGameViewModel by viewModels {
        TestQuizGameViewModelFactory((application as FlashCardApplication).repository)
    }
    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private lateinit var oneOrMultipleAnswerCardModel: OneOrMultipleAnswerCardModel
    private lateinit var trueOrFalseCardModel: TrueOrFalseCardModel
    private lateinit var flashCardModel: TrueOrFalseCardModel

    private var deckWithCards: ImmutableDeckWithCards? = null
    lateinit var testQuizGameAdapter: TestQuizGameAdapter

    private lateinit var frontAnim: AnimatorSet
    private lateinit var backAnim: AnimatorSet

    companion object {
        private const val TAG = "TestQuizGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }
        binding = ActivityTestQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards?.toMutableList()
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                viewModel.initOriginalCardList(cardList)
                viewModel.initCardList(cardList)
                viewModel.initDeck(deck)
                viewModel.initModelCardList(cardList)
                viewModel.getCards()
                //startWritingQuizGame(cardList, deck)
            } else {
                //onNoCardToRevise()
            }
        }

        binding.topAppBar.apply {
            title = getString(R.string.title_flash_card_game, viewModel.deck?.deckName)
            setNavigationOnClickListener { finish() }
        }

        lifecycleScope.launch {
            viewModel
                .modelCards
                .collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }
                        is UiState.Error -> {
                            onNoCardToRevise()
                        }
                        is UiState.Success -> {
                            launchMultiChoiceQuizGame(state.data)
                        }
                    }
                }
        }

    }

    private fun launchMultiChoiceQuizGame(
        data: List<ModelCard?>
    ) {
        testQuizGameAdapter = TestQuizGameAdapter(this, data, viewModel.getDeckColorCode()) { userResponseModel ->
            when (userResponseModel.modelCard.cardDetails?.cardType) {
                FLASHCARD -> {
                    onOneAndOneCardClicked(userResponseModel)
                }
                TRUE_OR_FALSE_CARD -> {
                    // TODO: Implement onCard clicked true or false card
                }
                ONE_OR_MULTI_ANSWER_CARD -> {
                    // TODO: Implement onCard clicked one or multiple answer card
                }
                else -> {
                    // TODO: Implement onCard clicked
                }
            }
        }
        binding.vpCardHolder.adapter = testQuizGameAdapter
    }

    private fun onOneAndOneCardClicked(userResponseModel: UserResponseModel) {
        viewModel.onFlipCard(userResponseModel.modelCardPosition)
    }

    private fun onNoCardToRevise() {
        binding.lyOnNoMoreCardsErrorContainer.isVisible = true
        binding.vpCardHolder.visibility = View.GONE
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.lyNoCardError.apply {
            btBackToDeck.setOnClickListener {
                finish()
            }

            btUnableUnknownCardOnly.setOnClickListener {
                //unableShowUnKnownCardOnly()
                //applySettings()
            }
        }
        //isFlashCardGameScreenHidden(true)
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

}