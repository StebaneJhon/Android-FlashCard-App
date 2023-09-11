package com.example.flashcard.quiz.baseFlashCardGame


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityBaseFlashCardGameBinding
import com.example.flashcard.databinding.ActivityTimedFlashCardGameBinding
import com.example.flashcard.deck.MainActivity
import com.example.flashcard.quiz.timedFlashCardGame.CardStackAdapter
import com.example.flashcard.quiz.timedFlashCardGame.CardStackCallback
import com.example.flashcard.quiz.timedFlashCardGame.TimedFlashCardViewModeFactory
import com.example.flashcard.quiz.timedFlashCardGame.TimedFlashCardViewModel
import com.example.flashcard.util.CardBackgroundSelector
import com.example.flashcard.util.DeckColorCategorySelector
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting
import com.yuyakaido.android.cardstackview.SwipeableMethod
import kotlinx.coroutines.launch

class BaseFlashCardGame : AppCompatActivity() {

    private val TAG: String = "TimedFlashCardGame"
    private lateinit var binding: ActivityBaseFlashCardGameBinding
    private val baseGameViewModel: BaseFlashCardGameViewModel by viewModels {
        BaseFlashCardGameViewModelFactory((application as FlashCardApplication).repository)
    }
    private lateinit var cardManager: CardStackLayoutManager
    private lateinit var cardAdapter: CardStackAdapter

    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private var deckWithCards: DeckWithCards? = null
    private var cardList: List<ImmutableCard>? = null
    private var deck: ImmutableDeck? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }

        binding = ActivityBaseFlashCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(BaseFlashCardGame.DECK_ID_KEY)
        deckWithCards?.let {
            cardList = it.cards.toExternal()
            deck = it.deck.toExternal()
            if (!cardList.isNullOrEmpty() && deck != null) {
                startBaseFlashCard(cardList!!, deck!!)
            }
        }

        binding.noButtonBF.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            cardManager.setSwipeAnimationSetting(setting)
            binding.cardStackViewBF.swipe()
        }
        binding.yesButtonBF.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            cardManager.setSwipeAnimationSetting(setting)
            binding.cardStackViewBF.swipe()
        }
        binding.rewindBF.setOnClickListener {
            Toast.makeText(
                this@BaseFlashCardGame,
                "Rewind",
                Toast.LENGTH_LONG
            ).show()
            binding.cardStackViewBF.rewind()
        }

    }

    private fun startBaseFlashCard(
        cardList: List<ImmutableCard>,
        deck: ImmutableDeck
    ) {
        areQuizButtonsActive(true)
        binding.feedbackCardBF.visibility = View.GONE
        cardManager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                Log.d(TAG, "onCardDragging: d=" + direction?.name + " ratio=" + ratio)
            }

            override fun onCardSwiped(direction: Direction?) {
                Log.d(TAG, "onCardSwiped: p=" + cardManager.topPosition + " d=" + direction)
                when (direction) {
                    Direction.Left -> {
                        baseGameViewModel.onCardUnknown(cardList)
                        lifecycleScope.launch {
                            getNextCard(cardList)
                        }
                    }

                    else -> {
                        // Right
                        baseGameViewModel.onCardKnown()
                        lifecycleScope.launch {
                            getNextCard(cardList)
                        }
                    }
                }
                if (cardManager.topPosition == cardAdapter.itemCount - 5) {
                    paginate()
                }
            }

            override fun onCardRewound() {
                Log.d(TAG, "onCardRewound: p=" + cardManager.topPosition)
            }

            override fun onCardCanceled() {
                Log.d(TAG, "onCardCancelde: p=" + cardManager.topPosition)
            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }
        })

        cardManager.apply {
            setStackFrom(StackFrom.None)
            setVisibleCount(3)
            setTranslationInterval(8.0f)
            setScaleInterval(0.95f)
            setSwipeThreshold(0.3f)
            setMaxDegree(20.0f)
            setDirections(Direction.HORIZONTAL)
            setCanScrollHorizontal(true)
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setOverlayInterpolator(LinearInterpolator())
        }

        cardAdapter = CardStackAdapter(this, cardList, deck)
        binding.cardStackViewBF.apply {
            layoutManager = cardManager
            adapter = cardAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private suspend fun getNextCard(
        cardList: List<ImmutableCard>
    ) {
        binding.feedbackCardBF.visibility = View.GONE
        baseGameViewModel.getActualCard(cardList)
        baseGameViewModel.actualCard.collect { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Error -> {
                    onQuizComplete(baseGameViewModel.getKnownCardSum(cardList), baseGameViewModel.getUnknownCards(), deck!!, cardList)
                }

                is UiState.Success -> {
                    val card = state.data
                    // initCard(card)
                }
            }
        }
    }

    private fun onQuizComplete(
        knownCardSum: Int,
        missedCard: List<ImmutableCard>,
        deck: ImmutableDeck,
        cardList: List<ImmutableCard>
    ) {
        binding.feedbackCardBF.visibility = View.VISIBLE
        areQuizButtonsActive(false)
        binding.feedbackLYBF.apply {
            knownCardsTF.text = knownCardSum.toString()
            missedCardTF.text = missedCard.size.toString()
            totalCardsSumTF.text = cardList.size.toString()
            backToDeckButtonTF.setOnClickListener {
                startActivity(Intent(this@BaseFlashCardGame, MainActivity::class.java))
            }
            if (missedCard.isEmpty()) {
                reviseMissedCardButtonTF.visibility = View.GONE
            } else {
                reviseMissedCardButtonTF.visibility = View.VISIBLE
            }
            reviseMissedCardButtonTF.setOnClickListener {
                val newCards = baseGameViewModel.getUnknownCards()
                baseGameViewModel.initFlashCard()
                startBaseFlashCard(newCards, deck)
            }
            restartFlashCardTF.setOnClickListener {
                Toast.makeText(this@BaseFlashCardGame, "Restart", Toast.LENGTH_LONG).show()
                baseGameViewModel.initFlashCard()
                startBaseFlashCard(cardList, deck)
            }
        }

    }

    private fun areQuizButtonsActive(isActive: Boolean) {
        binding.cardStackViewBF.isVisible = isActive
        binding.rewindBF.isClickable = isActive
        binding.rewindBF.isVisible = isActive
        binding.noButtonBF.isClickable = isActive
        binding.noButtonBF.isVisible = isActive
        binding.yesButtonBF.isClickable = isActive
        binding.yesButtonBF.isVisible = isActive
    }

    private fun paginate() {
        val old = cardAdapter.getItems()
        val baru = cardList
        val callback = CardStackCallback(old, baru!!)
        val hasil = DiffUtil.calculateDiff(callback)
        cardAdapter.setItems(baru)
        hasil.dispatchUpdatesTo(cardAdapter)

    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }
}