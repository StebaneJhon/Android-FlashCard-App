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
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
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
import com.google.android.material.snackbar.Snackbar
import com.ssoaharison.recall.backend.models.ExternalCardDefinition
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.ssoaharison.recall.databinding.LyAudioPlayerBinding
import com.ssoaharison.recall.databinding.LyQuizQuestionBinding
import com.ssoaharison.recall.helper.AppMath
import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.PhotoModel
import com.ssoaharison.recall.helper.playback.AndroidAudioPlayer
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_COUNT
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextType.DEFINITION
import com.ssoaharison.recall.util.TextWithLanguageModel
import com.ssoaharison.recall.util.parcelable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
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

    private lateinit var flashCardProgressBarAdapter: FlashCardProgressBarAdapter

//    private var sharedPref: SharedPreferences? = null
//    private var editor: SharedPreferences.Editor? = null
    private var miniGamePref: SharedPreferences? = null
    private var miniGamePrefEditor: SharedPreferences.Editor? = null
    private var deckWithCards: ExternalDeckWithCardsAndContentAndDefinitions? = null
    private lateinit var frontAnim: AnimatorSet
    private lateinit var backAnim: AnimatorSet
    var isFront = true
    private var dx: Float = 0.0f
    private var dy: Float = 0.0f

    private var modalBottomSheet: MiniGameSettingsSheet? = null
    private var tts: TextToSpeech? = null

//    private lateinit var tvDefinitions: List<TextView>
    private lateinit var tvDefinitions: List<FlashcardContentLyModel>
    private val EXTRA_MARGIN = -2

    var lastPlayedAudioFile: AudioModel? = null
    var lastPlayedAudioViw: LyAudioPlayerBinding? = null

    var statusBarHeight: Float = 58f

    private val player by lazy {
        AndroidAudioPlayer(this)
    }

    companion object {
        const private val MIN_SWIPE_DISTANCE = -275
        private const val TAG = "FlashCardGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        miniGamePref = getSharedPreferences(
            FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF,
            Context.MODE_PRIVATE
        )
//        editor = sharedPref?.edit()
        miniGamePrefEditor = miniGamePref?.edit()
        val themePicker = ThemePicker()
//        val appTheme = sharedPref?.getString("themName", "BASE THEME")
//        val themRef = themePicker.selectTheme(appTheme)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)

        val deckColorCode = deckWithCards?.deck?.deckColorCode
        val theme = themePicker.selectThemeByDeckColorCode(deckColorCode)
        setTheme(theme)

//        if (deckColorCode.isNullOrBlank() && themRef != null) {
//            setTheme(themRef)
//        } else if (themRef != null && !deckColorCode.isNullOrBlank()) {
//            val deckTheme = if (appTheme == DARK_THEME) {
//                themePicker.selectDarkThemeByDeckColorCode(deckColorCode, themRef)
//            } else {
//                themePicker.selectThemeByDeckColorCode(deckColorCode, themRef)
//            }
//            setTheme(deckTheme)
//        } else {
//            setTheme(themePicker.getDefaultTheme())
//        }

        tts = TextToSpeech(this, this)

        binding = ActivityFlashCardGameBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        WindowCompat.enableEdgeToEdge(window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout2) { v, windowInserts ->
            val insets = windowInserts.getInsets(WindowInsetsCompat.Type.statusBars())
            val px = insets.top
            statusBarHeight = px / resources.displayMetrics.density
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

//        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
//        statusBarHeight = if (resourceId > 0) {
//            val px = resources.getDimensionPixelSize(resourceId).toFloat()
//            px / resources.displayMetrics.density
//        } else {
//            0f
//        }

        deckWithCards?.let {
            val cardList = it.cards?.toMutableList()
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                initFlashCard(cardList, deck, true)
            } else {
                onNoCardToRevise()
            }
        }

