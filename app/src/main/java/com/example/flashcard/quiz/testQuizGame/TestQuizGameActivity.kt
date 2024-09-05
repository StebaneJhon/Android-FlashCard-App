package com.example.flashcard.quiz.testQuizGame

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.card.TextToSpeechHelper
import com.example.flashcard.databinding.ActivityTestQuizGameBinding
import com.example.flashcard.mainActivity.MainActivity
import com.example.flashcard.settings.MiniGameSettingsSheet
import com.example.flashcard.util.CardType.FLASHCARD
import com.example.flashcard.util.CardType.ONE_OR_MULTI_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.FlashCardMiniGameRef
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class TestQuizGameActivity : AppCompatActivity(), MiniGameSettingsSheet.SettingsApplication {

    private lateinit var binding: ActivityTestQuizGameBinding
    private val viewModel: TestQuizGameViewModel by viewModels {
        TestQuizGameViewModelFactory((application as FlashCardApplication).repository)
    }
    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var modalBottomSheet: MiniGameSettingsSheet? = null
    private var miniGamePref: SharedPreferences? = null
    private var miniGamePrefEditor: SharedPreferences.Editor? = null

    private lateinit var oneOrMultipleAnswerCardModel: OneOrMultipleAnswerCardModel
    private lateinit var trueOrFalseCardModel: TrueOrFalseCardModel
    private lateinit var flashCardModel: TrueOrFalseCardModel

    private var deckWithCards: ImmutableDeckWithCards? = null
    private lateinit var testQuizGameAdapter: TestQuizGameAdapter

    private lateinit var tts: TextToSpeech

    companion object {
        private const val TAG = "TestQuizGameActivity"
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
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }
        binding = ActivityTestQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards?.toMutableList()
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                viewModel.initOriginalCardList(cardList)
                startTest(cardList, deck)
            } else {
            }
        }

        binding.vpCardHolder.isUserInputEnabled = false

        binding.topAppBar.apply {
            title = getString(R.string.title_flash_card_game, viewModel.deck?.deckName)
            setNavigationOnClickListener { finish() }
        }

        applySettings()

        lifecycleScope.launch {
            viewModel
                .modelCards
                .collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }
                        is UiState.Error -> {
                            onNoCardToRevise()
                        }
                        is UiState.Success -> {
                            launchMultiChoiceQuizGame(state.data)
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

        viewModel.initTest()
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.getCards()
        binding.vpCardHolder.setCurrentItem(0, true)

    }

    private fun launchMultiChoiceQuizGame(
        data: List<ModelCard?>
    ) {
        testQuizGameAdapter = TestQuizGameAdapter(
            this,
            data,
            viewModel.getDeckColorCode(),
            viewModel.deck!!,
            {userResponseModel ->
                when (userResponseModel.modelCard.cardDetails?.cardType) {
                    FLASHCARD -> {
                        onOneAndOneCardClicked(userResponseModel)
                    }
                    TRUE_OR_FALSE_CARD -> {
                        onTrueOrFalseCardAnswered(userResponseModel)
                    }
                    ONE_OR_MULTI_ANSWER_CARD -> {
                        onOneOrMultiAnswerCardAnswered(userResponseModel)
                    }
                    else -> {
                        onOneAndOneCardClicked(userResponseModel)
                    }
                }
            },
            {dataToRead ->
                readText(
                    dataToRead.text,
                    dataToRead.views,
                    dataToRead.originalTextColor,
                    dataToRead.language,
                    viewModel.deck?.deckSecondLanguage!!,)
            })
        binding.vpCardHolder.adapter = testQuizGameAdapter
    }

    private fun onOneAndOneCardClicked(userResponseModel: UserResponseModel) {
        viewModel.onFlipCard(userResponseModel.modelCardPosition)
        specifyActions(userResponseModel)
        optionsState(userResponseModel)
    }

    private fun onTrueOrFalseCardAnswered(userResponseModel: UserResponseModel) {
        val cardModel = TrueOrFalseCardModel(userResponseModel.modelCard, viewModel.getModelCardsNonStream())
        if (cardModel.isAnswerCorrect(userResponseModel.userAnswer!!)) {
            viewModel.onCorrectAnswer(userResponseModel.modelCardPosition)
            giveFeedback(
                userResponseModel.view as MaterialButton,
                true
            )
        } else {
            viewModel.onNotCorrectAnswer(userResponseModel.modelCard.cardDetails)
            giveFeedback(
                userResponseModel.view as MaterialButton,
                false
            )
        }
        if (cardModel.getCorrectAnswerSum() == userResponseModel.modelCard.correctAnswerSum) {
            optionsState(userResponseModel)
        }
        specifyActions(userResponseModel)
    }

    private fun onOneOrMultiAnswerCardAnswered(userResponseModel: UserResponseModel) {
        val cardModel = OneOrMultipleAnswerCardModel(userResponseModel.modelCard, viewModel.getModelCardsNonStream())
        if (cardModel.isAnswerCorrect(userResponseModel.userAnswer!!)) {
            viewModel.onCorrectAnswer(userResponseModel.modelCardPosition)
            giveFeedback(
                userResponseModel.view as MaterialButton,
                true
            )
        } else {
            viewModel.onNotCorrectAnswer(userResponseModel.modelCard.cardDetails)
            giveFeedback(
                userResponseModel.view as MaterialButton,
                false
            )
        }
        if (cardModel.getCorrectAnswerSum() == userResponseModel.modelCard.correctAnswerSum) {
            optionsState(userResponseModel)
        }
        specifyActions(userResponseModel)
    }

    private fun specifyActions(userResponseModel: UserResponseModel) {
        var fetchJob1: Job? = null
        viewModel.onDrag(binding.vpCardHolder.currentItem)
        binding.btKnown.setOnClickListener {
            areOptionsEnabled(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
            viewModel.upOrDowngradeCard(true, userResponseModel.modelCard.cardDetails)
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getModelCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getModelCardsSum(),
                        viewModel.getProgress(),
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
        }
        binding.btKnownNot.setOnClickListener {
            viewModel.upOrDowngradeCard(false, userResponseModel.modelCard.cardDetails)
            viewModel.onNotCorrectAnswer(userResponseModel.modelCard.cardDetails)
            areOptionsEnabled(viewModel.isNextCardAnswered(binding.vpCardHolder.currentItem))
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(TIME_BEFORE_HIDING_ACTIONS)
                if (binding.vpCardHolder.currentItem >= viewModel.getModelCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getModelCardsSum(),
                        viewModel.getProgress(),
                        viewModel.getMissedCard()
                    )
                } else {
                    viewModel.onNotCorrectAnswer(userResponseModel.modelCard.cardDetails)
                    val itemPosition = binding.vpCardHolder.currentItem
                    binding.vpCardHolder.setCurrentItem(
                        itemPosition.plus(1),
                        true
                    )
                }
            }
        }
        if (userResponseModel.modelCardPosition > 0) {
            isRewindButtonActive(true)
            binding.btRewind.setOnClickListener {
                areOptionsEnabled(true)
                val b = userResponseModel.modelCardPosition.minus(1)
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

    private fun optionsState(
        userResponseModel: UserResponseModel
    ) {
        var fetchJob1: Job? = null
        fetchJob1?.cancel()
        fetchJob1 = lifecycleScope.launch {
            delay(TIME_BEFORE_SHOWING_ACTIONS)
            areOptionsEnabled(true)
        }
    }

    private fun displayReview(
        knownCardsSum: Int,
        missedCardsSum: Int,
        totalCardsSum: Int,
        progression: Int,
        missedCardsList: List<ImmutableCard?>
        ) {

        binding.vpCardHolder.visibility = View.GONE
        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
        binding.lyContainerOptions.visibility = View.GONE
        binding.gameReviewContainerMQ.visibility = View.VISIBLE
        binding.gameReviewLayoutMQ.apply {
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "Test")
            tvTotalCardsSumScoreLayout.text = totalCardsSum.toString()
            tvMissedCardSumScoreLayout.text = missedCardsSum.toString()
            tvKnownCardsSumScoreLayout.text = knownCardsSum.toString()

            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
                knownCardsSum.toFloat() / totalCardsSum,
                ContextCompat.getColor(this@TestQuizGameActivity, R.color.green50),
                ContextCompat.getColor(this@TestQuizGameActivity, R.color.green400),
            ) as Int

            val mossedCardsBackgroundColor = ArgbEvaluator().evaluate(
                missedCardsSum.toFloat() / totalCardsSum,
                ContextCompat.getColor(this@TestQuizGameActivity, R.color.red50),
                ContextCompat.getColor(this@TestQuizGameActivity, R.color.red400),
            ) as Int

            val textColorKnownCards =
                if (totalCardsSum / 2 < viewModel.getKnownCardSum())
                    ContextCompat.getColor(this@TestQuizGameActivity, R.color.green50)
                else ContextCompat.getColor(this@TestQuizGameActivity, R.color.green400)

            val textColorMissedCards =
                if (totalCardsSum / 2 < viewModel.getMissedCardSum())
                    ContextCompat.getColor(this@TestQuizGameActivity, R.color.red50)
                else ContextCompat.getColor(this@TestQuizGameActivity, R.color.red400)

            tvMissedCardSumScoreLayout.setTextColor(textColorMissedCards)
            tvMissedCardScoreLayout.setTextColor(textColorMissedCards)
            tvKnownCardsSumScoreLayout.setTextColor(textColorKnownCards)
            tvKnownCardsScoreLayout.setTextColor(textColorKnownCards)

            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
            cvContainerMissedCards.background.setTint(mossedCardsBackgroundColor)

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@TestQuizGameActivity, MainActivity::class.java))
                finish()
            }

            btRestartQuizScoreLayout.setOnClickListener {
                viewModel.initTest()
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
                    viewModel.initTest()
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
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.initModelCardList(cardList)
        viewModel.getCards()
        binding.vpCardHolder.setCurrentItem(0, true)
    }

    private fun isRewindButtonActive(isActive: Boolean) {
        binding.btRewind.isClickable = isActive
        binding.btRewind.isActivated = isActive
    }

    private fun giveFeedback(
        button: MaterialButton,
        isAnswerCorrect: Boolean
    ) {
        if (isAnswerCorrect) {
            button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green50)
            button.strokeColor = ContextCompat.getColorStateList(this, R.color.green500)
        } else {
            button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red50)
            button.strokeColor = ContextCompat.getColorStateList(this, R.color.red500)
        }
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
        viewColor: ColorStateList,
        firstLanguage: String,
        secondLanguage: String
    ) {

        var position = 0
        val textSum = text.size
        //val textColor = view[0].textColors
        val onReadColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, Color.GRAY)

        val params = Bundle()

        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onReading(position, view, onReadColor)
            }

            override fun onDone(utteranceId: String?) {
                onReadingStop(position, view, viewColor)
                position += 1
                if (position < textSum) {
                    speak(secondLanguage, params, text, position, this)
                } else {
                    position = 0
                    return
                }
            }

            override fun onError(utteranceId: String?) {
                TODO("Not yet implemented")
            }
        }

        speak(firstLanguage, params, text, position, speechListener)

    }

    private fun onReading(
        position: Int,
        view: List<View>,
        onReadColor: Int,
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
        onReadColor: ColorStateList
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
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            when (it) {
                TextToSpeech.SUCCESS -> {
                    if (tts.isSpeaking) {
                        tts.stop()
                        tts.shutdown()
                    } else {
                        tts.language = Locale.forLanguageTag(
                            TextToSpeechHelper().getLanguageCodeForTextToSpeech(language)!!
                        )
                        tts.setSpeechRate(1.0f)
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
                        tts.speak(text[position], TextToSpeech.QUEUE_ADD, params, "UniqueID")
                    }
                }

                else -> {
                    Toast.makeText(this, getString(R.string.error_read), Toast.LENGTH_LONG).show()
                }
            }
        })

        tts.setOnUtteranceProgressListener(speechListener)
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

}