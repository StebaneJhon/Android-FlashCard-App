package com.example.flashcard.quiz.writingQuizGame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import com.example.flashcard.databinding.ActivityWritingQuizGameBinding
import com.example.flashcard.deck.MainActivity
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WritingQuizGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWritingQuizGameBinding

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var imm: InputMethodManager? = null

    private val viewModel: WritingQuizGameViewModel by viewModels()
    private var deckWithCards: DeckWithCards? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }
        binding = ActivityWritingQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards.toExternal()
            val deck = it.deck.toExternal()
            viewModel.initOriginalCardList(cardList)
            startWritingQuizGame(cardList, deck)
        }

        lifecycleScope.launch {
            viewModel
                .actualCard
                .collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.pbCardLoadingLy.visibility = View.VISIBLE
                        binding.cvCardErrorLy.visibility = View.GONE
                    }
                    is UiState.Error -> {
                        /*
                        binding.pbCardLoadingLy.visibility = View.GONE
                        binding.tvErrorMessage.text = state.errorMessage.toString()
                        binding.cvCardErrorLy.visibility = View.VISIBLE

                         */
                        omQuizComplete()
                    }
                    is UiState.Success -> {
                        binding.cvCardErrorLy.visibility = View.GONE
                        binding.pbCardLoadingLy.visibility = View.GONE
                        bindCard(state.data)
                    }
                }
            }
        }

        binding
            .lyWritingQuizGameRoot
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
                            binding.pbQuiz.progress = viewModel.progress
                        }
                    }
                }
            })

        binding.btExit.setOnClickListener {
            finish()
        }
        binding.btBackToDeck.setOnClickListener {
            finish()
        }

    }

    private fun bindCard(card: WritingQuizGameModel) {

        binding.tvTopOnCardWord.text = card.onCardWord
        binding.tiTopCardContent.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val userInput = binding.tiTopCardContent.text?.trim().toString().lowercase()
                val correctAnswer = card.answer.lowercase()

                if (userInput == correctAnswer) {
                    binding.lyWritingQuizGameRoot.transitionToState(R.id.end)
                    binding.tiTopCardContent.text?.clear()
                } else {
                    viewModel.onCardMissed()
                    Toast.makeText(this, "XXX", Toast.LENGTH_LONG).show()
                }
                true
            } else {
                false
            }
        }

    }

    private fun omQuizComplete() {
        binding.gameReviewLayoutMQ.apply {
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "Writing Quiz")
            tvTotalCardsSumScoreLayout.text = viewModel.cardSum().toString()
            tvMissedCardSumScoreLayout.text = viewModel.getMissedCardSum().toString()
            tvKnownCardsSumScoreLayout.text = viewModel.getKnownCardSum().toString()

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@WritingQuizGameActivity, MainActivity::class.java))
                finish()
            }
            btRestartQuizScoreLayout.setOnClickListener {
                viewModel.initWritingQuizGame()
                binding.pbQuiz.progress = viewModel.progress
                viewModel.updateCard()
                binding.lyWritingQuizGameRoot.setTransition(R.id.start, R.id.displayGameReviewLayoutMQ)
            }
            btReviseMissedCardScoreLayout.setOnClickListener {
                val newCards = viewModel.getMissedCard()
                viewModel.initWritingQuizGame()
                startWritingQuizGame(newCards, viewModel.deck)
            }
        }
    }

    private fun startWritingQuizGame(cardList: List<ImmutableCard>, deck: ImmutableDeck) {
        binding.lyWritingQuizGameRoot.setTransition(R.id.start, R.id.displayGameReviewLayoutMQ)
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