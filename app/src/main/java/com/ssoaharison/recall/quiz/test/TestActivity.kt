package com.ssoaharison.recall.quiz.test

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.databinding.ActivityTestBinding
import com.ssoaharison.recall.quiz.quizGame.QuizGameActivity
import com.ssoaharison.recall.util.LanguageUtil
import com.ssoaharison.recall.util.TestResultAction.BACK_TO_DECK
import com.ssoaharison.recall.util.TestResultAction.RETAKE_TEST
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.ssoaharison.recall.util.TextType.CONTENT
import com.ssoaharison.recall.util.TextType.DEFINITION
import com.ssoaharison.recall.util.TextWithLanguageModel
import kotlinx.coroutines.launch
import java.util.Locale

class TestActivity :
    AppCompatActivity(),
    TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityTestBinding
    private var sharedPref: SharedPreferences? = null

    private var deckWithCards: ImmutableDeckWithCards? = null

    private var tts: TextToSpeech? = null

    private val testViewModel: TestViewModel by viewModels {
        TestViewModelFactory((application as FlashCardApplication).repository)
    }

    private lateinit var testAdapter: TestAdapter

    companion object {
        const val TAG = "TestActivity"
        const val DECK_ID_KEY = "Deck_id_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        deckWithCards = intent?.parcelable(QuizGameActivity.DECK_ID_KEY)

        deckWithCards?.let {

            val deck = it.deck
            val cardList = it.cards

            binding.topAppBar.apply {
                title = getString(R.string.title_test, deck?.deckName)
                setNavigationOnClickListener { finish() }
            }

            deck?.let { d ->
                testViewModel.initDeck(d)
            }

            cardList?.let { cards ->
                testViewModel.initOriginalCards(cards)
                testViewModel.initLocalCards(cards)
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    testViewModel.getTestCards()
                    testViewModel.testCards.collect { state ->
                        when (state) {
                            is UiState.Error -> {
                                onNoCardToRevise()
                            }

                            is UiState.Loading -> {
                                binding.testActivityProgressBar.visibility = View.VISIBLE
                                binding.fragmentContainerView.visibility = View.GONE
                            }

                            is UiState.Success -> {
                                binding.testActivityProgressBar.visibility = View.GONE
                                binding.fragmentContainerView.visibility = View.GONE
                                launchTest(state.data, deck!!)
                                startTimer()
                            }
                        }
                    }
                }
            }

        }

    }

    private fun startTimer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                testViewModel.startTimer()
                testViewModel.timer.collect { t ->
                    binding.topAppBar.subtitle = getString(R.string.text_time, t.formatTime())
                }
            }
        }
    }

    private fun launchTest(
        cards: List<TestCardModel>,
        deck: ImmutableDeck
    ) {
        testAdapter = TestAdapter(
            this,
            cards,
            deck,
            { userAnswer ->
                testViewModel.noteSingleUserAnswer(userAnswer)
                testAdapter.notifyDataSetChanged()
            }
        ) { dataToRead ->
            if (tts?.isSpeaking == true) {
                stopReading(dataToRead.views)
            } else {
                readText(
                    dataToRead.text,
                    dataToRead.views,
                )
            }

        }

//        val sideItemVisibility = 75
//        val horizontalItemMargin = 40
//
//        val itemDecoration = object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(
//                outRect: Rect,
//                view: View,
//                parent: RecyclerView,
//                state: RecyclerView.State
//            ) {
//                outRect.left = horizontalItemMargin
//                outRect.right = horizontalItemMargin
//            }
//        }
//
//        val pageTranslationX = horizontalItemMargin + sideItemVisibility
//        val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
//            page.translationX = -pageTranslationX * position
//        }

        binding.vpCardHolder.apply {
            adapter = testAdapter
//            offscreenPageLimit = 1
//            addItemDecoration(itemDecoration)
//            setPageTransformer(pageTransformer)
        }

        binding.vpCardHolder.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding.btNextQuestion.apply {
                    if (binding.vpCardHolder.currentItem < cards.size.minus(1)) {
                        setOnClickListener {
                            binding.vpCardHolder.currentItem += 1
                        }
                        text = null
                        iconPadding =
                            resources.getDimension(R.dimen.icon_padding_next_question_button)
                                .toInt()
                    } else {
                        text = getString(R.string.bt_text_submit)
                        iconGravity = MaterialButton.ICON_GRAVITY_END
                        iconPadding =
                            resources.getDimension(R.dimen.icon_padding_submit_button).toInt()
                        setOnClickListener {
                            onShowTestResult()
                        }
                    }
                    tts?.stop()
                }
                binding.btPreviousQuestion.apply {
                    if (binding.vpCardHolder.currentItem > 0) {
                        isActivated = true
                        isClickable = true
                        backgroundTintList = MaterialColors
                            .getColorStateList(
                                this@TestActivity,
                                com.google.android.material.R.attr.colorPrimary,
                                ContextCompat.getColorStateList(
                                    this@TestActivity, R.color.neutral700
                                )!!
                            )
                        setOnClickListener {
                            binding.vpCardHolder.currentItem -= 1
                        }
                        iconTint = MaterialColors
                            .getColorStateList(
                                this@TestActivity,
                                com.google.android.material.R.attr.colorSurfaceContainerLowest,
                                ContextCompat.getColorStateList(
                                    this@TestActivity, R.color.neutral50
                                )!!
                            )
//                        setOnClickListener {
//                            binding.vpCardHolder.currentItem -= 1
//                        }
                    } else {
                        isActivated = false
                        isClickable = false
                        backgroundTintList = MaterialColors
                            .getColorStateList(
                                this@TestActivity,
                                com.google.android.material.R.attr.colorSurfaceContainerLowest,
                                ContextCompat.getColorStateList(
                                    this@TestActivity, R.color.neutral50
                                )!!
                            )
                        iconTint = MaterialColors
                            .getColorStateList(
                                this@TestActivity,
                                com.google.android.material.R.attr.colorPrimary,
                                ContextCompat.getColorStateList(
                                    this@TestActivity, R.color.neutral700
                                )!!
                            )
                    }
                    tts?.stop()
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

    }

    private fun onShowTestResult() {
        testViewModel.pauseTimer()
        binding.testActivityProgressBar.visibility = View.GONE
        binding.fragmentContainerView.visibility = View.VISIBLE
        supportFragmentManager.apply {
            commit {
                setReorderingAllowed(true)
                add<TestResultFragment>(R.id.fragment_container_view)
            }
            setFragmentResultListener(
                TestResultFragment.TEST_RESULT_REQUEST_KEY,
                this@TestActivity
            ) { _, bundle ->
                val result = bundle.getString(TestResultFragment.TEST_RESULT_BUNDLE_KEY)
                if (result == BACK_TO_DECK) {
                    finish()
                }
                if (result == RETAKE_TEST) {
                    restartTest()
                }
            }
        }
    }


    private fun restartTest() {
        binding.testActivityProgressBar.visibility = View.VISIBLE
        binding.fragmentContainerView.visibility = View.GONE
        testViewModel.onRetakeTest()
        binding.vpCardHolder.currentItem = 0
        binding.testActivityProgressBar.visibility = View.GONE
        testViewModel.stopTimer()
        testViewModel.startTimer()
    }


    private fun onNoCardToRevise() {
        binding.lyOnNoMoreCardsErrorContainerTest.isVisible = true
        binding.vpCardHolder.visibility = View.GONE
        binding.lyNoCardErrorTest.apply {
            btBackToDeck.setOnClickListener {
                finish()
            }

            btUnableUnknownCardOnly.setOnClickListener {
            }
        }
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
                Toast.makeText(this@TestActivity, getString(R.string.error_read), Toast.LENGTH_LONG)
                    .show()
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
        if (actualText.language.isNullOrBlank()) {
            LanguageUtil().detectLanguage(
                text = actualText.text,
                onError = {showSnackBar(R.string.error_message_error_while_detecting_language)},
                onLanguageUnIdentified = {showSnackBar(R.string.error_message_can_not_identify_language)},
                onLanguageNotSupported = {showSnackBar(R.string.error_message_language_not_supported)},
                onSuccess = { detectedLanguage ->
                    when (actualText.textType) {
                        CONTENT -> testViewModel.updateCardContentLanguage(actualText.cardId, detectedLanguage)
                        DEFINITION -> testViewModel.updateCardDefinitionLanguage(actualText.cardId, detectedLanguage)
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
            LanguageUtil().getLanguageCodeForTextToSpeech(language)!!
        )
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts?.speak(text, TextToSpeech.QUEUE_ADD, params, "UniqueID")
        tts?.setOnUtteranceProgressListener(speechListener)
    }

    private fun showSnackBar(
        @StringRes messageRes: Int
    ) {
        Snackbar.make(
            binding.main,
            getString(messageRes),
            Snackbar.LENGTH_LONG
        ).show()
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

    override fun onPause() {
        super.onPause()
        testViewModel.pauseTimer()
    }

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