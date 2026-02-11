package com.soaharisonstebane.mneme.quiz.multichoiceQuizGame

import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.animation.Animation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.backend.models.ImmutableDeckWithCards
import com.soaharisonstebane.mneme.databinding.ActivityMultichoiceQuizGameBinding
import com.soaharisonstebane.mneme.settings.MiniGameSettingsSheet
import kotlinx.coroutines.Job

class MultiChoiceQuizGameActivity :
    AppCompatActivity()
//    MiniGameSettingsSheet.SettingsApplication,
//    TextToSpeech.OnInitListener
{

    private lateinit var binding: ActivityMultichoiceQuizGameBinding
    private val viewModel: MultiChoiceQuizGameViewModel by viewModels {
        MultiChoiceQuizGameViewModelFactory((application as FlashCardApplication).repository)
    }

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var deckWithCards: ImmutableDeckWithCards? = null

    private var animFadeIn: Animation? = null
    private var animFadeOut: Animation? = null
    private var modalBottomSheet: MiniGameSettingsSheet? = null
    private var miniGamePref: SharedPreferences? = null
    private var miniGamePrefEditor: SharedPreferences.Editor? = null

    private var appTheme: String? = null

    private var tts: TextToSpeech? = null
    private var multiChoiceQuizJob: Job? = null
    private lateinit var multiChoiceGameAdapter: MultiChoiceQuizGameAdapter
    private lateinit var multiChoiceGameProgressionAdapter: MultiChoiceQuizProgressionAdapter

    companion object {
        private const val TAG = "MultiChoiceQuizGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
//        miniGamePref = getSharedPreferences(
//            FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF,
//            Context.MODE_PRIVATE
//        )
//        miniGamePrefEditor = miniGamePref?.edit()
//        editor = sharedPref?.edit()
//        val themePicker = ThemePicker()
//        appTheme = sharedPref?.getString("themName", "WHITE THEM")
//        val themRef = themePicker.selectTheme(appTheme)
//
//        deckWithCards = intent?.parcelable(FlashCardGameActivity.DECK_ID_KEY)
//
//        val deckColorCode = deckWithCards?.deck?.deckColorCode
//
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
//
//        binding = ActivityMultichoiceQuizGameBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        tts = TextToSpeech(this, this)
//
//        animFadeIn = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
//        animFadeOut = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)
//
//        deckWithCards = intent?.parcelable(DECK_ID_KEY)
//        deckWithCards?.let {
//            val cardList = it.cards?.toMutableList()
//            val deck = it.deck
//            if (!cardList.isNullOrEmpty() && deck != null) {
//                viewModel.initOriginalCardList(cardList)
//                startMultiChoiceQuizGame(cardList, deck)
//            } else {
//                onNoCardToRevise()
//            }
//        }
//
//        binding.vpCardHolder.isUserInputEnabled = false
//
//        binding.topAppBar.apply {
//            title = getString(R.string.multiple_choice_quiz_button_text)
//            subtitle = viewModel.deck.deckName
//            setNavigationOnClickListener { finish() }
//        }
//
//        applySettings()
//        completelyRestartMultiChoiceQuiz()
//
//        modalBottomSheet = MiniGameSettingsSheet()
//
//        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
//            if (menuItem.itemId == R.id.mn_bt_settings) {
//                modalBottomSheet?.show(supportFragmentManager, MiniGameSettingsSheet.TAG)
//                true
//            } else {
//                false
//            }
//        }
    }

//    override fun onSettingsApplied() {
//        applySettings()
//        completelyRestartMultiChoiceQuiz()
//    }
//
//    private fun getCardOrientation() = miniGamePref?.getString(
//        CHECKED_CARD_ORIENTATION,
//        CARD_ORIENTATION_FRONT_AND_BACK
//    ) ?: CARD_ORIENTATION_FRONT_AND_BACK
//
//    private fun applySettings() {
//
//        val filter = miniGamePref?.getString(
//            FlashCardMiniGameRef.CHECKED_FILTER,
//            FlashCardMiniGameRef.FILTER_RANDOM
//        )
//
//        val unKnownCardFirst = miniGamePref?.getBoolean(
//            FlashCardMiniGameRef.IS_UNKNOWN_CARD_FIRST,
//            true
//        )
//        val unKnownCardOnly = miniGamePref?.getBoolean(
//            FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY,
//            false
//        )
//
//        if (unKnownCardOnly == true) {
//            viewModel.cardToReviseOnly()
//        } else {
//            viewModel.restoreCardList()
//        }
//
//        when (filter) {
//            FlashCardMiniGameRef.FILTER_RANDOM -> {
//                viewModel.shuffleCards()
//            }
//
//            FlashCardMiniGameRef.FILTER_BY_LEVEL -> {
//                viewModel.sortCardsByLevel()
//            }
//
//            FlashCardMiniGameRef.FILTER_CREATION_DATE -> {
//                viewModel.sortByCreationDate()
//            }
//        }
//
//        if (unKnownCardFirst == true) {
//            viewModel.sortCardsByLevel()
//        }
//    }
//
//    private fun onNoCardToRevise() {
//        binding.lyOnNoMoreCardsErrorContainer.isVisible = true
//        binding.vpCardHolder.visibility = View.GONE
//        binding.gameReviewContainerMQ.visibility = View.GONE
//        binding.lyNoCardError.apply {
//            btBackToDeck.setOnClickListener {
//                finish()
//            }
//            btUnableUnknownCardOnly.setOnClickListener {
//                unableShowUnKnownCardOnly()
//                applySettings()
//                completelyRestartMultiChoiceQuiz()
//            }
//        }
//    }
//
//    private fun unableShowUnKnownCardOnly() {
//        miniGamePrefEditor?.apply {
//            putBoolean(FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY, false)
//            apply()
//        }
//    }
//
//    private fun launchMultiChoiceQuizGame(data: List<MultiChoiceGameCardModel>) {
//        multiChoiceGameAdapter =
//            MultiChoiceQuizGameAdapter(
//                this,
//                data,
//                appTheme ?: "WHITE THEM",
//
//                {
//                    if (viewModel.isUserChoiceCorrect(
//                            it,
//                            getCardOrientation(),
//                            binding.vpCardHolder.currentItem
//                        )
//                    ) {
//                        areOptionsEnabled(true)
//                        specifyOptions(data.size)
//                    } else if (viewModel.getAttemptTime() <= 1) {
//                        multiChoiceGameProgressionAdapter.notifyDataSetChanged()
//                    }
//                    multiChoiceGameAdapter.notifyDataSetChanged()
//                }) {
//                if (tts?.isSpeaking == true) {
//                    stopReading(it.views)
//                } else {
//                    val cardOrientation = getCardOrientation()
//                    if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
//                        readText(
//                            it.text,
//                            it.views,
//                        )
//                    } else {
//                        readText(
//                            it.text,
//                            it.views,
//                        )
//                    }
//                }
//
//            }
//        binding.vpCardHolder.adapter = multiChoiceGameAdapter
//        binding.vpCardHolder.registerOnPageChangeCallback(object :
//            ViewPager2.OnPageChangeCallback() {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
//                binding.btNextQuestion.apply {
//                    if (position == data.size.minus(1)) {
//                        text = getString(R.string.text_finish)
//                        iconPadding =
//                            resources.getDimension(R.dimen.icon_padding_next_question_button)
//                                .toInt()
//                    } else {
//                        text = null
//                        iconPadding =
//                            resources.getDimension(R.dimen.icon_padding_next_question_button)
//                                .toInt()
//                    }
//                }
//                binding.btPreviousQuestion.apply {
//                    if (position > 0) {
//                        isActivated = true
//                        isClickable = true
//                        backgroundTintList = MaterialColors
//                            .getColorStateList(
//                                this@MultiChoiceQuizGameActivity,
//                                com.google.android.material.R.attr.colorPrimary,
//                                ContextCompat.getColorStateList(
//                                    this@MultiChoiceQuizGameActivity, R.color.neutral700
//                                )!!
//                            )
//                        iconTint = MaterialColors
//                            .getColorStateList(
//                                this@MultiChoiceQuizGameActivity,
//                                com.google.android.material.R.attr.colorSurfaceContainerLowest,
//                                ContextCompat.getColorStateList(
//                                    this@MultiChoiceQuizGameActivity, R.color.neutral50
//                                )!!
//                            )
//                        setTextColor(
//                            MaterialColors
//                                .getColorStateList(
//                                    this@MultiChoiceQuizGameActivity,
//                                    com.google.android.material.R.attr.colorSurfaceContainerLowest,
//                                    ContextCompat.getColorStateList(
//                                        this@MultiChoiceQuizGameActivity, R.color.neutral50
//                                    )!!
//                                )
//                        )
//                    } else {
//                        isActivated = false
//                        isClickable = false
//                        backgroundTintList = MaterialColors
//                            .getColorStateList(
//                                this@MultiChoiceQuizGameActivity,
//                                com.google.android.material.R.attr.colorSurfaceContainerLowest,
//                                ContextCompat.getColorStateList(
//                                    this@MultiChoiceQuizGameActivity, R.color.neutral50
//                                )!!
//                            )
//                        iconTint = MaterialColors
//                            .getColorStateList(
//                                this@MultiChoiceQuizGameActivity,
//                                com.google.android.material.R.attr.colorPrimary,
//                                ContextCompat.getColorStateList(
//                                    this@MultiChoiceQuizGameActivity, R.color.neutral700
//                                )!!
//                            )
//
//                        setTextColor(
//                            MaterialColors
//                                .getColorStateList(
//                                    this@MultiChoiceQuizGameActivity,
//                                    com.google.android.material.R.attr.colorPrimary,
//                                    ContextCompat.getColorStateList(
//                                        this@MultiChoiceQuizGameActivity, R.color.neutral700
//                                    )!!
//                                )
//                        )
//                    }
//                }
//            }
//        })
//        viewModel.startTimer()
//    }
//
//    private fun displayProgression(
//        data: List<MultiChoiceGameCardModel>,
//        recyclerView: RecyclerView
//    ) {
//        multiChoiceGameProgressionAdapter = MultiChoiceQuizProgressionAdapter(
//            cardList = data,
//            appTheme = appTheme ?: "WHITE THEME",
//            context = this,
//            recyclerView
//        )
//        binding.rvMiniGameProgression.apply {
//            adapter = multiChoiceGameProgressionAdapter
//            layoutManager = LinearLayoutManager(
//                this@MultiChoiceQuizGameActivity,
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//        }
//    }
//
//    private fun areOptionsEnabled(enabled: Boolean) {
//        binding.lyContainerOptions.isVisible = enabled
//        binding.btNextQuestion.isClickable = enabled
//        binding.btPreviousQuestion.isClickable = enabled
//    }
//
//    private fun specifyOptions(cardCount: Int) {
//        binding.btNextQuestion.setOnClickListener {
//            areOptionsEnabled(viewModel.isNextCardAnsweredCorrectly())
//            if (viewModel.increaseCurrentCardPosition(cardCount)) {
//                viewModel.setCardAsActualOrPassedByPosition(viewModel.getCurrentCardPosition())
//                multiChoiceGameProgressionAdapter.notifyDataSetChanged()
//                binding.vpCardHolder.setCurrentItem(
//                    viewModel.getCurrentCardPosition(),
//                    true
//                )
////                if (viewModel.isNextCardAnsweredCorrectly()) {
////                    viewModel.initAttemptTime()
////                }
//            } else {
//                onQuizComplete()
//            }
//            viewModel.initAttemptTime()
//            tts?.stop()
//        }
//        binding.btPreviousQuestion.setOnClickListener {
//            if (viewModel.getCurrentCardPosition() > 0) {
//                viewModel.setCardAsNotActualOrNotPassedByPosition(viewModel.getCurrentCardPosition())
//                multiChoiceGameProgressionAdapter.notifyDataSetChanged()
//                viewModel.decreaseCurrentCardPosition()
//                binding.vpCardHolder.setCurrentItem(
//                    viewModel.getCurrentCardPosition(),
//                    true
//                )
//                viewModel.initAttemptTime()
//                tts?.stop()
//            }
//        }
//        if (viewModel.getCurrentCardPosition() <= 0) {
//            binding.btPreviousQuestion.isClickable = false
//            binding.btPreviousQuestion.isActivated = false
//        } else {
//            binding.btPreviousQuestion.isClickable = true
//            binding.btPreviousQuestion.isActivated = true
//        }
//    }
//
//    private fun stopReading(
//        views: List<View>,
//    ) {
//        tts?.stop()
//        views.forEach { v ->
//            (v as TextView).setTextColor(
//                MaterialColors.getColor(
//                    this,
//                    com.google.android.material.R.attr.colorOnSurface,
//                    Color.BLACK
//                )
//            )
//        }
//    }
//
//    private fun stopReadingAllText() {
//        tts?.stop()
//        val tvOnCardWord: TextView = findViewById(R.id.tv_on_card_word)
//        val views = listOf(
//            findViewById<MaterialButton>(R.id.bt_alternative1),
//            findViewById<MaterialButton>(R.id.bt_alternative2),
//            findViewById<MaterialButton>(R.id.bt_alternative3),
//            findViewById<MaterialButton>(R.id.bt_alternative4),
//        )
//        tvOnCardWord.setTextColor(
//            MaterialColors.getColor(
//                this,
//                com.google.android.material.R.attr.colorOnSurface,
//                Color.BLACK
//            )
//        )
//        views.forEach {
//            it.setTextColor(
//                MaterialColors.getColor(
//                    this,
//                    com.google.android.material.R.attr.colorOnSurface,
//                    Color.BLACK
//                )
//            )
//        }
//
//    }
//
//    private fun readText(
//        text: List<TextWithLanguageModel>,
//        view: List<View>,
//    ) {
//        var position = 0
//        val textSum = text.size
//        val onReadColor = MaterialColors.getColor(
//            this,
//            com.google.android.material.R.attr.colorSurfaceContainerHighest,
//            Color.GRAY
//        )
//        val onStopColor = MaterialColors.getColor(
//            this,
//            com.google.android.material.R.attr.colorOnSurface,
//            Color.BLACK
//        )
//        val params = Bundle()
//        val speechListener = object : UtteranceProgressListener() {
//            override fun onStart(utteranceId: String?) {
//                onReading(position, view, onReadColor)
//            }
//
//            override fun onDone(utteranceId: String?) {
//                onReadingStop(position, view, onStopColor)
//                position += 1
//                if (position < textSum) {
//                    onSpeak(params, text, position, this)
//                } else {
//                    position = 0
//                    return
//                }
//            }
//
//            override fun onError(utteranceId: String?) {
//                Toast.makeText(
//                    this@MultiChoiceQuizGameActivity,
//                    getString(R.string.error_read),
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//        onSpeak(params, text, position, speechListener)
//    }
//
//    private fun onReading(
//        position: Int,
//        view: List<View>,
//        onReadColor: Int,
//    ) {
//        if (position == 0) {
//            (view[position] as TextView).setTextColor(onReadColor)
//        } else {
//            (view[position] as MaterialButton).setTextColor(onReadColor)
//        }
//    }
//
//    private fun onReadingStop(
//        position: Int,
//        view: List<View>,
//        onReadColor: Int,
//    ) {
//        if (position == 0) {
//            (view[position] as TextView).setTextColor(onReadColor)
//        } else {
//            (view[position] as MaterialButton).setTextColor(onReadColor)
//        }
//    }
//
//    private fun onSpeak(
//        params: Bundle,
//        text: List<TextWithLanguageModel>,
//        position: Int,
//        speechListener: UtteranceProgressListener
//    ) {
//        val actualText = text[position]
//        val languageUtil = LanguageUtil()
//        if (actualText.language.isNullOrBlank()) {
//            languageUtil.detectLanguage(
//                text = actualText.text,
//                onError = { showSnackBar(R.string.error_message_error_while_detecting_language) },
//                onLanguageUnIdentified = { showSnackBar(R.string.error_message_can_not_identify_language) },
//                onLanguageNotSupported = { showSnackBar(R.string.error_message_language_not_supported) },
//                onSuccess = { detectedLanguage ->
//                    when (actualText.textType) {
//                        CONTENT -> viewModel.updateCardContentLanguage(
//                            actualText.cardId,
//                            detectedLanguage
//                        )
//
//                        DEFINITION -> viewModel.updateCardDefinitionLanguage(
//                            actualText.cardId,
//                            detectedLanguage
//                        )
//                    }
//                    speak(actualText.text, detectedLanguage, params, speechListener)
//                }
//            )
//        } else {
//            speak(actualText.text, actualText.language, params, speechListener)
//        }
//    }
//
//    private fun speak(
//        text: String,
//        language: String,
//        params: Bundle,
//        speechListener: UtteranceProgressListener
//    ) {
//        tts?.language = Locale.forLanguageTag(
//            LanguageUtil().getLanguageCodeForTextToSpeech(language)!!
//        )
//        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
//        tts?.speak(text, TextToSpeech.QUEUE_ADD, params, "UniqueID")
//        tts?.setOnUtteranceProgressListener(speechListener)
//    }
//
//    private fun showSnackBar(
//        @StringRes messageRes: Int
//    ) {
//        Snackbar.make(
//            binding.multiChoiceQuizGameMotionLY,
//            getString(messageRes),
//            Snackbar.LENGTH_LONG
//        ).show()
//    }
//
//    private fun onQuizComplete(
//    ) {
//        viewModel.pauseTimer()
//        tts?.stop()
//        binding.gameReviewContainerMQ.visibility = View.VISIBLE
//        binding.vpCardHolder.visibility = View.GONE
//        binding.gameReviewLayoutMQ.apply {
//            tvTotalCardsSumScoreLayout.text = viewModel.getRevisedCardsCount().toString()
//            tvAccuracyScoreLayout.text = getString(
//                R.string.text_accuracy_mini_game_review,
//                viewModel.getUserAnswerAccuracy()
//            )
//            lifecycleScope.launch {
//                repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    viewModel.timer.collect { t ->
//                        tvTimeScoreLayout.text = viewModel.formatTime(t)
//                    }
//                }
//            }
//
//            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
//                viewModel.getUserAnswerAccuracyFraction(),
//                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.red400),
//                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.green400),
//            ) as Int
//
//            val textColorKnownCards = ArgbEvaluator().evaluate(
//                viewModel.getUserAnswerAccuracyFraction(),
//                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.red50),
//                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.green50)
//            ) as Int
//
//            tvAccuracyScoreLayout.setTextColor(textColorKnownCards)
//            tvAccuracyCardsScoreLayout.setTextColor(textColorKnownCards)
//
//            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
//
//            btBackToDeckScoreLayout.setOnClickListener {
//                startActivity(Intent(this@MultiChoiceQuizGameActivity, MainActivity::class.java))
//                finish()
//            }
////            btRestartQuizWithPreviousCardsScoreLayout.setOnClickListener {
////                restartMultiChoiceQuiz()
////            }
////            btRestartQuizWithAllCardsScoreLayout.setOnClickListener {
////                completelyRestartMultiChoiceQuiz()
////            }
////            if (viewModel.getMissedCardSum() == 0) {
////                btReviseMissedCardScoreLayout.apply {
////                    isActivated = false
////                    isVisible = false
////                }
////            } else {
////                btReviseMissedCardScoreLayout.apply {
////                    isActivated = true
////                    isVisible = true
////                    setOnClickListener {
////                        viewModel.updateActualCardsWithMissedCards(getCardOrientation())
////                        multiChoiceQuizJob?.cancel()
////                        multiChoiceQuizJob = lifecycleScope.launch {
////                            viewModel.getMultiChoiceCards()
////                            viewModel.externalMultiChoiceCards.collect { state ->
////                                when (state) {
////                                    is UiState.Error -> {
////                                        onNoCardToRevise()
////                                    }
////
////                                    is UiState.Loading -> {}
////                                    is UiState.Success -> {
////                                        binding.gameReviewContainerMQ.visibility = View.GONE
////                                        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
////                                        binding.vpCardHolder.visibility = View.VISIBLE
////                                        binding.vpCardHolder.setCurrentItem(0, true)
////                                        viewModel.initCurrentCardPosition()
////                                        viewModel.initProgress()
////                                        launchMultiChoiceQuizGame(state.data)
////                                    }
////                                }
////                            }
////                        }
////                    }
////                }
////            }
//
//            if (viewModel.cardLeft() <= 0) {
//                btContinueQuizScoreLayout.visibility = View.GONE
//                tvLeftCardsScoreLayout.text = getString(R.string.text_all_cards_learned)
//            } else {
//                tvLeftCardsScoreLayout.text =
//                    getString(R.string.text_cards_left_in_deck, viewModel.cardLeft())
//                btContinueQuizScoreLayout.apply {
//                    visibility = View.VISIBLE
//                    setOnClickListener {
//                        viewModel.updateActualCards(getCardCount(), getCardOrientation())
//                        multiChoiceQuizJob?.cancel()
//                        multiChoiceQuizJob = lifecycleScope.launch {
//                            viewModel.getMultiChoiceCards()
//                            viewModel.externalMultiChoiceCards.collect { state ->
//                                when (state) {
//                                    is UiState.Error -> {
//                                        onNoCardToRevise()
//                                    }
//
//                                    is UiState.Loading -> {}
//                                    is UiState.Success -> {
//                                        restartMultiChoiceQuiz()
//                                        launchMultiChoiceQuizGame(state.data)
//                                        displayProgression(
//                                            state.data,
//                                            binding.rvMiniGameProgression
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//        }
//    }
//
//    private fun restartMultiChoiceQuiz() {
//        binding.gameReviewContainerMQ.visibility = View.GONE
//        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
//        binding.vpCardHolder.visibility = View.VISIBLE
//        binding.vpCardHolder.setCurrentItem(0, true)
//        viewModel.initTimedFlashCard()
//        viewModel.initAttemptTime()
//    }
//
//
//    private fun startMultiChoiceQuizGame(
//        cardList: MutableList<ImmutableCard?>,
//        deck: ImmutableDeck
//    ) {
//        binding.vpCardHolder.setCurrentItem(0, true)
//        binding.gameReviewContainerMQ.visibility = View.GONE
//        binding.vpCardHolder.visibility = View.VISIBLE
//        viewModel.initCardList(cardList)
//        viewModel.initDeck(deck)
//        viewModel.updateActualCards(getCardCount(), getCardOrientation())
//    }
//
//    private fun completelyRestartMultiChoiceQuiz() {
//        restartMultiChoiceQuiz()
//        viewModel.onRestartQuiz()
//        viewModel.updateActualCards(getCardCount(), getCardOrientation())
//        multiChoiceQuizJob?.cancel()
//        multiChoiceQuizJob = lifecycleScope.launch {
//            viewModel.getMultiChoiceCards()
//            viewModel.externalMultiChoiceCards.collect { state ->
//                when (state) {
//                    is UiState.Loading -> {
//                    }
//
//                    is UiState.Error -> {
//                        onNoCardToRevise()
//                    }
//
//                    is UiState.Success -> {
//                        launchMultiChoiceQuizGame(state.data)
//                        displayProgression(state.data, binding.rvMiniGameProgression)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun getCardCount() = miniGamePref?.getString(CARD_COUNT, "10")?.toInt() ?: 10
//
//    override fun onInit(status: Int) {
//        when (status) {
//            TextToSpeech.SUCCESS -> {
//                tts?.setSpeechRate(1.0f)
//            }
//
//            else -> {
//                Toast.makeText(this, getString(R.string.error_read), Toast.LENGTH_LONG)
//                    .show()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (tts?.isSpeaking == true) {
//            tts?.stop()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (tts?.isSpeaking == true) {
//            stopReadingAllText()
//        }
//    }

}