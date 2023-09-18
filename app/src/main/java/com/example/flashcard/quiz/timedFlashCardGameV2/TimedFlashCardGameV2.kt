package com.example.flashcard.quiz.timedFlashCardGameV2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityTimedFlashCardGameV2Binding
import com.example.flashcard.util.CardBackgroundSelector
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import kotlinx.coroutines.launch

class TimedFlashCardGameV2 : AppCompatActivity() {

    private lateinit var binding: ActivityTimedFlashCardGameV2Binding
    private val viewModel: TimedFlashCardGameV2ViewModel by viewModels()

    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
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
        binding = ActivityTimedFlashCardGameV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards.toExternal()
            val deck = it.deck.toExternal()
            viewModel.initCardList(cardList)
            viewModel.initDeck(deck)
            viewModel.updateCards()
        }

        lifecycleScope.launch {
            viewModel
                .actualCards
                .collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }

                        is UiState.Error -> {
                            //onQuizComplete(timedFlashCardViewModel.getKnownCardSum(cardList), timedFlashCardViewModel.getUnknownCards(), deck!!, cardList)
                        }

                        is UiState.Success -> {
                            val cards = state.data
                            bindCard(cards)
                        }
                    }
                }
        }

        binding.flipButton.setOnClickListener {
            val scale: Float = applicationContext.resources.displayMetrics.density

        }

        binding
            .motionLayout
            .setTransitionListener(object : TransitionAdapter() {
                override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                    when (currentId) {
                        R.id.flipCard -> {
                        }

                        R.id.offScreenPass -> {
                            viewModel.swipe()
                            motionLayout.progress = 0f
                            motionLayout.setTransition(R.id.rest, R.id.pass)
                        }

                        R.id.offScreenLike -> {
                            viewModel.swipe()
                            motionLayout.progress = 0f
                            motionLayout.setTransition(R.id.rest, R.id.like)
                        }

                        R.id.cardBackOffScreenPass -> {
                            motionLayout.progress = 0f
                            viewModel.swipe()
                            motionLayout.setTransition(R.id.rest, R.id.backPass)
                        }

                        R.id.cardBackoffScreenLike -> {
                            motionLayout.progress = 0f
                            viewModel.swipe()
                            motionLayout.setTransition(R.id.rest, R.id.backLike)
                        }
                    }
                }

                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {
                    when (startId) {
                        R.id.flip -> {
                            val scale: Float = applicationContext.resources.displayMetrics.density
                            binding.topCard.cameraDistance = 8000 * scale
                            binding.backCard.cameraDistance = 8000 * scale
                        }
                        R.id.rest -> {
                            val scale: Float = applicationContext.resources.displayMetrics.density
                            binding.backCard.cameraDistance = 8000 * scale
                            binding.topCard.cameraDistance = 8000 * scale
                        }
                    }
                }
            })

    }

    private fun bindCard(model: TimedFlashCardGameModel) {
        val deckColorCode = viewModel.deck?.deckColorCode?.let {
            DeckColorCategorySelector().selectColor(it)
        } ?: R.color.black

        binding.topCard.setCardBackgroundColor(getColor(deckColorCode))
        binding.topCardText.text = model.top.cardContent
        binding.topCardTextDescription.text = model.top.contentDescription
        binding.cardLanguageHint.text = viewModel.deck?.deckFirstLanguage

        binding.backCardText.text = model.top.cardDefinition
        binding.backCard.setCardBackgroundColor(getColor(deckColorCode))
        binding.cardBackLanguageHint.text = viewModel.deck?.deckSecondLanguage
        binding.backCardTextDescription.text = model.top.valueDefinition

        binding.bottomCard.setCardBackgroundColor(getColor(deckColorCode))
        binding.bottomCardText.text = model.bottom.cardContent
        binding.bottomCardTextDescription.text = model.bottom.contentDescription
        binding.cardBottomLanguageHint.text = viewModel.deck?.deckFirstLanguage
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }
}