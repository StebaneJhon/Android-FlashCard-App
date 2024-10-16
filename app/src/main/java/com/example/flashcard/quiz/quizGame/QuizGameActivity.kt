package com.example.flashcard.quiz.quizGame

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.databinding.ActivityTestQuizGameBinding
import com.example.flashcard.mainActivity.MainActivity
import com.example.flashcard.settings.MiniGameSettingsSheet
import com.example.flashcard.util.LanguageUtil
import com.example.flashcard.util.FlashCardMiniGameRef
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
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

    private var deckWithCards: ImmutableDeckWithCards? = null
    private lateinit var quizGameAdapter: QuizGameAdapter

    private lateinit var tts: TextToSpeech

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
        miniGamePref = getSharedPreferences(FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF, Context.MODE_PRIVATE)
        miniGamePrefEditor = miniGamePref?.edit()
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
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
                viewModel.initOriginalCardList(cardList)
                viewModel.initLocalQuizGameCards(cardList.toList())
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getQuizGameCards()
                viewModel.externalQuizGameCards.collect { state ->
                    when (state) {
                        is UiState.Error -> {
                            onNoCardToRevise()
                        }
                        is UiState.Loading -> {

                        }
                        is UiState.Success -> {
                            launchTestQuizGame(state.data)
                        }
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

        viewModel.initQuizGame()
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.getQuizGameCards()
        binding.vpCardHolder.setCurrentItem(0, true)

    }

    private fun launchTestQuizGame(
        data: List<QuizGameCardModel>
    ) {
        quizGameAdapter = QuizGameAdapter(
            this,
            data,
            viewModel.getDeckColorCode(),
            viewModel.deck!!,
            {userAnswer ->
                viewModel.submitUserAnswer(userAnswer)
                quizGameAdapter.notifyDataSetChanged()
                if (viewModel.isAllAnswerSelected(userAnswer)) {
                    optionsState()
                }
                specifyActions(userAnswer)
            },
            {dataToRead ->
                if (tts.isSpeaking) {
                    stopReading(dataToRead.views)
                } else {
                    readText(
                        dataToRead.text,
                        dataToRead.views,
                        dataToRead.language,
                        viewModel.deck?.deckSecondLanguage!!
                    )
                }

            })
        binding.vpCardHolder.adapter = quizGameAdapter
    }

    private fun specifyActions(
        userResponseModel: QuizGameCardDefinitionModel
    ) {
        var fetchJob1: Job? = null
        binding.btKnown.setOnClickListener {
            areOptionsEnabled(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
            viewModel.updateCardOnKnownOrKnownNot (userResponseModel, true)
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getQuizGameCardsSum(),
                        viewModel.getMissedCard()
                    )
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
            restoreAnswerButtons()
        }
        binding.btKnownNot.setOnClickListener {
            viewModel.updateCardOnKnownOrKnownNot (userResponseModel, false)
            areOptionsEnabled(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getQuizGameCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getQuizGameCardsSum(),
                        viewModel.getMissedCard()
                    )
                } else {
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
            restoreAnswerButtons()
        }
        if (binding.vpCardHolder.currentItem > 0) {
            isRewindButtonActive(true)
            binding.btRewind.setOnClickListener {
                areOptionsEnabled(true)
                fetchJob1?.cancel()
                fetchJob1 = lifecycleScope.launch {
                    delay(TIME_BEFORE_HIDING_ACTIONS)
                    binding.vpCardHolder.apply {
                        beginFakeDrag()
                        fakeDragBy(10f)
                        endFakeDrag()
                    }
                }
            }
        } else {
            isRewindButtonActive(false)
        }
    }

    private fun optionsState() {
        lifecycleScope.launch {
            delay(TIME_BEFORE_SHOWING_ACTIONS)
            areOptionsEnabled(true)
        }
    }

    private fun displayReview(
        knownCardsSum: Int,
        missedCardsSum: Int,
        totalCardsSum: Int,
        missedCardsList: List<ImmutableCard?>
        ) {

        binding.vpCardHolder.visibility = View.GONE
        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
        binding.lyContainerOptions.visibility = View.GONE
        binding.gameReviewContainerMQ.visibility = View.VISIBLE
        binding.gameReviewLayoutMQ.apply {
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "TestActivity")
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

            btRestartQuizScoreLayout.setOnClickListener {
                viewModel.initQuizGame()
                val newCards = viewModel.getOriginalCardList()?.toMutableList()
                startTest(
                    newCards!!,
                    viewModel.deck!!
                )
            }

            if (missedCardsSum == 0) {
                btReviseMissedCardScoreLayout.isActivated = false
                btReviseMissedCardScoreLayout.isVisible = false
            } else {
                btReviseMissedCardScoreLayout.isActivated = true
                btReviseMissedCardScoreLayout.isVisible = true
                btReviseMissedCardScoreLayout.setOnClickListener {
                    val newCards = missedCardsList.toMutableList()
                    viewModel.initQuizGame()
                    startTest(newCards, viewModel.deck!!)
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
        viewModel.initDeck(deck)
        viewModel.initLocalQuizGameCards(cardList)
        viewModel.getQuizGameCards()
        binding.vpCardHolder.setCurrentItem(0, true)
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
                //unableShowUnKnownCardOnly()
                //applySettings()
            }
        }
        //isFlashCardGameScreenHidden(true)
    }

    private fun areOptionsEnabled(optionsEnabled: Boolean) {
        binding.lyContainerOptions.isVisible = optionsEnabled
        binding.btKnown.isClickable = optionsEnabled
        binding.btKnownNot.isClickable = optionsEnabled
    }

    private fun readText(
        text: List<String>,
        view: List<View>,
        firstLanguage: String,
        secondLanguage: String,
    ) {

        var position = 0
        val textSum = text.size
        val onStopColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
        val onReadColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, Color.GRAY)
        val params = Bundle()

        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onReading(position, view, onReadColor)
            }

            override fun onDone(utteranceId: String?) {
                onReadingStop(position, view, onStopColor)
                position += 1
                if (position < textSum) {
                    speak(secondLanguage, params, text, position, this)
                } else {
                    position = 0
                    return
                }
            }

            override fun onError(utteranceId: String?) {
                Toast.makeText(this@QuizGameActivity, getString(R.string.error_read), Toast.LENGTH_LONG).show()
            }
        }

        speak(firstLanguage, params, text, position, speechListener)

    }

    private fun stopReading (
        views: List<View>
    ) {
        tts.stop()
        views.forEach { v ->
            (v as TextView).setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK))
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
        language: String,
        params: Bundle,
        text: List<String>,
        position: Int,
        speechListener: UtteranceProgressListener
    ) {
        tts.language = Locale.forLanguageTag(
            LanguageUtil().getLanguageCodeForTextToSpeech(language)!!
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts.speak(text[position], TextToSpeech.QUEUE_ADD, params, "UniqueID")
        tts.setOnUtteranceProgressListener(speechListener)
    }

    private fun restoreAnswerButtons() {

        val buttonsDefaultColorStateList = ContextCompat.getColorStateList(this, R.color.neutral300)
        val buttonsOriginalColorStateList = MaterialColors.getColorStateList(this, com.google.android.material.R.attr.colorSurfaceContainerLowest, buttonsDefaultColorStateList!!)

        val buttonsStrokeDefaultColorStateList = ContextCompat.getColorStateList(this, R.color.neutral500)
        val buttonsStrokeOriginalColorStateList = MaterialColors.getColorStateList(this, com.google.android.material.R.attr.colorSurfaceContainerHigh, buttonsStrokeDefaultColorStateList!!)

        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative1).apply {
            backgroundTintList = buttonsOriginalColorStateList
            strokeColor = buttonsStrokeOriginalColorStateList
        }
        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative2).apply {
            backgroundTintList = buttonsOriginalColorStateList
            strokeColor = buttonsStrokeOriginalColorStateList
        }
        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative3).apply {
            backgroundTintList = buttonsOriginalColorStateList
            strokeColor = buttonsStrokeOriginalColorStateList
        }
        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative4).apply {
            backgroundTintList = buttonsOriginalColorStateList
            strokeColor = buttonsStrokeOriginalColorStateList
        }

    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    override fun onInit(status: Int) {
        when(status) {
            TextToSpeech.SUCCESS -> {
                tts.setSpeechRate(1.0f)
            }
            else -> {
                Toast.makeText(this, getString(R.string.error_read), Toast.LENGTH_LONG).show()
            }
        }
    }

}