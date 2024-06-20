package com.example.flashcard.quiz.testQuizGame

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
import com.example.flashcard.util.CardType.FLASHCARD
import com.example.flashcard.util.CardType.ONE_OR_MULTI_ANSWER_CARD
import com.example.flashcard.util.CardType.TRUE_OR_FALSE_CARD
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class TestQuizGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestQuizGameBinding
    private val viewModel: TestQuizGameViewModel by viewModels {
        TestQuizGameViewModelFactory((application as FlashCardApplication).repository)
    }
    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private lateinit var oneOrMultipleAnswerCardModel: OneOrMultipleAnswerCardModel
    private lateinit var trueOrFalseCardModel: TrueOrFalseCardModel
    private lateinit var flashCardModel: TrueOrFalseCardModel

    private var deckWithCards: ImmutableDeckWithCards? = null
    private lateinit var testQuizGameAdapter: TestQuizGameAdapter

    private lateinit var tts: TextToSpeech

    companion object {
        private const val TAG = "TestQuizGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
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
                //startWritingQuizGame(cardList, deck)
            } else {
                //onNoCardToRevise()
            }
        }

        binding.vpCardHolder.isUserInputEnabled = false

        binding.topAppBar.apply {
            title = getString(R.string.title_flash_card_game, viewModel.deck?.deckName)
            setNavigationOnClickListener { finish() }
        }

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

    }

    private fun launchMultiChoiceQuizGame(
        data: List<ModelCard?>
    ) {
        testQuizGameAdapter = TestQuizGameAdapter(
            this,
            data,
            viewModel.getDeckColorCode(),
            viewModel.deck!!,
            { userResponseModel ->
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

    private fun onOneAndOneCardClicked(userResponseModel: UserResponseModel) {

        viewModel.onFlipCard(userResponseModel.modelCardPosition)
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
    }

    private fun optionsState(
        userResponseModel: UserResponseModel
    ) {
        var fetchJob1: Job? = null
        fetchJob1?.cancel()
        fetchJob1 = lifecycleScope.launch {
            delay(700)
            areOptionsEnabled(true)
        }

        binding.btKnown.setOnClickListener {
            areOptionsEnabled(false)
            viewModel.upOrDowngradeCard(true, userResponseModel.modelCard.cardDetails)
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(50)
                if (binding.vpCardHolder.currentItem >= viewModel.getModelCardsSum() - 1) {
                    displayReview(
                        viewModel.getKnownCardSum(),
                        viewModel.getMissedCardSum(),
                        viewModel.getModelCardsSum(),
                        viewModel.getProgress(),
                        viewModel.getMissedCard()
                    )

                } else {
                    binding.vpCardHolder.setCurrentItem(
                        binding.vpCardHolder.currentItem.plus(1),
                        true
                    )
                }

            }
        }
        binding.btKnownNot.setOnClickListener {
            viewModel.upOrDowngradeCard(false, userResponseModel.modelCard.cardDetails)
            viewModel.onNotCorrectAnswer(userResponseModel.modelCard.cardDetails)
            areOptionsEnabled(false)
            fetchJob1?.cancel()
            fetchJob1 = lifecycleScope.launch {
                delay(50)
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
                    binding.vpCardHolder.setCurrentItem(
                        binding.vpCardHolder.currentItem.plus(1),
                        true
                    )
                }
            }
        }
        if (userResponseModel.modelCardPosition > 0) {
            isRewindButtonActive(true)
            binding.btRewind.setOnClickListener {
                areOptionsEnabled(true)
                fetchJob1?.cancel()
                fetchJob1 = lifecycleScope.launch {
                    delay(100)
                    binding.vpCardHolder.setCurrentItem(
                        userResponseModel.modelCardPosition.minus(1),
                        true
                    )
                }
            }
        } else {
            isRewindButtonActive(false)
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
            lpiQuizResultDiagramScoreLayout.progress = progression
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "Test")
            tvTotalCardsSumScoreLayout.text = totalCardsSum.toString()
            tvMissedCardSumScoreLayout.text = missedCardsSum.toString()
            tvKnownCardsSumScoreLayout.text = knownCardsSum.toString()

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

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

}