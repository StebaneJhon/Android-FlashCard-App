package com.ssoaharison.recall.quiz.quizGame

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.databinding.ActivityTestQuizGameBinding
import com.ssoaharison.recall.mainActivity.MainActivity
import com.ssoaharison.recall.settings.MiniGameSettingsSheet
import com.ssoaharison.recall.util.LanguageUtil
import com.ssoaharison.recall.util.FlashCardMiniGameRef
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.ssoaharison.recall.backend.models.ExternalCardWithContentAndDefinitions
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ExternalDeckWithCardsAndContentAndDefinitions
import com.ssoaharison.recall.quiz.flashCardGame.FlashCardGameActivity
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_COUNT
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextType.DEFINITION
import com.ssoaharison.recall.util.TextWithLanguageModel
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import com.ssoaharison.recall.util.parcelable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class QuizGameActivity :
    AppCompatActivity(),
    MiniGameSettingsSheet.SettingsApplication,
    TextToSpeech.OnInitListener
{

    private lateinit var binding: ActivityTestQuizGameBinding
    private val viewModel: TestQuizGameViewModel by viewModels {
        TestQuizGameViewModelFactory((application as FlashCardApplication).repository)
    }
    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var modalBottomSheet: MiniGameSettingsSheet? = null
    private var miniGamePref: SharedPreferences? = null
    private var miniGamePrefEditor: SharedPreferences.Editor? = null
    private var appTheme: String? = null

    private var deckWithCards: ExternalDeckWithCardsAndContentAndDefinitions? = null
    private lateinit var quizGameAdapter: QuizGameAdapter
    private lateinit var quizGameProgressBarAdapter: QuizGameProgressBarAdapter

    private var tts: TextToSpeech? = null
    private var quizJob: Job? = null
    private var fetchJob1: Job? = null

    companion object {
        private const val TAG = "QuizGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
        const val TIME_BEFORE_HIDING_ACTIONS = 200L
        const val TIME_BEFORE_SHOWING_ACTIONS = 700L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        miniGamePref = getSharedPreferences(
            FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF,
            Context.MODE_PRIVATE
        )
        val themePicker = ThemePicker()
        miniGamePrefEditor = miniGamePref?.edit()
        appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = themePicker.selectTheme(appTheme)

        deckWithCards = intent?.parcelable(FlashCardGameActivity.DECK_ID_KEY)

        val deckColorCode = deckWithCards?.deck?.deckColorCode

        if (deckColorCode.isNullOrBlank() && themRef != null) {
            setTheme(themRef)
        } else if (themRef != null && !deckColorCode.isNullOrBlank()) {
            val deckTheme = if (appTheme == DARK_THEME) {
                themePicker.selectDarkThemeByDeckColorCode(deckColorCode, themRef)
            } else {
                themePicker.selectThemeByDeckColorCode(deckColorCode, themRef)
            }
            setTheme(deckTheme)
        } else {
            setTheme(themePicker.getDefaultTheme())
        }

        binding = ActivityTestQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards.toMutableList()
            val deck = it.deck
            if (!cardList.isEmpty()) {
                viewModel.initCardList(cardList)
                viewModel.initOriginalCardList(cardList)
                startTest(cardList, deck)
            }
        }

        binding.vpCardHolder.isUserInputEnabled = false

        binding.topAppBar.apply {
            title = getString(R.string.start_quiz_button_text)
            subtitle = viewModel.deck?.deckName
            setNavigationOnClickListener { finish() }
        }

        applySettings()
        completelyRestartQuiz()

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
        completelyRestartQuiz()
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
    }

    private fun launchTestQuizGame(
        data: List<QuizGameCardModel>
    ) {
        quizGameAdapter = QuizGameAdapter(
            this,
            data,
            appTheme ?: "WHITE THEM",
            viewModel.deck!!,
            { userAnswer ->
                viewModel.submitUserAnswer(userAnswer, binding.vpCardHolder.currentItem)
                val actualCard = viewModel.getCardByPosition(binding.vpCardHolder.currentItem)
                if (userAnswer.cardType != SINGLE_ANSWER_CARD && actualCard.attemptTime <= 1) {
                    viewModel.updateMultipleAnswerAndChoiceCardOnAnswered(userAnswer, binding.vpCardHolder.currentItem)
                }
                quizGameAdapter.notifyDataSetChanged()
                quizGameProgressBarAdapter.notifyDataSetChanged()
                if (viewModel.isAllAnswerSelected(userAnswer, binding.vpCardHolder.currentItem)) {
                    optionsState(userAnswer, actualCard)
                }
            },
            { dataToRead ->
                if (tts?.isSpeaking == true) {
                    stopReading(dataToRead.views)
                } else {
                    readText(
                        dataToRead.text,
                        dataToRead.views,
                    )
                }

            })

        binding.vpCardHolder.apply {
            adapter = quizGameAdapter
            registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    binding.btNext.apply {
                        if (position == data.size.minus(1)) {
                            text = getString(R.string.text_finish)
                            iconPadding =
                                resources.getDimension(R.dimen.icon_padding_next_question_button)
                                    .toInt()
                        } else {
                            text = null
                            iconPadding =
                                resources.getDimension(R.dimen.icon_padding_next_question_button)
                                    .toInt()
                        }
                    }
                    binding.btRewind.apply {
                        if (position > 0) {
                            isActivated = true
                            isClickable = true
                            backgroundTintList = MaterialColors
                                .getColorStateList(
                                    this@QuizGameActivity,
                                    com.google.android.material.R.attr.colorPrimary,
                                    ContextCompat.getColorStateList(
                                        this@QuizGameActivity, R.color.neutral700
                                    )!!
                                )
                            iconTint = MaterialColors
                                .getColorStateList(
                                    this@QuizGameActivity,
                                    com.google.android.material.R.attr.colorSurfaceContainerLowest,
                                    ContextCompat.getColorStateList(
                                        this@QuizGameActivity, R.color.neutral50
                                    )!!
                                )
                            setTextColor(
                                MaterialColors
                                    .getColorStateList(
                                        this@QuizGameActivity,
                                        com.google.android.material.R.attr.colorSurfaceContainerLowest,
                                        ContextCompat.getColorStateList(
                                            this@QuizGameActivity, R.color.neutral50
                                        )!!
                                    )
                            )
                        } else {
                            isActivated = false
                            isClickable = false
                            backgroundTintList = MaterialColors
                                .getColorStateList(
                                    this@QuizGameActivity,
                                    com.google.android.material.R.attr.colorSurfaceContainerLowest,
                                    ContextCompat.getColorStateList(
                                        this@QuizGameActivity, R.color.neutral50
                                    )!!
                                )
                            iconTint = MaterialColors
                                .getColorStateList(
                                    this@QuizGameActivity,
                                    com.google.android.material.R.attr.colorPrimary,
                                    ContextCompat.getColorStateList(
                                        this@QuizGameActivity, R.color.neutral700
                                    )!!
                                )

                            setTextColor(
                                MaterialColors
                                    .getColorStateList(
                                        this@QuizGameActivity,
                                        com.google.android.material.R.attr.colorPrimary,
                                        ContextCompat.getColorStateList(
                                            this@QuizGameActivity, R.color.neutral700
                                        )!!
                                    )
                            )
                        }
                    }

                    val actualCard = viewModel.getCardByPosition(position)
                    if (actualCard.cardType == SINGLE_ANSWER_CARD) {
                        when {
                            actualCard.attemptTime > 0 && actualCard.isCorrectlyAnswered -> {
                                areNextAndBackButtonsVisible(true)
                                areKnownAndKnownNotButtonsVisible(false)
                            }

                            actualCard.attemptTime > 0 && !actualCard.isCorrectlyAnswered -> {
                                areNextAndBackButtonsVisible(true)
                                specifyKnownAndKnownNotActions()
                                areKnownAndKnownNotButtonsVisible(true)
                            }

                            else -> {
                                areNextAndBackButtonsVisible(false)
                                areKnownAndKnownNotButtonsVisible(false)
                            }
                        }
                    } else {
                        if (actualCard.isCorrectlyAnswered) {
                            areNextAndBackButtonsVisible(true)
                            areKnownAndKnownNotButtonsVisible(false)
                        } else {
                            areNextAndBackButtonsVisible(false)
                            areKnownAndKnownNotButtonsVisible(false)
                        }
                    }


                }
            })
        }
        viewModel.startTimer()
    }

    private fun displayProgression(data: List<QuizGameCardModel>, recyclerView: RecyclerView) {
        quizGameProgressBarAdapter = QuizGameProgressBarAdapter(
            cardList = data,
            context = this,
            recyclerView
        )
        binding.rvMiniGameProgression.apply {
            adapter = quizGameProgressBarAdapter
            layoutManager = LinearLayoutManager(
                this@QuizGameActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun specifyKnownAndKnownNotActions() {
        binding.btKnown.setOnClickListener {
            if (binding.vpCardHolder.currentItem < viewModel.getQuizGameCardsSum() - 1) {
                viewModel.setCardAsActualOrPassedByPosition(binding.vpCardHolder.currentItem.plus(1))
                quizGameProgressBarAdapter.notifyDataSetChanged()
            }
            viewModel.updateSingleAnsweredCardOnKnownOrKnownNot( true, binding.vpCardHolder.currentItem )
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview()
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
            if (tts?.isSpeaking == true) {
                stopReadingAllText()
            }
        }
        binding.btKnownNot.setOnClickListener {
            if (binding.vpCardHolder.currentItem < viewModel.getQuizGameCardsSum() - 1) {
                viewModel.setCardAsActualOrPassedByPosition(binding.vpCardHolder.currentItem.plus(1))
                quizGameProgressBarAdapter.notifyDataSetChanged()
            }
            viewModel.updateSingleAnsweredCardOnKnownOrKnownNot( false, binding.vpCardHolder.currentItem)
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview()
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
            if (tts?.isSpeaking == true) {
                stopReadingAllText()
            }
        }
    }

    private fun specifyNextAndBackActions() {
        if (binding.vpCardHolder.currentItem > 0) {
            isRewindButtonActive(true)
            binding.btRewind.setOnClickListener {
                viewModel.setCardAsNotActualOrNotPassedByPosition(binding.vpCardHolder.currentItem)
                quizGameProgressBarAdapter.notifyDataSetChanged()
                viewModel.initCardFlipCount(binding.vpCardHolder.currentItem)
                fetchJob1?.cancel()
                fetchJob1 = lifecycleScope.launch {
                    delay(TIME_BEFORE_HIDING_ACTIONS)
                    binding.vpCardHolder.setCurrentItem(
                        binding.vpCardHolder.currentItem.minus(1),
                        true
                    )
                }
                if (tts?.isSpeaking == true) {
                    stopReadingAllText()
                }
            }
        } else {
            isRewindButtonActive(false)
        }
        binding.btNext.setOnClickListener {

            if (binding.vpCardHolder.currentItem < viewModel.getQuizGameCardsSum() - 1) {
                viewModel.setCardAsActualOrPassedByPosition(binding.vpCardHolder.currentItem.plus(1))
                quizGameProgressBarAdapter.notifyDataSetChanged()
            }
            viewModel.initCardFlipCount(binding.vpCardHolder.currentItem)
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview()
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
            if (tts?.isSpeaking == true) {
                stopReadingAllText()
            }
        }
    }

    private fun optionsState(
        userResponseModel: QuizGameCardDefinitionModel,
        card: QuizGameCardModel
    ) {
        lifecycleScope.launch {
            delay(TIME_BEFORE_SHOWING_ACTIONS)
            when (userResponseModel.cardType) {
                SINGLE_ANSWER_CARD -> {
                    specifyNextAndBackActions()
                        if (!card.isCorrectlyAnswered) {
                            specifyKnownAndKnownNotActions()
                            areKnownAndKnownNotButtonsVisible(true)
                        } else {
                            areKnownAndKnownNotButtonsVisible(false)
                        }
                    areNextAndBackButtonsVisible(true)
                }

                else -> {
                    specifyNextAndBackActions()
                    areNextAndBackButtonsVisible(true)
                    areKnownAndKnownNotButtonsVisible(false)
                }
            }
        }
    }

    private fun displayReview(
    ) {
        viewModel.pauseTimer()
        areKnownAndKnownNotButtonsVisible(false)
        areNextAndBackButtonsVisible(false)
        binding.vpCardHolder.visibility = View.GONE
        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
        binding.gameReviewContainerMQ.visibility = View.VISIBLE
        binding.gameReviewLayoutMQ.apply {
            tvTotalCardsSumScoreLayout.text = viewModel.getRevisedCardsCount().toString()
            tvAccuracyScoreLayout.text = getString(R.string.text_accuracy_mini_game_review, viewModel.getUserAnswerAccuracy())
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.timer.collect { t ->
                        tvTimeScoreLayout.text = viewModel.formatTime(t)
                    }
                }
            }

            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getUserAnswerAccuracyFraction(),
                ContextCompat.getColor(this@QuizGameActivity, R.color.red400),
                ContextCompat.getColor(this@QuizGameActivity, R.color.green400),
            ) as Int

            val textColorKnownCards = ArgbEvaluator().evaluate(
                viewModel.getUserAnswerAccuracyFraction(),
                ContextCompat.getColor(this@QuizGameActivity, R.color.red50),
                ContextCompat.getColor(this@QuizGameActivity, R.color.green50)
            ) as Int

            tvAccuracyScoreLayout.setTextColor(textColorKnownCards)
            tvAccuracyCardsScoreLayout.setTextColor(textColorKnownCards)

            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@QuizGameActivity, MainActivity::class.java))
                finish()
            }

