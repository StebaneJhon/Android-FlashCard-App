package com.example.flashcard.quiz.writingQuizGame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityWritingQuizGameBinding
import com.example.flashcard.deck.MainActivity
import com.example.flashcard.util.DeckColorCategorySelector
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

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
        private const val TAG = "WritingQuizGameActivity"
    }

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

        binding.topAppBar.apply {
            title = getString(R.string.title_flash_card_game, viewModel.deck.deckName)
            setNavigationOnClickListener { finish() }
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
                        }
                    }
                }
            })

    }

    private fun bindCard(card: WritingQuizGameModel) {

        binding.tvTopOnCardWord.text = card.onCardWord
        Log.e(TAG, card.answer)
        val deckColorCode = viewModel.deck.deckColorCode?.let {
            DeckColorCategorySelector().selectColor(it)
        } ?: R.color.black
        binding.cardTopLY.backgroundTintList = ContextCompat.getColorStateList(this, deckColorCode)
        binding.cardBottomLY.backgroundTintList = ContextCompat.getColorStateList(this, deckColorCode)
        binding.tvWritingQuizFrontProgression.text = getString(R.string.tx_flash_card_game_progression, viewModel.getCurrentCardNumber(), viewModel.cardSum().toString())
        binding.tiTopCardContent.requestFocus()
        binding.tiCardContent.requestFocus()
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
            lpiQuizResultDiagramScoreLayout.progress = viewModel.progress
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
                viewModel.updateCard()
                binding.lyWritingQuizGameRoot.setTransition(R.id.start, R.id.displayGameReviewLayoutMQ)
            }
            if (viewModel.getMissedCardSum() == 0) {
                btReviseMissedCardScoreLayout.isActivated = false
                btReviseMissedCardScoreLayout.isVisible = false
            } else {
                btReviseMissedCardScoreLayout.isActivated = true
                btReviseMissedCardScoreLayout.isVisible = true
                btReviseMissedCardScoreLayout.setOnClickListener {
                    val newCards = viewModel.getMissedCard()
                    viewModel.initWritingQuizGame()
                    startWritingQuizGame(newCards, viewModel.deck)
                }
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

}