package com.soaharisonstebane.mneme.quiz.flashCardGame

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
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.databinding.ActivityFlashCardGameBinding
import com.soaharisonstebane.mneme.mainActivity.MainActivity
import com.soaharisonstebane.mneme.settings.MiniGameSettingsSheet
import com.soaharisonstebane.mneme.helper.DeckColorCategorySelector
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.CARD_ORIENTATION_BACK_AND_FRONT
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.CHECKED_CARD_ORIENTATION
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.CHECKED_FILTER
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.FILTER_BY_LEVEL
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.FILTER_CREATION_DATE
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.FILTER_RANDOM
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.IS_UNKNOWN_CARD_FIRST
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY
import com.soaharisonstebane.mneme.helper.LanguageUtil
import com.soaharisonstebane.mneme.util.ThemePicker
import com.soaharisonstebane.mneme.util.UiState
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.soaharisonstebane.mneme.backend.models.ExternalCardDefinition
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.backend.models.ExternalDeck
import com.soaharisonstebane.mneme.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.soaharisonstebane.mneme.databinding.LyAudioPlayerBinding
import com.soaharisonstebane.mneme.helper.AppMath
import com.soaharisonstebane.mneme.helper.AppThemeHelper
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel
import com.soaharisonstebane.mneme.helper.playback.AndroidAudioPlayer
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.CARD_COUNT
import com.soaharisonstebane.mneme.util.TextType.CONTENT
import com.soaharisonstebane.mneme.util.TextType.DEFINITION
import com.soaharisonstebane.mneme.util.TextWithLanguageModel
import com.soaharisonstebane.mneme.helper.parcelable
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.abs


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

    private var miniGamePref: SharedPreferences? = null
    private var miniGamePrefEditor: SharedPreferences.Editor? = null
    private var deckWithCards: ExternalDeckWithCardsAndContentAndDefinitions? = null
    private lateinit var frontAnim: AnimatorSet
    private lateinit var backAnim: AnimatorSet
    var isFront = true
    private var dx: Float = 0.0f
    private var dy: Float = 0.0f

    private var isDragging = false
    private var tts: TextToSpeech? = null

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

        miniGamePref = getSharedPreferences(
            FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF,
            Context.MODE_PRIVATE
        )
        miniGamePrefEditor = miniGamePref?.edit()
        val themePicker = ThemePicker()

        deckWithCards = intent?.parcelable(DECK_ID_KEY)

        val deckColorCode = deckWithCards?.deck?.deckBackground
        val theme = themePicker.selectThemeByDeckColorCode(deckColorCode)
        setTheme(theme)
        tts = TextToSpeech(this, this)

        binding = ActivityFlashCardGameBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        deckWithCards?.let {
            val cardList = it.cards.toMutableList()
            val deck = it.deck
            if (cardList.isNotEmpty()) {
                viewModel.initCardList(cardList)
                viewModel.initOriginalCardList(cardList)
                viewModel.initDeck(deck)
                initFlashCard(cardList, deck, true)
            } else {
                onNoCardToRevise()
            }
        }

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

        applySettings()
        completelyRestartFlashCard(getCardOrientation())

        gameOn(binding.clOnScreenCardRoot)

        binding.btKnow.setOnClickListener {
            onCardKnown(binding.clOnScreenCardRoot)
        }
        binding.btNotKnow.setOnClickListener {
            onCardKnownNot(binding.clOnScreenCardRoot)
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

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.mn_bt_settings) {
                MiniGameSettingsSheet.newInstance(FLASH_CARD_QUIZ).show(supportFragmentManager, MiniGameSettingsSheet.TAG)
                true
            } else {
                false
            }
        }

        setActinButtonsColor()

    }

    fun setActinButtonsColor() {
        when(AppThemeHelper.getSavedTheme(this)) {
            1 -> {
                setKnownButtonColorOnLightTheme()
                setUnKnownButtonColorOnLightTheme()
            }
            2 -> {
                setKnownButtonColorOnDarkTheme()
                setUnKnownButtonColorOnDarkTheme()
            }
            else -> {
                if (AppThemeHelper.isSystemDarkTheme(this))  {
                    setKnownButtonColorOnDarkTheme()
                    setUnKnownButtonColorOnDarkTheme()
                } else {
                    setKnownButtonColorOnLightTheme()
                    setUnKnownButtonColorOnLightTheme()
                }
            }
        }
    }

    fun setKnownButtonColorOnLightTheme() {
        binding.btKnow.apply {
            background.setTint(ContextCompat.getColor(context, R.color.green100))
            iconTint = ContextCompat.getColorStateList(context, R.color.red950)
        }
    }

    fun setKnownButtonColorOnDarkTheme() {
        binding.btKnow.apply {
            background.setTint(ContextCompat.getColor(context, R.color.green700))
            iconTint = ContextCompat.getColorStateList(context, R.color.green50)
        }
    }

    fun setUnKnownButtonColorOnLightTheme() {
        binding.btNotKnow.apply {
            background.setTint(ContextCompat.getColor(context, R.color.red100))
            iconTint = ContextCompat.getColorStateList(context, R.color.red950)
        }
    }

    fun setUnKnownButtonColorOnDarkTheme() {
        binding.btNotKnow.apply {
            background.setTint(ContextCompat.getColor(context, R.color.red700))
            iconTint = ContextCompat.getColorStateList(context, R.color.red50)
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
            view.parent.requestDisallowInterceptTouchEvent(true)

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    dx = view.translationX - motionEvent.rawX
                    dy = view.translationY - motionEvent.rawY

                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(motionEvent.rawX + dx) > 10 || abs(motionEvent.rawY + dy) > 10) {
                        isDragging = true
                    }
                    onCardMoved(view, motionEvent)
                }

                MotionEvent.ACTION_UP -> {
                    val currentX = view.translationX
                    when {
                        currentX > MIN_SWIPE_DISTANCE && currentX <= 0 || currentX < -MIN_SWIPE_DISTANCE && currentX >= 0 -> {
                            flipCardOnClicked(view)
                        }

                        currentX < MIN_SWIPE_DISTANCE -> {
                            onCardKnownNot(view)
                        }
                        currentX > -MIN_SWIPE_DISTANCE -> {
                            onCardKnown(view)
                        }

                    }
                }
            }
            true
        }
    }

    private fun onCardMoved(view: View, motionEvent: MotionEvent) {
        view.translationX = motionEvent.rawX + dx
        view.translationY = motionEvent.rawY + dy

        when {
            view.translationX < -MIN_SWIPE_DISTANCE -> isCardKnown(false)
            view.translationX > MIN_SWIPE_DISTANCE -> isCardKnown(true)
            else -> isCardKnown(null)
        }
    }

    private fun flipCardOnClicked(view: View) {
        view.animate()
            .translationX(0f)
            .translationY(0f)
            .setDuration(150)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!isDragging) {
                        flipCard()
                    }
                    isDragging = false
                }
            }).start()
    }

    private fun onCardKnown(view: View) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        view.animate()
            .translationX(screenWidth)
            .setDuration(250)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (viewModel.swipe(true)) onQuizComplete()
                    resetCardPosition(view)
                }
            }).start()

        if (tts?.isSpeaking == true) stopReadingAllText()
    }

    private fun onCardKnownNot(view: View) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        view.animate()
            .translationX(-screenWidth)
            .setDuration(250)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (viewModel.swipe(false)) onQuizComplete()
                    resetCardPosition(view)
                }
            }).start()

        if (tts?.isSpeaking == true) stopReadingAllText()
    }

    private fun resetCardPosition(view: View) {
        view.translationX = 0f
        view.translationY = 0f
        if (!isFront) initCardLayout()
        flashCardProgressBarAdapter.notifyDataSetChanged()
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
                        viewModel.deck?.deckBackground
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
            viewModel.deck?.deckBackground
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
//        viewModel.initCardList(cardList)
//        viewModel.initDeck(deck)
        viewModel.updateCardToRevise(getCardCount())
//        if (initOriginalCardList) {
//            viewModel.initOriginalCardList(cardList)
//        }
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