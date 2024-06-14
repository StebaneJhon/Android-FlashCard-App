package com.example.flashcard.quiz.multichoiceQuizGame

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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.card.TextToSpeechHelper
import com.example.flashcard.databinding.ActivityMultichoiceQuizGameBinding
import com.example.flashcard.mainActivity.MainActivity
import com.example.flashcard.settings.MiniGameSettingsSheet
import com.example.flashcard.util.FlashCardMiniGameRef
import com.example.flashcard.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MultiChoiceQuizGameActivity : AppCompatActivity(), MiniGameSettingsSheet.SettingsApplication {

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

    private lateinit var tts: TextToSpeech

    companion object {
        private const val TAG = "MultiChoiceQuizGameActivity"
        const val DECK_ID_KEY = "Deck_id_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        miniGamePref = getSharedPreferences(FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF, Context.MODE_PRIVATE)
        miniGamePrefEditor = miniGamePref?.edit()
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }
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
                startTimedFlashCard(cardList, deck)
            } else {
                onNoCardToRevise()
            }

        }

        binding.topAppBar.apply {
            title = getString(R.string.title_flash_card_game, viewModel.deck.deckName)
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
        //isFlashCardGameScreenHidden(true)
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
                if (viewModel.swipe()) {
                    binding.vpCardHolder.setCurrentItem(viewModel.getCurrentCardPosition(), true)
                } else {
                    onQuizComplete()
                }
            } else {
                onWrongAnswer(it.cvCard, it.cvCardOnWrongAnswer, animFadeIn!!, animFadeOut!!)
            }
        }) {
            val cardOrientation = getCardOrientation()
            if (cardOrientation == CARD_ORIENTATION_FRONT_AND_BACK) {
                readText(
                    it.text,
                    it.views,
                    it.originalTextColor,
                    viewModel.deck.deckFirstLanguage!!,
                    viewModel.deck.deckSecondLanguage!!
                )
            } else {
                readText(
                    it.text,
                    it.views,
                    it.originalTextColor,
                    viewModel.deck.deckSecondLanguage!!,
                    viewModel.deck.deckFirstLanguage!!
                )
            }

        }
        binding.vpCardHolder.adapter = multiChoiceGameAdapter
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
            lpiQuizResultDiagramScoreLayout.progress = viewModel.progress
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "Multi Choice Quiz")
            tvTotalCardsSumScoreLayout.text = viewModel.cardSum().toString()
            tvMissedCardSumScoreLayout.text = viewModel.getMissedCardSum().toString()
            tvKnownCardsSumScoreLayout.text = viewModel.getKnownCardSum().toString()

            btBackToDeckScoreLayout.setOnClickListener {
                startActivity(Intent(this@MultiChoiceQuizGameActivity, MainActivity::class.java))
                finish()
            }
            btRestartQuizScoreLayout.setOnClickListener {
                restartMultiChoiceQuiz()
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
                        startTimedFlashCard(newCards, viewModel.deck)
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


    private fun startTimedFlashCard(cardList: MutableList<ImmutableCard?>, deck: ImmutableDeck) {
        binding.vpCardHolder.setCurrentItem(0, true)
        binding.gameReviewContainerMQ.visibility = View.GONE
        binding.vpCardHolder.visibility = View.VISIBLE
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateCard(getCardOrientation())
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

}