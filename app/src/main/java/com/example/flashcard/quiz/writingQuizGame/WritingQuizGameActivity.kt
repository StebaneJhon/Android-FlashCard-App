package com.example.flashcard.quiz.writingQuizGame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityWritingQuizGameBinding
import com.example.flashcard.mainActivity.MainActivity
import com.example.flashcard.settings.MiniGameSettingsSheet
import com.example.flashcard.util.FlashCardMiniGameRef
import com.example.flashcard.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WritingQuizGameActivity : AppCompatActivity(), MiniGameSettingsSheet.SettingsApplication {

    private lateinit var binding: ActivityWritingQuizGameBinding

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private val viewModel: WritingQuizGameViewModel by viewModels {
        WritingQuizGameViewModelFactory((application as FlashCardApplication).repository)
    }
    private var deckWithCards: ImmutableDeckWithCards? = null

    private var animFadeIn: Animation? = null
    private var animFadeOut: Animation? = null
    private var modalBottomSheet: MiniGameSettingsSheet? = null
    private var miniGamePref: SharedPreferences? = null
    private var miniGamePrefEditor: SharedPreferences.Editor? = null


    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
        private const val TAG = "WritingQuizGameActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        miniGamePref = getSharedPreferences(FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF, Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        miniGamePrefEditor = miniGamePref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }
        binding = ActivityWritingQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vpCardHolder.isUserInputEnabled = false

        animFadeIn = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        animFadeOut = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards?.toMutableList()
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                viewModel.initOriginalCardList(cardList)
                startWritingQuizGame(cardList, deck)
            } else {
                onNoCardToRevise()
            }
        }

        binding.topAppBar.apply {
            title = getString(R.string.title_flash_card_game, viewModel.deck.deckName)
            setNavigationOnClickListener { finish() }
        }

        applySettings()

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
                        onNoCardToRevise()
                    }
                    is UiState.Success -> {
                        binding.cvCardErrorLy.visibility = View.GONE
                        binding.pbCardLoadingLy.visibility = View.GONE
                        launchWritingQuizGame(state.data)
                    }
                }
            }
        }

        modalBottomSheet = MiniGameSettingsSheet()

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.mn_bt_settings) {
                modalBottomSheet?.show(supportFragmentManager, MiniGameSettingsSheet.TAG)
                true
            } else {
                false
            }
        }

    }

    override fun onSettingsApplied() {
        applySettings()
    }

    private fun applySettings() {

        val filter = miniGamePref?.getString(
            FlashCardMiniGameRef.CHECKED_FILTER,
            FlashCardMiniGameRef.FILTER_RANDOM
        )

        val unKnownCardFirst = miniGamePref?.getBoolean(
            FlashCardMiniGameRef.IS_UNKNOWN_CARD_FIRST,
            true
        )
        val unKnownCardOnly = miniGamePref?.getBoolean(
            FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY,
            false
        )

        if (unKnownCardOnly == true) {
            viewModel.cardToReviseOnly()
        } else {
            viewModel.restoreCardList()
        }

        when (filter) {
            FlashCardMiniGameRef.FILTER_RANDOM -> {
                viewModel.shuffleCards()
            }

            FlashCardMiniGameRef.FILTER_BY_LEVEL -> {
                viewModel.sortCardsByLevel()
            }

            FlashCardMiniGameRef.FILTER_CREATION_DATE -> {
                viewModel.sortByCreationDate()
            }
        }

        if (unKnownCardFirst == true) {
            viewModel.sortCardsByLevel()
        }

        restartWritingQuiz()
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
                unableShowUnKnownCardOnly()
                applySettings()
            }
        }
        //isFlashCardGameScreenHidden(true)
    }

    private fun unableShowUnKnownCardOnly() {
        miniGamePrefEditor?.apply {
            putBoolean(FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY, false)
            apply()
        }
    }

    private fun getCardOrientation() = miniGamePref?.getString(
        FlashCardMiniGameRef.CHECKED_CARD_ORIENTATION,
        FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
    ) ?: CARD_ORIENTATION_FRONT_AND_BACK

    private fun launchWritingQuizGame(cards: List<WritingQuizGameModel>) {
        val writingQuizGameAdapter = WritingQuizGameAdapter(this, cards, viewModel.deck.deckColorCode!!) {
            if (viewModel.isUserAnswerCorrect(it.userAnswer, it.correctAnswer)) {
                if (viewModel.swipe()) {
                    binding.vpCardHolder.setCurrentItem(viewModel.getCurrentCardPosition(), true)
                } else {
                    onQuizComplete()
                }
            } else {
                onWrongAnswer(it.cvCardFront, it.cvCardOnWrongAnswer, animFadeIn!!, animFadeOut!!)
            }
        }
        binding.vpCardHolder.adapter = writingQuizGameAdapter
    }

    private fun onWrongAnswer(
        card: MaterialCardView,
        onWrongCard: MaterialCardView,
        animFadeIn: Animation,
        animFadeOut: Animation
    ) {
        card.startAnimation(animFadeOut)
        card.visibility = View.GONE
        onWrongCard.visibility = View.VISIBLE
        onWrongCard.startAnimation(animFadeIn)
        lifecycleScope.launch {
            delay(500)
            onWrongCard.startAnimation(animFadeOut)
            onWrongCard.visibility = View.GONE
            card.visibility = View.VISIBLE
            card.startAnimation(animFadeIn)
        }
    }

    private fun onQuizComplete() {
        binding.gameReviewContainerMQ.visibility = View.VISIBLE
        binding.vpCardHolder.visibility = View.GONE
        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
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
                restartWritingQuiz()
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

    private fun restartWritingQuiz() {
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.initWritingQuizGame()
        viewModel.updateCard(getCardOrientation())
    }

    private fun startWritingQuizGame(cardList: MutableList<ImmutableCard>, deck: ImmutableDeck) {
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        binding.vpCardHolder.setCurrentItem(0, true)
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateCard(getCardOrientation())
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

}