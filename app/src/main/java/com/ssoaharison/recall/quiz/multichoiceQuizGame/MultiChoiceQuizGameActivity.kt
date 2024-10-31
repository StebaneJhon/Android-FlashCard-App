package com.ssoaharison.recall.quiz.multichoiceQuizGame

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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.Model.ImmutableCard
import com.ssoaharison.recall.backend.Model.ImmutableDeck
import com.ssoaharison.recall.backend.Model.ImmutableDeckWithCards
import com.ssoaharison.recall.databinding.ActivityMultichoiceQuizGameBinding
import com.ssoaharison.recall.mainActivity.MainActivity
import com.ssoaharison.recall.settings.MiniGameSettingsSheet
import com.ssoaharison.recall.util.LanguageUtil
import com.ssoaharison.recall.util.FlashCardMiniGameRef
import com.ssoaharison.recall.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MultiChoiceQuizGameActivity :
    AppCompatActivity(),
    MiniGameSettingsSheet.SettingsApplication,
    TextToSpeech.OnInitListener
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

    private var tts: TextToSpeech? = null
    private var fetchJob: Job? = null

    companion object {
        private const val TAG = "MultiChoiceQuizGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
        const val WAITING_TIME_ON_CORRECT_ANSWER_BEFORE_SWIPE = 700L
        const val WAITING_TIME_ON_WRONG_ANSWER_BEFORE_FEEDBACK = 150L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        miniGamePref = getSharedPreferences(FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF, Context.MODE_PRIVATE)
        miniGamePrefEditor = miniGamePref?.edit()
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }
        tts = TextToSpeech(this, this)
        binding = ActivityMultichoiceQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vpCardHolder.isUserInputEnabled = false

        animFadeIn = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        animFadeOut = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards?.toMutableList()
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                viewModel.initOriginalCardList(cardList)
                startMultiChoiceQuizGame(cardList, deck)
            } else {
                onNoCardToRevise()
            }

        }

        binding.topAppBar.apply {
            title = getString(R.string.multiple_choice_quiz_button_text)
            subtitle = getString(R.string.title_flash_card_game, viewModel.deck.deckName)
            setNavigationOnClickListener { finish() }
        }

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

    private fun getCardOrientation() = miniGamePref?.getString(
        FlashCardMiniGameRef.CHECKED_CARD_ORIENTATION,
        FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
    ) ?: FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK

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
        restartMultiChoiceQuiz()
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
            }
        }
    }

    private fun unableShowUnKnownCardOnly() {
        miniGamePrefEditor?.apply {
            putBoolean(FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY, false)
            apply()
        }
    }

    private fun launchMultiChoiceQuizGame(data: List<MultiChoiceGameCardModel>) {
        val multiChoiceGameAdapter = MultiChoiceQuizGameAdapter(this, data, viewModel.deck.deckColorCode!!, {
            if (viewModel.isUserChoiceCorrect(it.userChoice, it.answer)) {
                giveFeedback(it.selectedButton, true)
                fetchJob?.cancel()
                fetchJob = lifecycleScope.launch {
                    delay(WAITING_TIME_ON_CORRECT_ANSWER_BEFORE_SWIPE)
                    if (viewModel.swipe()) {
                        binding.vpCardHolder.setCurrentItem(viewModel.getCurrentCardPosition(), true)
                        restoreAnswerButtons()
                    } else {
                        onQuizComplete()
                    }
                }
            } else {
                onWrongAnswer(it.cvCard, it.cvCardOnWrongAnswer, animFadeIn!!, animFadeOut!!)
                fetchJob?.cancel()
                fetchJob = lifecycleScope.launch {
                    delay(WAITING_TIME_ON_WRONG_ANSWER_BEFORE_FEEDBACK)
                    giveFeedback(it.selectedButton, false)
                    giveFeedback(it.selectedButtonOnWrong, false)
                }
            }
        }) {
            if (tts?.isSpeaking == true) {
                stopReading(it.views, it.speakButton)
            } else {
                val cardOrientation = getCardOrientation()
                if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
                    readText(
                        it.text,
                        it.views,
                        viewModel.deck.deckFirstLanguage!!,
                        viewModel.deck.deckSecondLanguage!!,
                        it.speakButton,
                    )
                } else {
                    readText(
                        it.text,
                        it.views,
                        viewModel.deck.deckSecondLanguage!!,
                        viewModel.deck.deckFirstLanguage!!,
                        it.speakButton
                    )
                }
            }

        }
        binding.vpCardHolder.adapter = multiChoiceGameAdapter
    }

    private fun stopReading (
        views: List<View>,
        speakButton: Button
        ) {
        speakButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_speak, 0, 0, 0)
        tts?.stop()
        views.forEach { v ->
            (v as TextView).setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK))
        }
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

    private fun readText(
        text: List<String>,
        view: List<View>,
        firstLanguage: String,
        secondLanguage: String,
        speakButton: Button
    ) {
        var position = 0
        val textSum = text.size
        val onReadColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainerHighest, Color.GRAY)
        val onStopColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
        val params = Bundle()
        val speechListener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onReading(position, view, onReadColor, speakButton)
            }
            override fun onDone(utteranceId: String?) {
                onReadingStop(position, view, onStopColor, speakButton)
                position += 1
                if (position < textSum) {
                    speak(secondLanguage, params, text, position, this)
                } else {
                    position = 0
                    return
                }
            }
            override fun onError(utteranceId: String?) {
                Toast.makeText(this@MultiChoiceQuizGameActivity, getString(R.string.error_read), Toast.LENGTH_LONG).show()
            }
        }
        speak(firstLanguage, params, text, position, speechListener)
    }

    private fun onReading(
        position: Int,
        view: List<View>,
        onReadColor: Int,
        speakButton: Button
    ) {
        speakButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_stop, 0, 0, 0)
        if (position == 0) {
            (view[position] as TextView).setTextColor(onReadColor)
        } else {
            (view[position] as MaterialButton).setTextColor(onReadColor)
        }
    }

    private fun onReadingStop(
        position: Int,
        view: List<View>,
        onReadColor: Int,
        speakButton: Button
    ) {
        speakButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_speak, 0, 0, 0)
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
        tts?.language = Locale.forLanguageTag(
            LanguageUtil().getLanguageCodeForTextToSpeech(language)!!
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts?.speak(text[position], TextToSpeech.QUEUE_ADD, params, "UniqueID")
        tts?.setOnUtteranceProgressListener(speechListener)
    }

    private fun onWrongAnswer(
        card: MaterialCardView,
        onWrongCard: MaterialCardView,
        animFadeIn: Animation,
        animFadeOut: Animation
    ) {
        card.startAnimation(animFadeOut)
        card.visibility = View.GONE
        onWrongCard.visibility = View.VISIBLE
        onWrongCard.startAnimation(animFadeIn)
        lifecycleScope.launch {
            delay(500)
            onWrongCard.startAnimation(animFadeOut)
            onWrongCard.visibility = View.GONE
            card.visibility = View.VISIBLE
            card.startAnimation(animFadeIn)
        }
    }

    private fun onQuizComplete() {
        binding.gameReviewContainerMQ.visibility = View.VISIBLE
        binding.vpCardHolder.visibility = View.GONE
        binding.gameReviewLayoutMQ.apply {
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "Multi Choice Quiz")
            tvTotalCardsSumScoreLayout.text = viewModel.cardSum().toString()
            tvMissedCardSumScoreLayout.text = viewModel.getMissedCardSum().toString()
            tvKnownCardsSumScoreLayout.text = viewModel.getKnownCardSum().toString()

            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getKnownCardSum().toFloat() / viewModel.cardSum(),
                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.green50),
                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.green400),
            ) as Int

            val mossedCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getMissedCardSum().toFloat() / viewModel.cardSum(),
                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.red50),
                ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.red400),
            ) as Int

            val textColorKnownCards =
                if (viewModel.cardSum() / 2 < viewModel.getKnownCardSum())
                    ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.green50)
                else ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.green400)

            val textColorMissedCards =
                if (viewModel.cardSum() / 2 < viewModel.getMissedCardSum())
                    ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.red50)
                else ContextCompat.getColor(this@MultiChoiceQuizGameActivity, R.color.red400)

            tvMissedCardSumScoreLayout.setTextColor(textColorMissedCards)
            tvMissedCardScoreLayout.setTextColor(textColorMissedCards)
            tvKnownCardsSumScoreLayout.setTextColor(textColorKnownCards)
            tvKnownCardsScoreLayout.setTextColor(textColorKnownCards)

            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
            cvContainerMissedCards.background.setTint(mossedCardsBackgroundColor)

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@MultiChoiceQuizGameActivity, MainActivity::class.java))
                finish()
            }
            btRestartQuizWithPreviousCardsScoreLayout.setOnClickListener {
                viewModel.initTimedFlashCard()
                startMultiChoiceQuizGame(viewModel.getOriginalCardList().toMutableList(), viewModel.deck)
            }
            if (viewModel.getMissedCardSum() == 0) {
                btReviseMissedCardScoreLayout.apply {
                    isActivated = false
                    isVisible = false
                }
            } else {
                btReviseMissedCardScoreLayout.apply {
                    isActivated = true
                    isVisible = true
                    setOnClickListener {
                        val newCards = viewModel.getMissedCard()
                        viewModel.initTimedFlashCard()
                        startMultiChoiceQuizGame(newCards, viewModel.deck)
                    }
                }
            }
        }
    }

    private fun restartMultiChoiceQuiz() {
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.lyOnNoMoreCardsErrorContainer.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.initTimedFlashCard()
        viewModel.updateCard(getCardOrientation())
    }


    private fun startMultiChoiceQuizGame(cardList: MutableList<ImmutableCard?>, deck: ImmutableDeck) {
        binding.vpCardHolder.setCurrentItem(0, true)
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateCard(getCardOrientation())
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

        val onWrongButtonsOriginalColorStateList = ContextCompat.getColorStateList(this, R.color.red50)
        val onWrongButtonsStrokeOriginalColorStateList = ContextCompat.getColorStateList(this, R.color.red500)

        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative1_on_wrong_answer).apply {
            backgroundTintList = onWrongButtonsOriginalColorStateList
            strokeColor = onWrongButtonsStrokeOriginalColorStateList
        }
        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative2_on_wrong_answer).apply {
            backgroundTintList = onWrongButtonsOriginalColorStateList
            strokeColor = onWrongButtonsStrokeOriginalColorStateList
        }
        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative3_on_wrong_answer).apply {
            backgroundTintList = onWrongButtonsOriginalColorStateList
            strokeColor = onWrongButtonsStrokeOriginalColorStateList
        }
        binding.vpCardHolder.findViewById<MaterialButton>(R.id.bt_alternative4_on_wrong_answer).apply {
            backgroundTintList = onWrongButtonsOriginalColorStateList
            strokeColor = onWrongButtonsStrokeOriginalColorStateList
        }

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
                Toast.makeText(this, getString(R.string.error_read), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

}