//            btRestartQuizWithPreviousCardsScoreLayout.setOnClickListener {
//                restartQuiz()
//            }
//
//            btRestartQuizWithAllCardsScoreLayout.setOnClickListener {
//                completelyRestartQuiz()
//            }

//            if (missedCardsSum == 0) {
//                btReviseMissedCardScoreLayout.isActivated = false
//                btReviseMissedCardScoreLayout.isVisible = false
//            } else {
//                btReviseMissedCardScoreLayout.isActivated = true
//                btReviseMissedCardScoreLayout.isVisible = true
//                btReviseMissedCardScoreLayout.setOnClickListener {
//                    viewModel.updateActualCardsWithMissedCards()
//                    quizJob?.cancel()
//                    quizJob = lifecycleScope.launch {
//                        viewModel.getQuizGameCards()
//                        viewModel.externalQuizGameCards.collect { state ->
//                            when (state) {
//                                is UiState.Error -> {
//                                    onNoCardToRevise()
//                                }
//
//                                is UiState.Loading -> {}
//                                is UiState.Success -> {
//                                    binding.gameReviewContainerMQ.visibility = View.GONE
//                                    binding.vpCardHolder.visibility = View.VISIBLE
//                                    binding.vpCardHolder.setCurrentItem(0, true)
//                                    viewModel.resetLocalQuizGameCardsState()
//                                    launchTestQuizGame(state.data)
//                                }
//                            }
//                        }
//                    }
//                }
//            }

            if (viewModel.cardLeft() <= 0) {
                btContinueQuizScoreLayout.visibility = View.GONE
                tvLeftCardsScoreLayout.text = getString(R.string.text_all_cards_learned)
            } else {
                tvLeftCardsScoreLayout.text = getString(R.string.text_cards_left_in_deck, viewModel.cardLeft())
                btContinueQuizScoreLayout.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        viewModel.updateActualCards(getCardCount())
                        quizJob?.cancel()
                        quizJob = lifecycleScope.launch {
                            viewModel.getQuizGameCards()
                            viewModel.externalQuizGameCards.collect { state ->
                                when (state) {
                                    is UiState.Error -> {
                                        onNoCardToRevise()
                                    }
                                    is UiState.Loading -> {}
                                    is UiState.Success -> {
                                        restartQuiz()
                                        launchTestQuizGame(state.data)
                                        displayProgression(state.data, binding.rvMiniGameProgression)
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

    }

    private fun startTest(
        cardList: MutableList<ExternalCardWithContentAndDefinitions>,
        deck: ExternalDeck
    ) {
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateActualCards(getCardCount())
        binding.vpCardHolder.setCurrentItem(0, true)
    }

    private fun restartQuiz() {
        viewModel.initMissedCards()
        viewModel.initQuizGame()
        viewModel.stopTimer()
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        binding.vpCardHolder.setCurrentItem(0, true)
        areNextAndBackButtonsVisible(false)
        areKnownAndKnownNotButtonsVisible(false)
    }

    private fun completelyRestartQuiz() {
        viewModel.onRestartQuiz()
        viewModel.updateActualCards(getCardCount())
        quizJob?.cancel()
        quizJob = lifecycleScope.launch {
            viewModel.getQuizGameCards()
            viewModel.externalQuizGameCards.collect { state ->
                when (state) {
                    is UiState.Error -> {
                        onNoCardToRevise()
                    }

                    is UiState.Loading -> {

                    }

                    is UiState.Success -> {
                        restartQuiz()
                        launchTestQuizGame(state.data)
                        displayProgression(state.data, binding.rvMiniGameProgression)
                    }
                }
            }
        }
    }

    private fun isRewindButtonActive(isActive: Boolean) {
        binding.btRewind.isClickable = isActive
        binding.btRewind.isActivated = isActive
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
                completelyRestartQuiz()
            }
        }
    }

    private fun unableShowUnKnownCardOnly() {
        miniGamePrefEditor?.apply {
            putBoolean(FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY, false)
            apply()
        }
    }

    private fun areNextAndBackButtonsVisible(areVisible: Boolean) {
        binding.btRewind.isVisible = areVisible
        binding.btNext.isVisible = areVisible
    }

    private fun areKnownAndKnownNotButtonsVisible(areVisible: Boolean) {
        binding.btKnown.isVisible = areVisible
        binding.btKnownNot.isVisible = areVisible
        binding.tvActionQuestion.isVisible = areVisible
    }

    private fun readText(
        text: List<TextWithLanguageModel>,
        view: List<View>,
    ) {

        var position = 0
        val textSum = text.size
        val onStopColor = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnSurface,
            Color.BLACK
        )
        val onReadColor =
            MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, Color.GRAY)
        val params = Bundle()

        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onReading(position, view, onReadColor)
            }

            override fun onDone(utteranceId: String?) {
                onReadingStop(position, view, onStopColor)
                position += 1
                if (position < textSum) {
                    onSpeak(params, text, position, this)
                } else {
                    position = 0
                    return
                }
            }

            override fun onError(utteranceId: String?) {
                Toast.makeText(
                    this@QuizGameActivity,
                    getString(R.string.error_read),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        onSpeak(params, text, position, speechListener)

    }

    private fun stopReading(
        views: List<View>
    ) {
        tts?.stop()
        views.forEach { v ->
            (v as TextView).setTextColor(
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
        val tvContent: TextView = findViewById(R.id.tv_content)
        val tvDefinition: TextView = findViewById(R.id.tv_definition)
        val alternatives = listOf(
            findViewById<MaterialButton>(R.id.bt_alternative1),
            findViewById<MaterialButton>(R.id.bt_alternative2),
            findViewById<MaterialButton>(R.id.bt_alternative3),
            findViewById<MaterialButton>(R.id.bt_alternative4),
            findViewById<MaterialButton>(R.id.bt_alternative5),
            findViewById<MaterialButton>(R.id.bt_alternative6),
            findViewById<MaterialButton>(R.id.bt_alternative7),
            findViewById<MaterialButton>(R.id.bt_alternative8),
            findViewById<MaterialButton>(R.id.bt_alternative9),
            findViewById<MaterialButton>(R.id.bt_alternative10),
        )
        tvContent.setTextColor(
            MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorOnSurface,
                Color.BLACK
            )
        )
        tvDefinition.setTextColor(
            MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorOnSurface,
                Color.BLACK
            )
        )
        alternatives.forEach {
            it.setTextColor(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnSurface,
                    Color.BLACK
                )
            )
        }
    }

    private fun onReading(
        position: Int,
        view: List<View>,
        onReadColor: Int
    ) {
        if (position == 0) {
            (view[position] as TextView).setTextColor(onReadColor)
        } else {
            (view[position] as MaterialButton).setTextColor(onReadColor)
        }
    }

    private fun onReadingStop(
        position: Int,
        view: List<View>,
        onReadColor: Int
    ) {
        if (position == 0) {
            (view[position] as TextView).setTextColor(onReadColor)
        } else {
            (view[position] as MaterialButton).setTextColor(onReadColor)
        }
    }

    private fun onSpeak(
        params: Bundle,
        text: List<TextWithLanguageModel>,
        position: Int,
        speechListener: UtteranceProgressListener
    ) {
        val actualText = text[position]
        val languageUtil = LanguageUtil()
        if (actualText.language.isNullOrBlank()) {
            languageUtil.detectLanguage(
                text = actualText.text,
                onError = {showSnackBar(R.string.error_message_error_while_detecting_language)},
                onLanguageUnIdentified = {showSnackBar(R.string.error_message_can_not_identify_language)},
                onLanguageNotSupported = {showSnackBar(R.string.error_message_language_not_supported)},
                onSuccess = { detectedLanguage ->
                    when (actualText.textType) {
                        CONTENT -> viewModel.updateCardContentLanguage(actualText.cardId, detectedLanguage)
                        DEFINITION -> viewModel.updateCardDefinitionLanguage(actualText.cardId, detectedLanguage)
                    }
                    speak(actualText.text, detectedLanguage, params, speechListener)
                }
            )
        } else {
            speak(actualText.text, actualText.language, params, speechListener)
        }
    }

    private fun speak(
        text: String,
        language: String,
        params: Bundle,
        speechListener: UtteranceProgressListener
    ) {
        tts?.language = Locale.forLanguageTag(
            LanguageUtil().getLanguageCodeForTextToSpeech(language)
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts?.speak(text, TextToSpeech.QUEUE_ADD, params, "UniqueID")
        tts?.setOnUtteranceProgressListener(speechListener)
    }

    private fun showSnackBar(
        @StringRes messageRes: Int
    ) {
        Snackbar.make(
            binding.clQuizGameRoot,
            getString(messageRes),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun getCardCount() = miniGamePref?.getString(CARD_COUNT, "10")?.toInt() ?: 10

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