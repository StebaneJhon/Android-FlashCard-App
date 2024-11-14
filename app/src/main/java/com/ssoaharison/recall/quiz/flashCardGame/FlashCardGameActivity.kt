package com.ssoaharison.recall.quiz.flashCardGame

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.Model.ImmutableCard
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.backend.Model.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.entities.CardDefinition
import com.ssoaharison.recall.databinding.ActivityFlashCardGameBinding
import com.ssoaharison.recall.mainActivity.MainActivity
import com.ssoaharison.recall.settings.MiniGameSettingsSheet
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.ssoaharison.recall.util.FlashCardMiniGameRef
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_ORIENTATION_BACK_AND_FRONT
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CHECKED_CARD_ORIENTATION
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CHECKED_FILTER
import com.ssoaharison.recall.util.FlashCardMiniGameRef.FILTER_BY_LEVEL
import com.ssoaharison.recall.util.FlashCardMiniGameRef.FILTER_CREATION_DATE
import com.ssoaharison.recall.util.FlashCardMiniGameRef.FILTER_RANDOM
import com.ssoaharison.recall.util.FlashCardMiniGameRef.IS_UNKNOWN_CARD_FIRST
import com.ssoaharison.recall.util.FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY
import com.ssoaharison.recall.util.LanguageUtil
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.UiState
import com.google.android.material.color.MaterialColors
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_COUNT
import com.ssoaharison.recall.util.TextWithLanguageModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


