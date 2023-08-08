package com.example.flashcard.quiz.timedFlashCardGame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityTimedFlashCardGameBinding
import com.example.flashcard.quiz.baseFlashCardGame.BaseFlashCardGame
import com.example.flashcard.util.ThemePicker
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeableMethod

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) {
            setTheme(themRef)
        }

        binding = ActivityTimedFlashCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(BaseFlashCardGame.DECK_ID_KEY)
        deckWithCards?.let {
            cardList = it.cards.toExternal()
            deck = it.deck.toExternal()

            cardManager = CardStackLayoutManager(this, object : CardStackListener {
                override fun onCardDragging(direction: Direction?, ratio: Float) {
                    Log.d(TAG, "onCardDragging: d=" + direction?.name + " ratio=" + ratio)
                }

                override fun onCardSwiped(direction: Direction?) {
                    Log.d(TAG, "onCardSwiped: p=" + cardManager.topPosition + " d=" + direction)
                    when (direction) {
                        Direction.Top -> {
                            Toast.makeText(
                                this@TimedFlashCardGame,
                                "Direction Up",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        Direction.Bottom -> {
                            Toast.makeText(
                                this@TimedFlashCardGame,
                                "Direction Down",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        Direction.Left -> {
                            Toast.makeText(
                                this@TimedFlashCardGame,
                                "Direction Left",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this@TimedFlashCardGame,
                                "Direction Right",
                                Toast.LENGTH_LONG
                            ).show()
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
                setDirections(Direction.FREEDOM)
                setCanScrollHorizontal(true)
                setSwipeableMethod(SwipeableMethod.Manual)
                setOverlayInterpolator(LinearInterpolator())
            }

            cardAdapter = CardStackAdapter(this, cardList!!, deck!!)
            binding.cardStackView.apply {
                layoutManager = cardManager
                adapter = cardAdapter
                itemAnimator = DefaultItemAnimator()
            }

        }
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