//        tvDefinitions = listOf(
//            binding.tvQuizBack1,
//            binding.tvQuizBack2,
//            binding.tvQuizBack3,
//            binding.tvQuizBack4,
//            binding.tvQuizBack5,
//            binding.tvQuizBack6,
//            binding.tvQuizBack7,
//            binding.tvQuizBack8,
//            binding.tvQuizBack9,
//            binding.tvQuizBack10,
//        )

        tvDefinitions = listOf(
            FlashcardContentLyModel(container = binding.containerBack1, view = binding.inBack1),
            FlashcardContentLyModel(container = binding.containerBack2, view = binding.inBack2),
            FlashcardContentLyModel(container = binding.containerBack3, view = binding.inBack3),
            FlashcardContentLyModel(container = binding.containerBack4, view = binding.inBack4),
            FlashcardContentLyModel(container = binding.containerBack5, view = binding.inBack5),
            FlashcardContentLyModel(container = binding.containerBack6, view = binding.inBack6),
            FlashcardContentLyModel(container = binding.containerBack7, view = binding.inBack7),
            FlashcardContentLyModel(container = binding.containerBack8, view = binding.inBack8),
            FlashcardContentLyModel(container = binding.containerBack9, view = binding.inBack9),
            FlashcardContentLyModel(container = binding.containerBack10, view = binding.inBack10),
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
                            viewModel.startTimer()
                            bindCard(state.data, getCardOrientation())
                        }
                    }
                }
        }

        lifecycleScope.launch {
            viewModel.getFlashCardCards()
            viewModel.flashCardCardsCardsToRevise.collect { state ->
                when (state) {
                    is UiState.Error -> {}
                    is UiState.Loading -> {}
                    is UiState.Success -> {
                        displayProgression(state.data, binding.rvMiniGameProgression)
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
                flashCardProgressBarAdapter.notifyDataSetChanged()
                if (!isFront) {
                    initCardLayout()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.error_message_previous_card),
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun displayProgression(data: List<FlashCardCardModel?>, recyclerView: RecyclerView) {
        flashCardProgressBarAdapter = FlashCardProgressBarAdapter(
            cardList = data,
            context = this,
            recyclerView
        )
        binding.rvMiniGameProgression.apply {
            adapter = flashCardProgressBarAdapter
            layoutManager = LinearLayoutManager(
                this@FlashCardGameActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
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
        val cardHeight = card.height
        val cardStartX = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)
        val cardStartY = (displayMetrics.heightPixels.toFloat() / 2) - (cardHeight / 2) + binding.appBarLayout2.height
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
        val cardHeight = card.height
        val cardStartX = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)
        val cardStartY = (displayMetrics.heightPixels.toFloat() / 2) - (cardHeight / 2) + binding.appBarLayout2.height
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
        viewModel.pauseTimer()
        isFlashCardGameScreenHidden(true)
        binding.lyGameReviewContainer.isVisible = true
        binding.lyGameReviewLayout.apply {
            tvTotalCardsSumScoreLayout.text = viewModel.getRevisedCardsCount().toString()
            tvAccuracyScoreLayout.text = getString(
                R.string.text_accuracy_mini_game_review,
                viewModel.getUserAnswerAccuracy()
            )
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.timer.collect { t ->
                        tvTimeScoreLayout.text = viewModel.formatTime(t)
                    }
                }
            }

            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getUserAnswerAccuracyFraction(),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.red400),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.green400),
            ) as Int

            val textColorKnownCards = ArgbEvaluator().evaluate(
                viewModel.getUserAnswerAccuracyFraction(),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.red50),
                ContextCompat.getColor(this@FlashCardGameActivity, R.color.green50)
            ) as Int
            tvAccuracyScoreLayout.setTextColor(textColorKnownCards)
            tvAccuracyCardsScoreLayout.setTextColor(textColorKnownCards)

            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@FlashCardGameActivity, MainActivity::class.java))
                finish()
            }
