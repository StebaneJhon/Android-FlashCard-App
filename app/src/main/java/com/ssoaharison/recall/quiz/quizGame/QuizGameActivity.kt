package com.ssoaharison.recall.quiz.quizGame

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
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
import com.ssoaharison.recall.util.CardType.SINGLE_ANSWER_CARD
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_COUNT
import com.ssoaharison.recall.util.TextWithLanguageModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class QuizGameActivity :
    AppCompatActivity(),
    MiniGameSettingsSheet.SettingsApplication,
    TextToSpeech.OnInitListener {

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

    private var deckWithCards: ImmutableDeckWithCards? = null
    private lateinit var quizGameAdapter: QuizGameAdapter

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
        miniGamePrefEditor = miniGamePref?.edit()
        appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }
        binding = ActivityTestQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards?.toMutableList()
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                viewModel.initCardList(cardList)
                viewModel.initOriginalCardList(cardList)
                startTest(cardList, deck)
            }
        }

        binding.vpCardHolder.isUserInputEnabled = false

        binding.topAppBar.apply {
            title = getString(R.string.start_quiz_button_text)
            subtitle = getString(R.string.title_flash_card_game, viewModel.deck?.deckName)
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
            viewModel.getDeckColorCode(),
            appTheme ?: "WHITE THEM",
            viewModel.deck!!,
            { userAnswer ->
                viewModel.submitUserAnswer(userAnswer)
                val actualCard = viewModel.getCardByPosition(binding.vpCardHolder.currentItem)
                if (userAnswer.cardType != SINGLE_ANSWER_CARD && actualCard.attemptTime <= 1) {
                    viewModel.updateMultipleAnswerAndChoiceCardOnAnswered(userAnswer)
                }
                quizGameAdapter.notifyDataSetChanged()
                if (viewModel.isAllAnswerSelected(userAnswer)) {
                    optionsState(userAnswer, actualCard)
                }
//                viewModel.increaseAttemptTime()
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
                                specifyKnownAndKnownNotActions(actualCard)
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
//                            if (actualCard.cardType == SINGLE_ANSWER_CARD) {
//                                specifyKnownAndKnownNotActions(actualCard)
//                                areKnownAndKnownNotButtonsVisible(true)
//                            } else {
//                                areKnownAndKnownNotButtonsVisible(false)
//                            }
                            areKnownAndKnownNotButtonsVisible(false)
                        } else {
                            areNextAndBackButtonsVisible(false)
                            areKnownAndKnownNotButtonsVisible(false)
                        }
                    }


                }
            })
        }
    }

    private fun specifyKnownAndKnownNotActions(card: QuizGameCardModel) {
        binding.btKnown.setOnClickListener {
//            areKnownAndKnownNotButtonsVisible(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
//            areNextAndBackButtonsVisible(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
            viewModel.updateSingleAnsweredCardOnKnownOrKnownNot(card, true, binding.vpCardHolder.currentItem )
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getQuizGameCardsSum(),
                        viewModel.cardLeft()
                    )
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
//            viewModel.initAttemptTime()
        }
        binding.btKnownNot.setOnClickListener {
            viewModel.updateSingleAnsweredCardOnKnownOrKnownNot(card, false, binding.vpCardHolder.currentItem)
//            areKnownAndKnownNotButtonsVisible(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
//            areNextAndBackButtonsVisible(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getQuizGameCardsSum(),
                        viewModel.cardLeft()
                    )
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
//            viewModel.initAttemptTime()
        }
    }

    private fun specifyNextAndBackActions() {
        if (binding.vpCardHolder.currentItem > 0) {
            isRewindButtonActive(true)
            binding.btRewind.setOnClickListener {
//                areNextAndBackButtonsVisible(true)
                viewModel.initCardFlipCount(binding.vpCardHolder.currentItem)
                fetchJob1?.cancel()
                fetchJob1 = lifecycleScope.launch {
                    delay(TIME_BEFORE_HIDING_ACTIONS)
//                    binding.vpCardHolder.apply {
//                        beginFakeDrag()
//                        fakeDragBy(10f)
//                        endFakeDrag()
//                    }
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.minus(1),
                        true
                    )
                }
            }
//            viewModel.initAttemptTime()
        } else {
            isRewindButtonActive(false)
        }
        binding.btNext.setOnClickListener {
//            areNextAndBackButtonsVisible(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
//            areKnownAndKnownNotButtonsVisible(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
            viewModel.initCardFlipCount(binding.vpCardHolder.currentItem)
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getQuizGameCardsSum(),
                        viewModel.cardLeft()
                    )
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
//            viewModel.initAttemptTime()
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
                            specifyKnownAndKnownNotActions(card)
                            areKnownAndKnownNotButtonsVisible(true)
                        } else {
                            areKnownAndKnownNotButtonsVisible(false)
                        }
                    areNextAndBackButtonsVisible(true)
