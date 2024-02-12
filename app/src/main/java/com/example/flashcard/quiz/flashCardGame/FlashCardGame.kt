package com.example.flashcard.quiz.flashCardGame

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityFlashCardGameBinding
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FlashCardGame : AppCompatActivity() {

    private lateinit var binding: ActivityFlashCardGameBinding
    private val viewModel: FlashCardGameViewModel by viewModels()

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var deckWithCards: DeckWithCards? = null
    lateinit var front_anim: AnimatorSet
    lateinit var back_anim: AnimatorSet
    var isFront = true
    private var dx: Float = 0.0f
    private var dy: Float = 0.0f
    private val MIN_SWIPE_DISTANCE = -75

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }

        binding = ActivityFlashCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards.toExternal()
            val deck = it.deck.toExternal()
            initFlashCard(cardList, deck)
        }

        lifecycleScope.launch {
            viewModel
                .actualCards
                .collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }

                        is UiState.Error -> {
                            //onQuizComplete()
                            Toast.makeText(
                                this@FlashCardGame,
                                "FlashCard Completed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is UiState.Success -> {
                            bindCard(state.data)
                        }
                    }
                }
        }

        front_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.front_animator
        ) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.back_animator
        ) as AnimatorSet
        val scale: Float = applicationContext.resources.displayMetrics.density
        binding.cvCardFront.cameraDistance = 8000 * scale
        binding.cvCardBack.cameraDistance = 8000 * scale


        gameOn(binding.clOnScreenCardRoot)

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun gameOn(card: ConstraintLayout) {
        card.setOnTouchListener { view, motionEvent ->
            val displayMetrics = resources.displayMetrics
            val cardWidth = view.width
            val cardHeight = binding.cvCardFront.height
            val cardStartX = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)
            val cardStartY = (displayMetrics.heightPixels.toFloat() / 2) - (cardHeight / 2) - 40

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = view.x - motionEvent.rawX
                    dy = view.y - motionEvent.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    view.animate()
                        .x(motionEvent.rawX + dx)
                        .y(motionEvent.rawY + dy)
                        .setDuration(0)
                        .start()

                    when {
                        (view.x > (((MIN_SWIPE_DISTANCE * -1) / 2) + view.width / 2)) -> {
                            isCardKnown(true)
                        }

                        (view.x < MIN_SWIPE_DISTANCE) -> {
                            isCardKnown(false)
                        }

                        else -> {
                            isCardKnown(null)
                        }
                    }


                }

                MotionEvent.ACTION_UP -> {
                    var currentX = view.x
                    var currentY = view.y
                    if (
                        currentX > MIN_SWIPE_DISTANCE && currentX <= 0 ||
                        currentX < (((MIN_SWIPE_DISTANCE * -1) / 2) + view.width / 2) && currentX >= 0
                    ) {
                        view.animate()
                            .x(cardStartX)
                            .y(cardStartY)
                            .setDuration(150)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    if (currentX == view.x || currentY == view.y) {
                                        flipCard()
                                    } else {
                                        val deckColorCode = viewModel.deck?.deckColorCode?.let {
                                            DeckColorCategorySelector().selectColor(it)
                                        } ?: R.color.black
                                        binding.cvCardFront.backgroundTintList =
                                            ContextCompat.getColorStateList(
                                                this@FlashCardGame,
                                                deckColorCode
                                            )
                                    }
                                    currentX = 0f
                                    currentY = 0f
                                }
                            })
                            .start()
                    } else if (currentX < MIN_SWIPE_DISTANCE) {
                        view.animate()
                            .x(currentX - 1000)
                            .setDuration(250)
                            .start()

                        lifecycleScope.launch {
                            delay(500)
                            view.animate()
                                .x(cardStartX)
                                .y(cardStartY)
                                .setDuration(0)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        if (currentX < MIN_SWIPE_DISTANCE) {
                                            if (viewModel.swipe(true)) {
                                                Toast.makeText(
                                                    this@FlashCardGame,
                                                    "Not Known",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                currentY = 0f
                                                currentX = 0f
                                            } else {
                                                Toast.makeText(
                                                    this@FlashCardGame,
                                                    "FlashCard Complete",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                })
                                .start()
                        }
                    } else if (currentX > (MIN_SWIPE_DISTANCE * (-1))) {
                        view.animate()
                            .x(currentX + 10000)
                            .setDuration(1000)
                            .start()
                        currentY = 0f
                        currentX = 0f
                    }
                }
            }

            return@setOnTouchListener true
        }
    }

    private fun isCardKnown(isKnown: Boolean?) {
        when (isKnown) {
            true -> {
                binding.cvCardFront.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.green200)
                binding.cvCardBack.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.green200)
            }

            false -> {
                binding.cvCardFront.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.red200)
                binding.cvCardBack.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.red200)
            }

            else -> {
                val deckColorCode = viewModel.deck?.deckColorCode?.let {
                    DeckColorCategorySelector().selectColor(it)
                } ?: R.color.black
                binding.cvCardFront.backgroundTintList =
                    ContextCompat.getColorStateList(this@FlashCardGame, deckColorCode)
                binding.cvCardBack.backgroundTintList =
                    ContextCompat.getColorStateList(this@FlashCardGame, deckColorCode)
            }
        }

    }

    private fun flipCard() {
        val scale: Float = applicationContext.resources.displayMetrics.density
        binding.cvCardFront.cameraDistance = 8000 * scale
        binding.cvCardBack.cameraDistance = 8000 * scale
        front_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.front_animator
        ) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.back_animator
        ) as AnimatorSet
        isFront = if (isFront) {
            front_anim.setTarget(binding.cvCardFront)
            back_anim.setTarget(binding.cvCardBack)
            front_anim.start()
            back_anim.start()
            false
        } else {
            front_anim.setTarget(binding.cvCardBack)
            back_anim.setTarget(binding.cvCardFront)
            back_anim.start()
            front_anim.start()
            true
        }
    }

    private fun bindCard(onScreenCards: FlashCardGameModel) {

        val deckColorCode = viewModel.deck?.deckColorCode?.let {
            DeckColorCategorySelector().selectColor(it)
        } ?: R.color.black

        binding.cvCardFront.backgroundTintList =
            ContextCompat.getColorStateList(this, deckColorCode)
        binding.tvQuizFront.text = onScreenCards.top.cardContent
        val sumCardsInDeck = viewModel.getTotalCards()
        val currentCardNumber = viewModel.getCurrentCardNumber()

        binding.cvCardBack.backgroundTintList = ContextCompat.getColorStateList(this, deckColorCode)
        binding.tvQuizBack.text = onScreenCards.top.cardDefinition
        binding.tvFlashCardFrontProgression.text = getString(
            R.string.tx_flash_card_game_progression,
            "$currentCardNumber",
            "$sumCardsInDeck"
        )
        binding.tvFlashCardBackProgression.text = getString(
            R.string.tx_flash_card_game_progression,
            "$currentCardNumber",
            "$sumCardsInDeck"
        )

        if (onScreenCards.bottom != null) {
            binding.cvCardBottom.backgroundTintList =
                ContextCompat.getColorStateList(this, deckColorCode)
            binding.tvQuizBottom.text = onScreenCards.bottom.cardContent
            binding.tvFlashCardBottomProgression.text = getString(
                R.string.tx_flash_card_game_progression,
                "${currentCardNumber.plus(1)}",
                "$sumCardsInDeck"
            )
        } else {
            binding.cvCardBottom.backgroundTintList =
                ContextCompat.getColorStateList(this, deckColorCode)
            binding.tvQuizBottom.text = "..."
        }

    }

    private fun initFlashCard(
        cardList: List<ImmutableCard>,
        deck: ImmutableDeck
    ) {
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateOnScreenCards()
        binding.topAppBar.title = deck.deckName
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }

}