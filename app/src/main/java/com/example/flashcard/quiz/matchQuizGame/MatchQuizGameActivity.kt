package com.example.flashcard.quiz.matchQuizGame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.ImmutableDeckWithCards
import com.example.flashcard.backend.Model.MatchQuizGameItemModel
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityMatchQuizGameBinding
import com.example.flashcard.mainActivity.MainActivity
import com.example.flashcard.util.MatchQuizGameClickStatus
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatchQuizGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchQuizGameBinding

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var deckWithCards: ImmutableDeckWithCards? = null
    private lateinit var matchQuizGameRecyclerView: MatchQuizGameAdapter

    private val viewModel: MatchQuizGameViewModel by viewModels()

    private var firstSelectedItemInfo: MatchingQuizGameSelectedItemInfo? = null

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
        private const val TAG = "MatchQuizGameActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }
        binding = ActivityMatchQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                viewModel.initOriginalCardList(cardList)
                startMatchQuizGame(cardList, deck)
            } else {
                // TODO: On Deck or CardList null
            }
        }

        binding.topAppBar.apply {
            title = getString(R.string.title_flash_card_game, viewModel.deck.deckName)
            setNavigationOnClickListener { finish() }
        }

        lifecycleScope.launch {
            viewModel.updateBoard()
            viewModel
                .actualCards
                .collect { state ->
                    when (state) {
                        is UiState.Error -> Toast.makeText(this@MatchQuizGameActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            displayMatchQuizGameItems(state.data)
                        }
                    }
                }
        }

    }

    private fun displayMatchQuizGameItems(items: List<MatchQuizGameItemModel>) {
        binding.gameScoreContainer.visibility = View.GONE
        binding.rvMatchingGame.visibility = View.VISIBLE
        binding.lpiMatchingQuizGameProgression.progress = 0
        viewModel.initOnBoardItems(items.toMutableList())
        matchQuizGameRecyclerView = MatchQuizGameAdapter(this@MatchQuizGameActivity, items, viewModel.boardSize) {
            onItemClicked(it, items)
        }
        binding.rvMatchingGame.apply {
            adapter = matchQuizGameRecyclerView
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun onItemClicked(itemDetails: MatchingQuizGameSelectedItemInfo, items: List<MatchQuizGameItemModel>) {
        if (viewModel.isQuizComplete()) {
            Snackbar.make(binding.lyMatchQuizGameRoot, "You already finished the quiz", Snackbar.LENGTH_LONG).show()
            return
        }

        if (viewModel.isItemActive(itemDetails.item) && firstSelectedItemInfo != null) {
            Snackbar.make(binding.lyMatchQuizGameRoot, "Select another item please", Snackbar.LENGTH_LONG).show()
            return
        }

        when (viewModel.selectItem(itemDetails.item)) {
            MatchQuizGameClickStatus.FIRST_TRY -> {
                activateItem(itemDetails)
                firstSelectedItemInfo = itemDetails
            }
            MatchQuizGameClickStatus.MATCHE -> {
                disableItem(itemDetails)
                disableItem(firstSelectedItemInfo!!)
                Toast.makeText(this, "Match! Match! Match!", Toast.LENGTH_LONG).show()
                binding.lpiMatchingQuizGameProgression.progress = viewModel.getProgression()
                if (viewModel.isQuizComplete()) {
                    Snackbar.make(binding.lyMatchQuizGameRoot, "Done Congratulations!", Snackbar.LENGTH_LONG).show()
                    onQuizComplete(viewModel.cardLeft(), items)
                }
                firstSelectedItemInfo = null
            }
            MatchQuizGameClickStatus.MATCH_NOT -> {
                activateItemOnWrong(firstSelectedItemInfo!!)
                activateItemOnWrong(itemDetails)
                lifecycleScope.launch {
                    delay(400)
                    inactivateItem(firstSelectedItemInfo!!)
                    inactivateItem(itemDetails)
                    firstSelectedItemInfo = null
                }
            }
        }

    }

    private fun activateItem(itemInfo: MatchingQuizGameSelectedItemInfo) {
        itemInfo.itemContainerActive.visibility = View.VISIBLE
        itemInfo.itemContainerWrong.visibility = View.GONE
        itemInfo.itemContainerInactive.visibility = View.GONE
    }

    private fun activateItemOnWrong(itemInfo: MatchingQuizGameSelectedItemInfo) {
        itemInfo.itemContainerActive.visibility = View.GONE
        itemInfo.itemContainerWrong.visibility = View.VISIBLE
        itemInfo.itemContainerInactive.visibility = View.GONE
    }

    private fun inactivateItem(itemInfo: MatchingQuizGameSelectedItemInfo) {
        itemInfo.itemContainerActive.visibility = View.GONE
        itemInfo.itemContainerWrong.visibility = View.GONE
        itemInfo.itemContainerInactive.visibility = View.VISIBLE
    }

    private fun disableItem(itemInfo: MatchingQuizGameSelectedItemInfo) {
        itemInfo.itemContainerRoot.apply {
            visibility = View.GONE
            isClickable = false
        }
    }

    private fun onQuizComplete(cardsLeft: Int, onBoardItems: List<MatchQuizGameItemModel>) {
        binding.gameScoreContainer.visibility = View.VISIBLE
        binding.rvMatchingGame.visibility = View.GONE
        binding.lyGameScore.apply {
            tvScoreTitleScoreLayout.text = getString(R.string.flashcard_score_title_text, "Matching Quiz")
            tvMoveNumberSumLayout.text = viewModel.getNumMove().toString()
            tvMissedMoveSumLayout.text = viewModel.getNumMiss().toString()
            tvTotalCardsSumScoreLayout.text = viewModel.getOnBoardCardSum().toString()
            lpiQuizResultDiagramScoreLayout.progress = viewModel.getUserPerformance()
            btBackToDeck.setOnClickListener {
                startActivity(Intent(this@MatchQuizGameActivity, MainActivity::class.java))
            }
            btRestart.setOnClickListener {
                displayMatchQuizGameItems(onBoardItems)
            }
            if (cardsLeft < 0) {
                btContinue.visibility = View.GONE
            } else {
                btContinue.apply {
                    text = getString(R.string.cards_left_match_quiz_score, "$cardsLeft")
                    setOnClickListener {
                        lifecycleScope.launch {
                            viewModel.updateBoard()
                            viewModel
                                .actualCards
                                .collect { state ->
                                    when (state) {
                                        is UiState.Error -> Toast.makeText(this@MatchQuizGameActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                                        is UiState.Loading -> {}
                                        is UiState.Success -> {
                                            displayMatchQuizGameItems(state.data)
                                        }
                                    }
                                }
                        }
                    }
                }
            }

        }
    }

    private fun startMatchQuizGame(cardList: List<ImmutableCard>, deck: ImmutableDeck) {
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

}