//            btRestartQuizWithPreviousCardsScoreLayout.setOnClickListener {
//                isFlashCardGameScreenHidden(false)
//                binding.lyOnNoMoreCardsErrorContainer.isVisible = false
//                binding.lyGameReviewContainer.isVisible = false
//                viewModel.initFlashCard()
//                viewModel.updateOnScreenCards()
//                if (getCardOrientation() == CARD_ORIENTATION_FRONT_AND_BACK) {
//                    initCardLayout()
//                } else {
//                    onCardOrientationBackFront()
//                }
//            }
//            btRestartQuizWithAllCardsScoreLayout.setOnClickListener {
//                completelyRestartFlashCard(getCardOrientation())
//            }
//            if (viewModel.getMissedCardSum() == 0) {
//                btReviseMissedCardScoreLayout.isActivated = false
//                btReviseMissedCardScoreLayout.isVisible = false
//            } else {
//                btReviseMissedCardScoreLayout.isActivated = true
//                btReviseMissedCardScoreLayout.isVisible = true
//                btReviseMissedCardScoreLayout.setOnClickListener {
//                    isFlashCardGameScreenHidden(false)
//                    binding.lyOnNoMoreCardsErrorContainer.isVisible = false
//                    binding.lyGameReviewContainer.isVisible = false
//                    viewModel.initCurrentCardPosition()
//                    viewModel.initProgress()
//                    viewModel.updateCardOnReviseMissedCards()
//                    viewModel.updateOnScreenCards()
//                    if (getCardOrientation() == CARD_ORIENTATION_FRONT_AND_BACK) {
//                        initCardLayout()
//                    } else {
//                        onCardOrientationBackFront()
//                    }
//                }
//            }

            if (viewModel.getCardLeft() <= 0) {
                btContinueQuizScoreLayout.visibility = View.GONE
                tvLeftCardsScoreLayout.text = getString(R.string.text_all_cards_learned)
            } else {
                tvLeftCardsScoreLayout.text =
                    getString(R.string.text_cards_left_in_deck, viewModel.getCardLeft())
                btContinueQuizScoreLayout.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        isFlashCardGameScreenHidden(false)
                        binding.lyOnNoMoreCardsErrorContainer.isVisible = false
                        binding.lyGameReviewContainer.isVisible = false
                        viewModel.initFlashCard()
                        viewModel.updateCardToRevise(getCardCount())
                        viewModel.getFlashCardCards()
                        flashCardProgressBarAdapter.notifyDataSetChanged()
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
        viewModel.getFlashCardCards()
        flashCardProgressBarAdapter.notifyDataSetChanged()
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
            val cardHeight = view.height
            val cardStartX = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)
            val cardStartY = (displayMetrics.heightPixels.toFloat() / 2) - (cardHeight / 2)  + binding.appBarLayout2.height

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
                        val deckColorCode =
                            DeckColorCategorySelector().selectDeckColorStateListSurfaceContainerLow(
                                this@FlashCardGameActivity,
                                viewModel.deck?.deckColorCode
                            )
                        binding.cvCardFront.backgroundTintList = deckColorCode
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
                            flashCardProgressBarAdapter.notifyDataSetChanged()

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
        if (tts?.isSpeaking == true) {
            stopReadingAllText()
        }
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
                            flashCardProgressBarAdapter.notifyDataSetChanged()


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
        if (tts?.isSpeaking == true) {
            stopReadingAllText()
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
                val deckColorCode =
                    DeckColorCategorySelector().selectDeckColorStateListSurfaceContainerLow(
                        this,
                        viewModel.deck?.deckColorCode
                    )
                binding.cvCardFront.backgroundTintList = deckColorCode
                binding.cvCardBack.backgroundTintList = deckColorCode
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
        if (tts?.isSpeaking == true) {
            stopReadingAllText()
        }
    }

    @SuppressLint("ResourceType")
    private fun bindCard(onScreenCards: FlashCardGameModel, cardOrientation: String) {

        val deckColorCode = DeckColorCategorySelector().selectDeckColorStateListSurfaceContainerLow(
            this,
            viewModel.deck?.deckColorCode
        )

        if (cardOrientation == CARD_ORIENTATION_BACK_AND_FRONT) {
            onCardOrientationBackFront()
            val onFlippedBackgroundColor = MaterialColors.getColorStateListOrNull(
                this,
                com.google.android.material.R.attr.colorSurfaceContainerHigh
            )
//            val text = onScreenCards.bottom?.cardDefinition?.first()?.definition
//            onScreenCards.bottom?.contentWithDefinitions?.definitions?.first()?.definitionText?.let { text ->
//
//            }
            val text = onScreenCards.bottom?.contentWithDefinitions?.definitions?.first()?.definitionText

            bindCardBottom(
                onFlippedBackgroundColor,
                onScreenCards,
                deckColorCode,
                text,
                onScreenCards.bottom?.contentWithDefinitions?.definitions?.first()?.definitionImage,
                onScreenCards.bottom?.contentWithDefinitions?.definitions?.first()?.definitionAudio,
            )
        } else {
            val onFlippedBackgroundColor = MaterialColors.getColorStateListOrNull(
                this,
                com.google.android.material.R.attr.colorSurfaceContainer
            )
//            val text = onScreenCards.bottom?.cardContent?.content
            val text = onScreenCards.bottom?.contentWithDefinitions?.content?.contentText
            bindCardBottom(
                onFlippedBackgroundColor,
                onScreenCards,
                deckColorCode,
                text,
                onScreenCards.bottom?.contentWithDefinitions?.content?.contentImage,
                onScreenCards.bottom?.contentWithDefinitions?.content?.contentAudio,
                )
        }
        bindCardFrontAndBack(deckColorCode, onScreenCards,)
    }

    private fun bindCardFrontAndBack(
        deckColorCode: ColorStateList?,
        onScreenCards: FlashCardGameModel,
    ) {
        binding.cvCardFront.backgroundTintList = deckColorCode

//        binding.tvQuizFront.text = onScreenCards.top.cardContent?.content
        //binding.inFront.tvText.text = onScreenCards.top.contentWithDefinitions.content.contentText
        val contentText = onScreenCards.top.contentWithDefinitions.content.contentText
        val contentImage = onScreenCards.top.contentWithDefinitions.content.contentImage
        val contentAudio = onScreenCards.top.contentWithDefinitions.content.contentAudio
        if (contentText != null) {
            val spannableString = Html.fromHtml(contentText, FROM_HTML_MODE_LEGACY).trim()
            binding.inFront.tvText.apply {
                visibility = View.VISIBLE
                text = spannableString
            }
        } else {
            binding.inFront.tvText.visibility = View.GONE
        }

        if (contentImage != null) {
            val photoFile = File(this.filesDir, contentImage.name)
            val photoBytes = photoFile.readBytes()
            val photoBtm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
            binding.inFront.imgPhoto.apply {
                visibility = View.VISIBLE
                setImageBitmap(photoBtm)
            }
        } else {
            binding.inFront.imgPhoto.visibility = View.GONE
        }

        if (contentAudio != null) {
            binding.inFront.llAudioContainer.visibility = View.VISIBLE
            binding.inFront.inAudioPlayer.btPlay.setOnClickListener {
                playPauseAudio(binding.inFront.inAudioPlayer, contentAudio)
            }
        } else {
            binding.inFront.llAudioContainer.visibility = View.GONE
        }

//        val correctDefinitions = getCorrectDefinition(onScreenCards.top.cardDefinition)
        val correctDefinitions = getCorrectDefinition(onScreenCards.top.contentWithDefinitions.definitions)
        val views = arrayListOf<TextView>()
        tvDefinitions.forEachIndexed { index, ly ->
            if (index < correctDefinitions?.size!!) {
                ly.container.visibility = View.VISIBLE
//                tv.text = correctDefinitions[index].definition
                //ly.view.tvText.text = correctDefinitions[index].definitionText
                val definitionText = correctDefinitions[index].definitionText
                val definitionImage = correctDefinitions[index].definitionImage
                val definitionAudio = correctDefinitions[index].definitionAudio

                if (definitionText != null) {
                    val spannableString = Html.fromHtml(definitionText, FROM_HTML_MODE_LEGACY).trim()
                    ly.view.tvText.apply {
                        visibility = View.VISIBLE
                        text = spannableString
                    }
                } else {
                    ly.view.tvText.visibility = View.GONE
                }

                if (definitionImage != null) {
                    val photoFile = File(this.filesDir, definitionImage.name)
                    val photoBytes = photoFile.readBytes()
                    val photoBtm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                    ly.view.imgPhoto.apply {
                        visibility = View.VISIBLE
                        setImageBitmap(photoBtm)
                    }
                } else {
                    ly.view.imgPhoto.visibility = View.GONE
                }

                if (definitionAudio != null) {
                    ly.view.llAudioContainer.visibility = View.VISIBLE
                    ly.view.inAudioPlayer.btPlay.setOnClickListener {
                        playPauseAudio(ly.view.inAudioPlayer, definitionAudio)
                    }
                } else {
                    ly.view.llAudioContainer.visibility = View.GONE
                }

                views.add(ly.view.tvText)
            } else {
                ly.container.visibility = View.GONE
            }
        }

        binding.cvCardBack.backgroundTintList = deckColorCode

        binding.btCardFrontSpeak.setOnClickListener {
            if (tts?.isSpeaking == true) {
                stopReading(listOf(binding.inFront.tvText))
            } else {
//                val language = onScreenCards.top.cardContentLanguage
                val language = onScreenCards.top.card.cardContentLanguage
                    ?: viewModel.deck?.cardContentDefaultLanguage
                if (language.isNullOrBlank()) {
                    val text = Html.fromHtml(onScreenCards.top.contentWithDefinitions.content.contentText, FROM_HTML_MODE_LEGACY).toString()
                    LanguageUtil().detectLanguage(
//                        text = onScreenCards.top.cardContent?.content!!,
                        text = text,
                        onError = { showSnackBar(R.string.error_message_error_while_detecting_language) },
                        onLanguageUnIdentified = { showSnackBar(R.string.error_message_can_not_identify_language) },
                        onLanguageNotSupported = { showSnackBar(R.string.error_message_language_not_supported) },
                        onSuccess = { detectedLanguage ->
                            viewModel.updateCardContentLanguage(
//                                onScreenCards.top.cardId,
                                onScreenCards.top.card.cardId,
                                detectedLanguage
                            )
                            readText(
                                listOf(
                                    TextWithLanguageModel(
//                                        onScreenCards.top.cardId,
                                        cardId = onScreenCards.top.card.cardId,
//                                        text = onScreenCards.top.cardContent.content,
                                        text = text,
                                        textType = CONTENT,
                                        detectedLanguage
                                    )
                                ),
                                listOf(binding.inFront.tvText),
                            )
                        }
                    )
                } else {
                    val text = Html.fromHtml(onScreenCards.top.contentWithDefinitions.content.contentText, FROM_HTML_MODE_LEGACY).toString()
                    readText(
                        listOf(
                            TextWithLanguageModel(
//                                cardId = onScreenCards.top.cardId,
                                cardId = onScreenCards.top.card.cardId,
//                                text = onScreenCards.top.cardContent?.content!!,
                                text = text,
                                textType = CONTENT,
                                language
                            )
                        ),
                        listOf(binding.inFront.tvText),
                    )
                }

            }
        }
        binding.btCardBackSpeak.setOnClickListener {
            if (tts?.isSpeaking == true) {
                stopReading(views)
            } else {
                val definitions = cardDefinitionsToStrings(correctDefinitions)
//                val language = onScreenCards.top.cardDefinitionLanguage ?: viewModel.deck?.cardDefinitionDefaultLanguage
                val language = onScreenCards.top.card.cardDefinitionLanguage ?: viewModel.deck?.cardDefinitionDefaultLanguage
                if (language.isNullOrBlank()) {
                    LanguageUtil().detectLanguage(
                        text = Html.fromHtml(definitions.first(), FROM_HTML_MODE_LEGACY).toString(),
                        onError = { showSnackBar(R.string.error_message_error_while_detecting_language) },
                        onLanguageUnIdentified = { showSnackBar(R.string.error_message_can_not_identify_language) },
                        onLanguageNotSupported = { showSnackBar(R.string.error_message_language_not_supported) },
                        onSuccess = { detectedLanguage ->
                            viewModel.updateCardDefinitionLanguage(
//                                cardId = onScreenCards.top.cardId,
                                cardId = onScreenCards.top.card.cardId,
                                language = detectedLanguage
                            )
                            val textsToRead = definitions.map { d ->
                                TextWithLanguageModel(
//                                    cardId = onScreenCards.top.cardId,
                                    cardId = onScreenCards.top.card.cardId,
                                    text = Html.fromHtml(d, FROM_HTML_MODE_LEGACY).toString(),
                                    textType = DEFINITION,
                                    language = detectedLanguage
                                )
                            }
                            readText(
                                textsToRead,
                                views,
                            )
                        }
                    )
                } else {
                    val textsToRead = definitions.map { d ->
                        TextWithLanguageModel(
//                            cardId = onScreenCards.top.cardId,
                            cardId = onScreenCards.top.card.cardId,
                            text = Html.fromHtml(d, FROM_HTML_MODE_LEGACY).toString(),
                            textType = DEFINITION,
                            language = language
                        )
                    }
                    readText(
                        textsToRead,
                        views,
                    )
                }
            }
        }
    }


    private fun showSnackBar(
        @StringRes messageRes: Int
    ) {
        Snackbar.make(
            binding.clFlashCardRootView,
            getString(messageRes),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun stopReading(views: List<TextView>) {
        tts?.stop()
        views.forEach { v ->
            v.setTextColor(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnSurface,
                    Color.BLACK
                )
            )
        }
    }

    private fun stopReadingAllText() {
        tts?.stop()
        val views = tvDefinitions + listOf(FlashcardContentLyModel(container = binding.containerFront, view = binding.inFront))
        views.forEach { v ->
            v.view.tvText.setTextColor(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnSurface,
                    Color.BLACK
                )
            )
        }
    }

    private fun cardDefinitionsToStrings(definitions: List<ExternalCardDefinition>?): List<String> {
        val correctAlternative = mutableListOf<String>()
        definitions?.forEach {
            it.definitionText?.let { text ->
                correctAlternative.add(text)
            }
        }
        return correctAlternative
    }

    private fun readText(
        text: List<TextWithLanguageModel>,
        view: List<TextView>,
    ) {

        var position = 0
        val textSum = text.size
        val textColor = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnSurface,
            Color.BLACK
        )
        val onReadColor =
            MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, Color.GRAY)

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
                Toast.makeText(
                    this@FlashCardGameActivity,
                    getString(R.string.error_read),
                    Toast.LENGTH_LONG
                ).show()
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
            LanguageUtil().getLanguageCodeForTextToSpeech(text[position].language!!)!!
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts?.speak(text[position].text, TextToSpeech.QUEUE_ADD, params, "UniqueID")
        tts?.setOnUtteranceProgressListener(speechListener)
    }


    private fun bindCardBottom(
        onFlippedBackgroundColor: ColorStateList?,
        onScreenCards: FlashCardGameModel,
        deckColorCode: ColorStateList?,
        text: String?,
        image: PhotoModel?,
        audioModel: AudioModel?
    ) {
        binding.clCardBottomContainer.backgroundTintList = onFlippedBackgroundColor
        if (onScreenCards.bottom != null) {
            binding.cvCardBottom.backgroundTintList = deckColorCode
            //binding.inBottom.tvText.text = text
            if (text != null) {
                val spannableString = Html.fromHtml(text, FROM_HTML_MODE_LEGACY).trim()
                binding.inBottom.tvText.visibility = View.VISIBLE
                binding.inBottom.tvText.text = spannableString
            } else {
                binding.inBottom.tvText.visibility = View.GONE
            }

            if (image != null) {
                val photoFile = File( this.filesDir, image.name)
                val photoBytes = photoFile.readBytes()
                val photoBtm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                binding.inBottom.imgPhoto.apply {
                    visibility = View.VISIBLE
                    setImageBitmap(photoBtm)
                }
            } else {
                binding.inBottom.imgPhoto.visibility = View.GONE
            }

            if (audioModel != null) {
                binding.inBottom.llAudioContainer.visibility = View.VISIBLE
                binding.inBottom.inAudioPlayer.btPlay.setOnClickListener {
                    playPauseAudio(binding.inBottom.inAudioPlayer, audioModel)
                }
            } else {
                binding.inBottom.llAudioContainer.visibility = View.GONE
            }

        } else {
            binding.cvCardBottom.backgroundTintList = deckColorCode
            binding.inBottom.tvText.text = "..."
        }
    }

    private fun playPauseAudio(
        view: LyAudioPlayerBinding,
        audio: AudioModel
    ) {
        when {
            player.hasPlayed() && !player.isPlaying() -> {
                // Resume audio
                view.btPlay.setIconResource(R.drawable.icon_pause)
                player.play()
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(
                            player.getCurrentPosition(),
                            player.getDuration()
                        )
                        view.lpiAudioProgression.progress = progress
                        delay(100L)
                    }
                }
            }

            player.hasPlayed() && player.isPlaying() -> {
                // Pause audio
                view.btPlay.setIconResource(R.drawable.icon_play)
                player.pause()
            }

            !player.hasPlayed() && !player.isPlaying() -> {
                // Play audio
                view.btPlay.setIconResource(R.drawable.icon_pause)
                val audioFile = File(this.filesDir, audio.name)
                player.playFile(audioFile)
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(
                            player.getCurrentPosition(),
                            player.getDuration()
                        )
                        view.lpiAudioProgression.progress = progress
                        delay(100L)
                    }
                }
                player.onCompletion {
                    lastPlayedAudioFile = null
                    lastPlayedAudioViw = null
                    view.btPlay.setIconResource(R.drawable.icon_play)
                }
            }
        }
    }

