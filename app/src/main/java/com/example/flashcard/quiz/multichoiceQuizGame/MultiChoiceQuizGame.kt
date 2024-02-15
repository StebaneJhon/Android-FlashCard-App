package com.example.flashcard.quiz.multichoiceQuizGame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityMultichoiceQuizGameBinding
import com.example.flashcard.deck.MainActivity
import com.example.flashcard.quiz.timedFlashCardGame.TimedFlashCardGame
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import kotlinx.coroutines.launch

class MultiChoiceQuizGame : AppCompatActivity() {

    private lateinit var binding: ActivityMultichoiceQuizGameBinding
    private val viewModel: MultiChoiceQuizGameViewModel by viewModels()

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var deckWithCards: DeckWithCards? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }
        binding = ActivityMultichoiceQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(TimedFlashCardGame.DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards.toExternal()
            val deck = it.deck.toExternal()
            viewModel.initOriginalCardList(cardList)
            startTimedFlashCard(cardList, deck)
        }

        lifecycleScope.launch {
            viewModel
                .actualCard
                .collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }
                        is UiState.Error -> {
                            onQuizComplete()
                        }
                        is UiState.Success -> {
                            bindCard( state.data)
                        }
                    }
                }
        }

        binding
            .multiChoiceQuizGameMotionLY
            .setTransitionListener(object: TransitionAdapter() {
                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    when (currentId) {
                        R.id.endOffScreen -> {
                            motionLayout?.progress = 0f
                            if (viewModel.swipe()) {
                                motionLayout?.setTransition(R.id.start, R.id.end)
                            } else {
                                motionLayout?.setTransition(R.id.displayGameReviewLayoutMQ, R.id.end)
                            }
                            binding.quizProgress.progress = viewModel.progress
                        }
                    }
                }
            })

        binding.backToDeckBT.setOnClickListener {
            finish()
        }
        binding.exitBT.setOnClickListener {
            finish()
        }
    }

    private fun onAlternativeClicked(word: String, card: MultiChoiceGameCardModel) {
        if (word == card.answer) {
            binding.multiChoiceQuizGameMotionLY.transitionToState(R.id.end)
        } else {
            viewModel.onCardMissed()
            Toast.makeText(this, "XXX", Toast.LENGTH_LONG).show()
        }
    }

    private fun bindCard(card: MultiChoiceGameCardModel) {
        binding.onCardWord.text = card.onCardWord
        binding.alternative1.text = card.alternative1
        binding.alternative2.text = card.alternative2
        binding.alternative3.text = card.alternative3

        binding.alternative1.setOnClickListener {
            onAlternativeClicked(binding.alternative1.text.toString(), card)
        }
        binding.alternative2.setOnClickListener {
            onAlternativeClicked(binding.alternative2.text.toString(), card)
        }
        binding.alternative3.setOnClickListener {
            onAlternativeClicked(binding.alternative3.text.toString(), card)
        }
    }

    private fun onQuizComplete() {
        binding.gameReviewLayoutMQ.apply {
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "Multi Choice Quiz")
            tvTotalCardsSumScoreLayout.text = viewModel.cardSum().toString()
            tvMissedCardSumScoreLayout.text = viewModel.getMissedCardSum().toString()
            tvKnownCardsSumScoreLayout.text = viewModel.getKnownCardSum().toString()

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@MultiChoiceQuizGame, MainActivity::class.java))
            }
            btRestartQuizScoreLayout.setOnClickListener {
                viewModel.initTimedFlashCard()
                binding.quizProgress.progress = viewModel.progress
                viewModel.updateCard()
                binding.multiChoiceQuizGameMotionLY.setTransition(R.id.start, R.id.displayGameReviewLayoutMQ)
            }
            btReviseMissedCardScoreLayout.setOnClickListener {
                val newCards = viewModel.getMissedCard()
                viewModel.initTimedFlashCard()
                startTimedFlashCard(newCards, viewModel.deck)

            }
        }
    }


    private fun startTimedFlashCard(cardList: List<ImmutableCard>, deck: ImmutableDeck) {
        binding.multiChoiceQuizGameMotionLY.setTransition(R.id.start, R.id.displayGameReviewLayoutMQ)
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateCard()
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }
}