class FlashCardGameActivity :
    AppCompatActivity(),
    MiniGameSettingsSheet.SettingsApplication,
    TextToSpeech.OnInitListener
{

    private lateinit var binding: ActivityFlashCardGameBinding
    private val viewModel: FlashCardGameViewModel by viewModels {
        FlashCardGameViewModelFactory((application as FlashCardApplication).repository)
    }

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var miniGamePref: SharedPreferences? = null
    private var miniGamePrefEditor: SharedPreferences.Editor? = null
    private var deckWithCards: ImmutableDeckWithCards? = null
    private lateinit var frontAnim: AnimatorSet
    private lateinit var backAnim: AnimatorSet
    var isFront = true
    private var dx: Float = 0.0f
    private var dy: Float = 0.0f

    private var modalBottomSheet: MiniGameSettingsSheet? = null
    private var tts: TextToSpeech? = null

    private lateinit var tvDefinitions: List<TextView>
    private val EXTRA_MARGIN = -2

    companion object {
        private val MIN_SWIPE_DISTANCE = -275
        private const val TAG = "FlashCardGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        miniGamePref = getSharedPreferences(
            FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF,
            Context.MODE_PRIVATE
        )
        editor = sharedPref?.edit()
        miniGamePrefEditor = miniGamePref?.edit()
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }

        tts = TextToSpeech(this, this)

        binding = ActivityFlashCardGameBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards?.toMutableList()
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                initFlashCard(cardList, deck, true)
            } else {
                onNoCardToRevise()
            }
        }

        tvDefinitions = listOf(
            binding.tvQuizBack1, binding.tvQuizBack2, binding.tvQuizBack3, binding.tvQuizBack4, binding.tvQuizBack5,
            binding.tvQuizBack6, binding.tvQuizBack7, binding.tvQuizBack8, binding.tvQuizBack9, binding.tvQuizBack10,
        )

        applySettings()
        lifecycleScope.launch {
            viewModel
                .actualCards
                .collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }

                        is UiState.Error -> {
                            onNoCardToRevise()
                        }

                        is UiState.Success -> {
                            bindCard(state.data, getCardOrientation())
                        }
                    }
                }
        }

        gameOn(binding.clOnScreenCardRoot)

        binding.btKnow.setOnClickListener {
            onKnownButtonClicked()
        }

        binding.btNotKnow.setOnClickListener {
            onKnownNotButtonClicked()
        }

        binding.btRewind.setOnClickListener {
            if (viewModel.getCurrentCardNumber() > 1) {
                viewModel.rewind()
                if (!isFront) {
                    initCardLayout()
                }
            } else {
                Toast.makeText(this, getString(R.string.error_message_previous_card), Toast.LENGTH_SHORT).show()
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
        completelyRestartFlashCard(getCardOrientation())
    }

    private fun applySettings() {

        val filter = miniGamePref?.getString(
            CHECKED_FILTER,
            FILTER_RANDOM
        )

        val unKnownCardFirst = miniGamePref?.getBoolean(
            IS_UNKNOWN_CARD_FIRST,
            true
        )
        val unKnownCardOnly = miniGamePref?.getBoolean(
            IS_UNKNOWN_CARD_ONLY,
            false
        )

        if (unKnownCardOnly == true) {
            viewModel.cardToReviseOnly()
        } else {
            viewModel.restoreCardList()
        }

        when (filter) {
            FILTER_RANDOM -> {
                viewModel.shuffleCards()
            }

            FILTER_BY_LEVEL -> {
                viewModel.sortCardsByLevel()
            }

            FILTER_CREATION_DATE -> {
                viewModel.sortByCreationDate()
            }
        }

        if (unKnownCardFirst == true) {
            viewModel.sortCardsByLevel()
        }
    }

    private fun getCardOrientation() = miniGamePref?.getString(
        CHECKED_CARD_ORIENTATION,
        CARD_ORIENTATION_FRONT_AND_BACK
    ) ?: CARD_ORIENTATION_FRONT_AND_BACK

    private fun getCardCount() = miniGamePref?.getString(CARD_COUNT, "10")?.toInt() ?: 10

    private fun onKnownButtonClicked() {
        val card = binding.clOnScreenCardRoot
        val displayMetrics = resources.displayMetrics
        val cardWidth = card.width
        val cardHeight = binding.cvCardFront.height
        val cardStartX = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)
        val cardStartY = (displayMetrics.heightPixels.toFloat() / 2) - (cardHeight / 2) - EXTRA_MARGIN
        val currentX = card.x
        val currentY = card.y
        completeSwipeToRight(card, currentX)
        onSwipeToRightComplete(
            card,
            cardStartX,
            cardStartY,
            (MIN_SWIPE_DISTANCE * (-10)).toFloat(),
            currentY
        )
    }

    private fun onKnownNotButtonClicked() {
        val card = binding.clOnScreenCardRoot
        val displayMetrics = resources.displayMetrics
        val cardWidth = card.width
        val cardHeight = binding.cvCardFront.height
        val cardStartX = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)
        val cardStartY = (displayMetrics.heightPixels.toFloat() / 2) - (cardHeight / 2) - EXTRA_MARGIN
        val currentX = card.x
        val currentY = card.y
        completeSwipeToLeft(card, currentX)
        onSwipeToLeftCompleted(
            card,
            cardStartX,
            cardStartY,
            (MIN_SWIPE_DISTANCE * 10).toFloat(),
            currentY
        )
    }

    private fun onNoCardToRevise() {
        binding.lyOnNoMoreCardsErrorContainer.isVisible = true
        binding.lyNoCardError.apply {
            btBackToDeck.setOnClickListener {
                finish()
            }
            btUnableUnknownCardOnly.setOnClickListener {
                unableShowUnKnownCardOnly()
                applySettings()
                completelyRestartFlashCard(getCardOrientation())
            }
        }
        isFlashCardGameScreenHidden(true)
    }

    private fun unableShowUnKnownCardOnly() {
        miniGamePrefEditor?.apply {
            putBoolean(IS_UNKNOWN_CARD_ONLY, false)
            apply()
        }
    }

    private fun onQuizComplete() {
        isFlashCardGameScreenHidden(true)
        binding.lyGameReviewContainer.isVisible = true
        binding.lyGameReviewLayout.apply {
            tvScoreTitleScoreLayout.text =
                getString(R.string.flashcard_score_title_text, "Flash Card")
            tvTotalCardsSumScoreLayout.text = viewModel.getTotalCards().toString()
            tvMissedCardSumScoreLayout.text = viewModel.getMissedCardSum().toString()
            tvKnownCardsSumScoreLayout.text = viewModel.getKnownCardSum().toString()

            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getKnownCardSum().toFloat() / viewModel.getTotalCards(),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.green50),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.green400),
            ) as Int

            val mossedCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getMissedCardSum().toFloat() / viewModel.getTotalCards(),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.red50),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.red400),
            ) as Int

            val textColorKnownCards =
                if (viewModel.getTotalCards() / 2 < viewModel.getKnownCardSum())
                    ContextCompat.getColor(this@FlashCardGameActivity, R.color.green50)
                else ContextCompat.getColor(this@FlashCardGameActivity, R.color.green400)

            val textColorMissedCards =
                if (viewModel.getTotalCards() / 2 < viewModel.getMissedCardSum())
                    ContextCompat.getColor(this@FlashCardGameActivity, R.color.red50)
                else ContextCompat.getColor(this@FlashCardGameActivity, R.color.red400)

            tvMissedCardSumScoreLayout.setTextColor(textColorMissedCards)
            tvMissedCardScoreLayout.setTextColor(textColorMissedCards)
            tvKnownCardsSumScoreLayout.setTextColor(textColorKnownCards)
            tvKnownCardsScoreLayout.setTextColor(textColorKnownCards)

            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
            cvContainerMissedCards.background.setTint(mossedCardsBackgroundColor)

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@FlashCardGameActivity, MainActivity::class.java))
                finish()
            }
            btRestartQuizWithPreviousCardsScoreLayout.setOnClickListener {
                isFlashCardGameScreenHidden(false)
                binding.lyOnNoMoreCardsErrorContainer.isVisible = false
                binding.lyGameReviewContainer.isVisible = false
                viewModel.initFlashCard()
                viewModel.updateOnScreenCards()
                if (getCardOrientation() == CARD_ORIENTATION_FRONT_AND_BACK) {
                    initCardLayout()
                } else {
                    onCardOrientationBackFront()
                }
            }
            btRestartQuizWithAllCardsScoreLayout.setOnClickListener {
                completelyRestartFlashCard(getCardOrientation())
            }
            if (viewModel.getMissedCardSum() == 0) {
                btReviseMissedCardScoreLayout.isActivated = false
                btReviseMissedCardScoreLayout.isVisible = false
            } else {
                btReviseMissedCardScoreLayout.isActivated = true
                btReviseMissedCardScoreLayout.isVisible = true
                btReviseMissedCardScoreLayout.setOnClickListener {
                    isFlashCardGameScreenHidden(false)
                    binding.lyOnNoMoreCardsErrorContainer.isVisible = false
                    binding.lyGameReviewContainer.isVisible = false
                    viewModel.initCurrentCardPosition()
                    viewModel.initProgress()
                    viewModel.updateCardOnReviseMissedCards()
                    viewModel.updateOnScreenCards()
                    if (getCardOrientation() == CARD_ORIENTATION_FRONT_AND_BACK) {
                        initCardLayout()
                    } else {
                        onCardOrientationBackFront()
                    }
                }
            }

            if (viewModel.getCardLeft() <= 0) {
                btContinueQuizScoreLayout.visibility = View.GONE
            } else {
                btContinueQuizScoreLayout.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.cards_left_match_quiz_score, "${viewModel.getCardLeft()}")
                    setOnClickListener {
                        isFlashCardGameScreenHidden(false)
                        binding.lyOnNoMoreCardsErrorContainer.isVisible = false
                        binding.lyGameReviewContainer.isVisible = false
                        viewModel.initFlashCard()
                        viewModel.updateCardToRevise(getCardCount())
                        viewModel.updateOnScreenCards()
                        if (getCardOrientation() == CARD_ORIENTATION_FRONT_AND_BACK) {
                            initCardLayout()
                        } else {
                            onCardOrientationBackFront()
                        }
                    }
                }
            }

        }
    }

    private fun completelyRestartFlashCard(orientation: String) {
        isFlashCardGameScreenHidden(false)
        binding.lyOnNoMoreCardsErrorContainer.isVisible = false
        binding.lyGameReviewContainer.isVisible = false
        viewModel.onRestartQuizWithAllCards()
        viewModel.updateCardToRevise(getCardCount())
        viewModel.updateOnScreenCards()
        if (orientation == CARD_ORIENTATION_FRONT_AND_BACK) {
            initCardLayout()
        } else {
            onCardOrientationBackFront()
        }
    }

    private fun isFlashCardGameScreenHidden(isHidden: Boolean) {
        binding.clOnScreenCardRoot.isVisible = !isHidden
        binding.cvCardBottom.isVisible = !isHidden
        binding.btKnow.isVisible = !isHidden
        binding.btRewind.isVisible = !isHidden
        binding.btNotKnow.isVisible = !isHidden
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun gameOn(card: ConstraintLayout) {
        card.setOnTouchListener { view, motionEvent ->
            val displayMetrics = resources.displayMetrics
            val cardWidth = view.width
            val cardHeight = binding.cvCardFront.height
            val cardStartX = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)
            val cardStartY = ((displayMetrics.heightPixels.toFloat() / 2) - (cardHeight / 2)) - EXTRA_MARGIN

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = view.x - motionEvent.rawX
                    dy = view.y - motionEvent.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    onCardMoved(view, motionEvent)
                }

                MotionEvent.ACTION_UP -> {
                    var currentX = view.x
                    var currentY = view.y
                    when {
                        currentX > MIN_SWIPE_DISTANCE && currentX <= 0 || currentX < (MIN_SWIPE_DISTANCE * -1) && currentX >= 0
                        -> {
                            flipCardOnClicked(view, cardStartX, cardStartY, currentX, currentY)
                        }

                        currentX < MIN_SWIPE_DISTANCE
                        -> {
                            onCardKnownNot(view, currentX, cardStartX, cardStartY, currentY)
                        }

                        currentX > (MIN_SWIPE_DISTANCE * (-1))
                        -> {
                            onCardKnown(view, currentX, cardStartX, cardStartY, currentY)
                        }
                    }
                }
            }

            return@setOnTouchListener true
        }
    }

    private fun onCardMoved(view: View, motionEvent: MotionEvent) {
        view.animate()
            .x(motionEvent.rawX + dx)
            .y(motionEvent.rawY + dy)
            .setDuration(0)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    when {
                        (view.x > (MIN_SWIPE_DISTANCE * -1)) -> {
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
            })
            .start()
    }

    private fun flipCardOnClicked(
        view: View,
        cardStartX: Float,
        cardStartY: Float,
        currentX: Float,
        currentY: Float
    ) {
        var currentX1 = currentX
        var currentY1 = currentY
        view.animate()
            .x(cardStartX)
            .y(cardStartY)
            .setDuration(150)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (currentX1 == view.x || currentY1 == view.y) {
                        flipCard()
                    } else {
                        val deckColorCode = viewModel.deck?.deckColorCode?.let {
                            DeckColorCategorySelector().selectColor(it)
                        } ?: R.color.black
                        binding.cvCardFront.backgroundTintList =
                            ContextCompat.getColorStateList(
                                this@FlashCardGameActivity,
                                deckColorCode
                            )
                    }
                    currentX1 = 0f
                    currentY1 = 0f
                }
            })
            .start()
    }

    private fun onCardKnownNot(
        view: View,
        currentX: Float,
        cardStartX: Float,
        cardStartY: Float,
        currentY: Float
    ) {
        var currentX1 = currentX
        var currentY1 = currentY
        completeSwipeToLeft(view, currentX1)

        onSwipeToLeftCompleted(view, cardStartX, cardStartY, currentX1, currentY1)
    }

    private fun onSwipeToLeftCompleted(
        view: View,
        cardStartX: Float,
        cardStartY: Float,
        currentX1: Float,
        currentY1: Float
    ) {
        var currentX11 = currentX1
        var currentY11 = currentY1
        lifecycleScope.launch {
            delay(500)
            view.animate()
                .x(cardStartX)
                .y(cardStartY)
                .setDuration(0)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (currentX11 < MIN_SWIPE_DISTANCE) {
                            if (!isFront) {
                                initCardLayout()
                            }
                            if (viewModel.swipe(false)) {
                                onQuizComplete()
                            }
                            currentY11 = 0f
                            currentX11 = 0f
                        }
                    }
                })
                .start()
        }
    }

    private fun completeSwipeToLeft(view: View, currentX1: Float) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        view.animate()
            .x(currentX1 - width)
            .setDuration(250)
            .start()
    }

    private fun onCardKnown(
        view: View,
        currentX: Float,
        cardStartX: Float,
        cardStartY: Float,
        currentY: Float
    ) {
        var currentX1 = currentX
        var currentY1 = currentY
        completeSwipeToRight(view, currentX1)
        onSwipeToRightComplete(view, cardStartX, cardStartY, currentX1, currentY1)
    }

    private fun onSwipeToRightComplete(
        view: View,
        cardStartX: Float,
        cardStartY: Float,
        currentX1: Float,
        currentY1: Float
    ) {
        var currentX11 = currentX1
        var currentY11 = currentY1
        lifecycleScope.launch {
            delay(500)
            view.animate()
                .x(cardStartX)
                .y(cardStartY)
                .setDuration(0)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (currentX11 > (MIN_SWIPE_DISTANCE * (-1))) {
                            if (!isFront) {
                                initCardLayout()
                            }
                            if (viewModel.swipe(true)) {
                                onQuizComplete()
                            }
                            currentY11 = 0f
                            currentX11 = 0f
                        }
                    }
                })
                .start()
        }
    }

    private fun completeSwipeToRight(view: View, currentX1: Float) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        view.animate()
            .x(currentX1 + width)
            .setDuration(250)
            .start()
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
                    ContextCompat.getColorStateList(this@FlashCardGameActivity, deckColorCode)
                binding.cvCardBack.backgroundTintList =
                    ContextCompat.getColorStateList(this@FlashCardGameActivity, deckColorCode)
            }
        }

    }

    private fun flipCard() {
        val scale: Float = applicationContext.resources.displayMetrics.density
        binding.cvCardFront.cameraDistance = 8000 * scale
        binding.cvCardBack.cameraDistance = 8000 * scale
        frontAnim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.front_animator
        ) as AnimatorSet
        backAnim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.back_animator
        ) as AnimatorSet
        isFront = if (isFront) {
            frontAnim.setTarget(binding.cvCardFront)
            backAnim.setTarget(binding.cvCardBack)
            frontAnim.start()
            backAnim.start()
            false
        } else {
            frontAnim.setTarget(binding.cvCardBack)
            backAnim.setTarget(binding.cvCardFront)
            backAnim.start()
            frontAnim.start()
            true
        }
    }

    @SuppressLint("ResourceType")
    private fun bindCard(onScreenCards: FlashCardGameModel, cardOrientation: String) {

        val deckColorCode = viewModel.deck?.deckColorCode?.let {
            DeckColorCategorySelector().selectColor(it)
        } ?: R.color.black
        val sumCardsInDeck = viewModel.getTotalCards()
        val currentCardNumber = viewModel.getCurrentCardNumber()

        if (cardOrientation == CARD_ORIENTATION_BACK_AND_FRONT) {
            onCardOrientationBackFront()
            val onFlippedBackgroundColor = MaterialColors.getColorStateListOrNull(
                this,
                com.google.android.material.R.attr.colorSurfaceContainerHigh
            )
            val text = onScreenCards.bottom?.cardDefinition?.first()?.definition
            bindCardBottom(
                onFlippedBackgroundColor,
                onScreenCards,
                deckColorCode,
                text,
                currentCardNumber,
                sumCardsInDeck
            )
        } else {
            val onFlippedBackgroundColor = MaterialColors.getColorStateListOrNull(
                this,
                com.google.android.material.R.attr.colorSurfaceContainer
            )
            val text = onScreenCards.bottom?.cardContent?.content
            bindCardBottom(
                onFlippedBackgroundColor,
                onScreenCards,
                deckColorCode,
                text,
                currentCardNumber,
                sumCardsInDeck
            )
        }
        bindCardFrontAndBack(deckColorCode, onScreenCards, currentCardNumber, sumCardsInDeck)
    }

    private fun bindCardFrontAndBack(
        deckColorCode: Int,
        onScreenCards: FlashCardGameModel,
        currentCardNumber: Int,
        sumCardsInDeck: Int
    ) {
        binding.cvCardFront.backgroundTintList =
            ContextCompat.getColorStateList(this, deckColorCode)

        binding.tvQuizFront.text = onScreenCards.top.cardContent?.content

        val correctDefinitions = getCorrectDefinition(onScreenCards.top.cardDefinition)
        val views = arrayListOf<TextView>()
        tvDefinitions.forEachIndexed { index, tv ->
            if (index < correctDefinitions?.size!!) {
                tv.visibility = View.VISIBLE
                tv.text = correctDefinitions[index].definition
                views.add(tv)
            } else {
                tv.visibility = View.GONE
            }
        }

        binding.cvCardBack.backgroundTintList = ContextCompat.getColorStateList(this, deckColorCode)
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

        binding.btCardFrontSpeak.setOnClickListener { v ->
            if (tts?.isSpeaking == true) {
                stopReading(listOf(binding.tvQuizFront), v as Button)
            } else {
                val language = onScreenCards.top.cardContentLanguage ?: viewModel.deck?.cardContentDefaultLanguage!!
                val textToRead = TextWithLanguageModel(onScreenCards.top.cardContent?.content!!, language)
                readText(
                    listOf(textToRead),
                    listOf(binding.tvQuizFront),
                    v as Button
                )
            }
        }
        binding.btCardBackSpeak.setOnClickListener { v ->
            if (tts?.isSpeaking == true) {
                stopReading(views, v as Button)
            } else {
                val definitions = cardDefinitionsToStrings(correctDefinitions)
                val textsToRead = definitions.map { d ->
                    val language = onScreenCards.top.cardDefinitionLanguage ?: viewModel.deck?.cardDefinitionDefaultLanguage!!
                    TextWithLanguageModel(d, language)
                }
                readText(
                    textsToRead,
                    views,
                    v as Button
                )
            }
        }
    }

    private fun stopReading(views: List<TextView>, button: Button) {
        tts?.stop()
        views.forEach { v ->
            v.setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK))
        }
    }

    private fun cardDefinitionsToStrings(definitions: List<CardDefinition>?): List<String> {
        val correctAlternative = mutableListOf<String>()
        definitions?.forEach {
            correctAlternative.add(it.definition)
        }
        return correctAlternative
    }

    private fun readText(
        text: List<TextWithLanguageModel>,
        view: List<TextView>,
        v: Button
    ) {

        var position = 0
        val textSum = text.size
        val textColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
        val onReadColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, Color.GRAY)

        val params = Bundle()

        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                view[position].setTextColor(onReadColor)
            }

            override fun onDone(utteranceId: String?) {
                view[position].setTextColor(textColor)
                position += 1
                if (position < textSum) {
                    speak(params, text, position, this)
                } else {
                    position = 0
                    return
                }
            }

            override fun onError(utteranceId: String?) {
                Toast.makeText(this@FlashCardGameActivity, getString(R.string.error_read), Toast.LENGTH_LONG).show()
            }
        }

        speak(params, text, position, speechListener)

    }

    private fun speak(
        params: Bundle,
        text: List<TextWithLanguageModel>,
        position: Int,
        speechListener: UtteranceProgressListener
    ) {
        tts?.language = Locale.forLanguageTag(
            LanguageUtil().getLanguageCodeForTextToSpeech(text[position].language)!!
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts?.speak(text[position].text, TextToSpeech.QUEUE_ADD, params, "UniqueID")
        tts?.setOnUtteranceProgressListener(speechListener)
    }


    private fun bindCardBottom(
        onFlippedBackgroundColor: ColorStateList?,
        onScreenCards: FlashCardGameModel,
        deckColorCode: Int,
        text: String?,
        currentCardNumber: Int,
        sumCardsInDeck: Int
    ) {
        binding.clCardBottomContainer.backgroundTintList = onFlippedBackgroundColor
        if (onScreenCards.bottom != null) {
            binding.cvCardBottom.backgroundTintList =
                ContextCompat.getColorStateList(this, deckColorCode)
            binding.tvQuizBottom.text = text
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

    private fun getCorrectDefinition(definitions: List<CardDefinition>?): List<CardDefinition>? {
        definitions?.let { defins ->
            return defins.filter { isCorrect(it.isCorrectDefinition) }
        }
        return null
    }

    fun isCorrect(index: Int?) = index == 1

    private fun initFlashCard(
        cardList: MutableList<ImmutableCard?>,
        deck: ImmutableDeck,
        initOriginalCardList: Boolean = false
    ) {
        isFlashCardGameScreenHidden(false)
        binding.lyOnNoMoreCardsErrorContainer.isVisible = false
        binding.lyGameReviewContainer.isVisible = false
        initCardLayout()
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateCardToRevise(getCardCount())
        if (initOriginalCardList) { viewModel.initOriginalCardList(cardList) }
        viewModel.updateOnScreenCards()
        binding.topAppBar.apply {
            setNavigationOnClickListener { finish() }
            title = getString(R.string.bt_flash_card_game_start)
            subtitle = deck.deckName
        }

    }

    fun initCardLayout() {
        binding.cvCardFront.alpha = 1f
        binding.cvCardFront.rotationY = 0f
        isFront = true
        binding.cvCardBack.alpha = 0f
    }

    private fun onCardOrientationBackFront() {
        binding.cvCardBack.alpha = 1f
        binding.cvCardBack.rotationY = 0f
        isFront = false
        binding.cvCardFront.alpha = 0f
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    override fun onInit(status: Int) {
        when(status) {
            TextToSpeech.SUCCESS -> {
                tts?.setSpeechRate(1.0f)
            }
            else -> {
                Toast.makeText(this, getString(R.string.error_read), Toast.LENGTH_LONG).show()
            }
        }
    }

}