//    private fun getCorrectDefinition(definitions: List<CardDefinition>?): List<CardDefinition>? {
//        definitions?.let { defins ->
//            return defins.filter { isCorrect(it.isCorrectDefinition) }
//        }
//        return null
//    }

    private fun getCorrectDefinition(definitions: List<ExternalCardDefinition>?): List<ExternalCardDefinition>? {
        definitions?.let { defins ->
            return defins.filter { isCorrect(it.isCorrectDefinition) }
        }
        return null
    }

    fun isCorrect(index: Int?) = index == 1

    private fun initFlashCard(
        cardList: MutableList<ExternalCardWithContentAndDefinitions>,
        deck: ExternalDeck,
        initOriginalCardList: Boolean = false
    ) {
        isFlashCardGameScreenHidden(false)
        binding.lyOnNoMoreCardsErrorContainer.isVisible = false
        binding.lyGameReviewContainer.isVisible = false
        initCardLayout()
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateCardToRevise(getCardCount())
        if (initOriginalCardList) {
            viewModel.initOriginalCardList(cardList)
        }
        viewModel.updateOnScreenCards()
        binding.topAppBar.apply {
            setNavigationOnClickListener { finish() }
            title = deck.deckName
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

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                tts?.setSpeechRate(1.0f)
            }

            else -> {
                Toast.makeText(this, getString(R.string.error_read), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
    }

    override fun onPause() {
        super.onPause()
        if (tts?.isSpeaking == true) {
            stopReadingAllText()
        }
    }

}