//                    specifyKnownAndKnownNotActions(card)
//                    areKnownAndKnownNotButtonsVisible(true)
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
        knownCardsSum: Int,
        missedCardsSum: Int,
        totalCardsSum: Int,
        cardsLeft: Int,
    ) {
//        viewModel.initAttemptTime()
        areKnownAndKnownNotButtonsVisible(false)
        areNextAndBackButtonsVisible(false)
        binding.vpCardHolder.visibility = View.GONE
        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
        binding.gameReviewContainerMQ.visibility = View.VISIBLE
        binding.gameReviewLayoutMQ.apply {
            tvTotalCardsSumScoreLayout.text = totalCardsSum.toString()
            tvMissedCardSumScoreLayout.text = missedCardsSum.toString()
            tvKnownCardsSumScoreLayout.text = knownCardsSum.toString()

            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
                knownCardsSum.toFloat() / totalCardsSum,
                ContextCompat.getColor(this@QuizGameActivity, R.color.green50),
                ContextCompat.getColor(this@QuizGameActivity, R.color.green400),
            ) as Int

            val missedCardsBackgroundColor = ArgbEvaluator().evaluate(
                missedCardsSum.toFloat() / totalCardsSum,
                ContextCompat.getColor(this@QuizGameActivity, R.color.red50),
                ContextCompat.getColor(this@QuizGameActivity, R.color.red400),
            ) as Int

            val textColorKnownCards =
                if (totalCardsSum / 2 < viewModel.getKnownCardSum())
                    ContextCompat.getColor(this@QuizGameActivity, R.color.green50)
                else ContextCompat.getColor(this@QuizGameActivity, R.color.green400)

            val textColorMissedCards =
                if (totalCardsSum / 2 < viewModel.getMissedCardSum())
                    ContextCompat.getColor(this@QuizGameActivity, R.color.red50)
                else ContextCompat.getColor(this@QuizGameActivity, R.color.red400)

            tvMissedCardSumScoreLayout.setTextColor(textColorMissedCards)
            tvMissedCardScoreLayout.setTextColor(textColorMissedCards)
            tvKnownCardsSumScoreLayout.setTextColor(textColorKnownCards)
            tvKnownCardsScoreLayout.setTextColor(textColorKnownCards)

            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
            cvContainerMissedCards.background.setTint(missedCardsBackgroundColor)

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@QuizGameActivity, MainActivity::class.java))
                finish()
            }

            btRestartQuizWithPreviousCardsScoreLayout.setOnClickListener {
                restartQuiz()
            }

            btRestartQuizWithAllCardsScoreLayout.setOnClickListener {
                completelyRestartQuiz()
            }

            if (missedCardsSum == 0) {
                btReviseMissedCardScoreLayout.isActivated = false
                btReviseMissedCardScoreLayout.isVisible = false
            } else {
                btReviseMissedCardScoreLayout.isActivated = true
                btReviseMissedCardScoreLayout.isVisible = true
                btReviseMissedCardScoreLayout.setOnClickListener {
                    viewModel.updateActualCardsWithMissedCards()
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
                                    binding.gameReviewContainerMQ.visibility = View.GONE
                                    binding.vpCardHolder.visibility = View.VISIBLE
                                    binding.vpCardHolder.setCurrentItem(0, true)
                                    viewModel.resetLocalQuizGameCardsState()
                                    launchTestQuizGame(state.data)
                                }
                            }
                        }
                    }
                }
            }

            if (cardsLeft <= 0) {
                btContinueQuizScoreLayout.visibility = View.GONE
            } else {
                btContinueQuizScoreLayout.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.cards_left_match_quiz_score, "$cardsLeft")
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
        cardList: MutableList<ImmutableCard?>,
        deck: ImmutableDeck
    ) {
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateActualCards(getCardCount())
//        viewModel.initAttemptTime()
        binding.vpCardHolder.setCurrentItem(0, true)
    }

    private fun restartQuiz() {
        viewModel.initMissedCards()
        viewModel.initQuizGame()
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
                    speak(params, text, position, this)
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

        speak(params, text, position, speechListener)

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

    private fun getCardCount() = miniGamePref?.getString(CARD_COUNT, "10")?.toInt() ?: 10

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
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

}