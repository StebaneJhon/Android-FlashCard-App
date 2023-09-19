package com.example.flashcard.quiz.timedFlashCardGame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
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
import com.example.flashcard.databinding.ActivityTimedFlashCardGameBinding
import com.example.flashcard.deck.MainActivity
import com.example.flashcard.quiz.baseFlashCardGame.BaseFlashCardGame
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.button.MaterialButton
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting
import com.yuyakaido.android.cardstackview.SwipeableMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimedFlashCardGame : AppCompatActivity() {

    private val TAG: String = "TimedFlashCardGame"
    private lateinit var binding: ActivityTimedFlashCardGameBinding
    private val timedFlashCardViewModel: TimedFlashCardViewModel by viewModels {
        TimedFlashCardViewModeFactory((application as FlashCardApplication).repository)
    }
    private lateinit var cardManager: CardStackLayoutManager
    private lateinit var cardAdapter: CardStackAdapter

    var sharedPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private var deckWithCards: DeckWithCards? = null
    private var cardList: List<ImmutableCard>? = null
    private var deck: ImmutableDeck? = null

    private var isFront = true
    private var cardItemFront: CardView? = null
    private var cardItemBack: CardView? = null
    private var front_anim: AnimatorSet? = null
    private var back_anim: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }

        binding = ActivityTimedFlashCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        front_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.front_animator
        ) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.back_animator
        ) as AnimatorSet

        deckWithCards = intent?.parcelable(BaseFlashCardGame.DECK_ID_KEY)
        deckWithCards?.let {
            cardList = it.cards.toExternal()
            deck = it.deck.toExternal()
            if (!cardList.isNullOrEmpty() && deck != null) {
                startTimedFlashCard(cardList!!, deck!!)
            }
        }

        binding.noButton.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            cardManager.setSwipeAnimationSetting(setting)
            binding.cardStackView.swipe()
        }
        binding.yesButton.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            cardManager.setSwipeAnimationSetting(setting)
            binding.cardStackView.swipe()
        }
        binding.rewind.setOnClickListener {
            Toast.makeText(
                this@TimedFlashCardGame,
                "Rewind",
                Toast.LENGTH_LONG
            ).show()
            binding.cardStackView.rewind()
        }
    }

    private fun startTimedFlashCard(
        cardList: List<ImmutableCard>,
        deck: ImmutableDeck
    ) {
        areQuizButtonsActive(true)
        binding.feedbackCardTF.visibility = View.GONE
        cardManager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                Log.d(TAG, "onCardDragging: d=" + direction?.name + " ratio=" + ratio)
            }

            override fun onCardSwiped(direction: Direction?) {
                Log.d(TAG, "onCardSwiped: p=" + cardManager.topPosition + " d=" + direction)
                when (direction) {
                    Direction.Left -> {
                        timedFlashCardViewModel.onCardUnknown(cardList)
                        lifecycleScope.launch {
                            getNextCard(cardList)
                        }
                    }

                    else -> {
                        // Right
                        timedFlashCardViewModel.onCardKnown()
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


        cardAdapter = CardStackAdapter(this, cardList, deck) { front, back ->
            cardItemFront = front
            cardItemBack = back
            flipCardItem(cardItemFront!!, cardItemBack!!)
        }

        binding.cardStackView.apply {
            layoutManager = cardManager
            adapter = cardAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun flipCardItem(
        front: CardView,
        back: CardView
    ) {

        val scale: Float = applicationContext.resources.displayMetrics.density
        cardItemFront?.cameraDistance = 8000 * scale
        cardItemBack?.cameraDistance = 8000 * scale

        isFront = if (isFront) {
            front_anim?.setTarget(front)
            back_anim?.setTarget(back)
            front_anim?.start()
            back_anim?.start()
            false
        } else {
            front_anim?.setTarget(back)
            back_anim?.setTarget(front)
            back_anim?.start()
            front_anim?.start()
            true
        }
    }

    private suspend fun getNextCard(
        cardList: List<ImmutableCard>
    ) {
        binding.feedbackCardTF.visibility = View.GONE
        timedFlashCardViewModel.getActualCard(cardList)
        timedFlashCardViewModel.actualCard.collect { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Error -> {
                    onQuizComplete(timedFlashCardViewModel.getKnownCardSum(cardList), timedFlashCardViewModel.getUnknownCards(), deck!!, cardList)
                }

                is UiState.Success -> {
                    val card = state.data
                    /*
                    lifecycleScope.launch {
                        delay(5000)
                        flipCardItem(cardItemFront!!, cardItemBack!!)
                    }

                     */
                }

                else -> {}
            }
        }
    }

    private fun onQuizComplete(
        knownCardSum: Int,
        missedCard: List<ImmutableCard>,
        deck: ImmutableDeck,
        cardList: List<ImmutableCard>
    ) {
        binding.feedbackCardTF.visibility = View.VISIBLE
        areQuizButtonsActive(false)
        binding.feedbackLY.apply {
            knownCardsTF.text = knownCardSum.toString()
            missedCardTF.text = missedCard.size.toString()
            totalCardsSumTF.text = cardList.size.toString()
            backToDeckButtonTF.setOnClickListener {
                startActivity(Intent(this@TimedFlashCardGame, MainActivity::class.java))
            }
            if (missedCard.isEmpty()) {
                reviseMissedCardButtonTF.visibility = View.GONE
            } else {
                reviseMissedCardButtonTF.visibility = View.VISIBLE
            }
            reviseMissedCardButtonTF.setOnClickListener {
                val newCards = timedFlashCardViewModel.getUnknownCards()
                timedFlashCardViewModel.initFlashCard()
                startTimedFlashCard(newCards, deck)
            }
            restartFlashCardTF.setOnClickListener {
                Toast.makeText(this@TimedFlashCardGame, "Restart", Toast.LENGTH_LONG).show()
                timedFlashCardViewModel.initFlashCard()
                startTimedFlashCard(cardList, deck)
            }
        }

    }

    private fun areQuizButtonsActive(isActive: Boolean) {
        binding.cardStackView.isVisible = isActive
        binding.rewind.isClickable = isActive
        binding.rewind.isVisible = isActive
        binding.noButton.isClickable = isActive
        binding.noButton.isVisible = isActive
        binding.yesButton.isClickable = isActive
        binding.yesButton.isVisible = isActive
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
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }
}