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
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.MatchQuizGameItemModel
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityMatchQuizGameBinding
import com.example.flashcard.deck.MainActivity
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MatchQuizGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchQuizGameBinding

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var deckWithCards: DeckWithCards? = null
    lateinit var matchQuizGameRecyclerView: MatchQuizGameAdapter

    private val viewModel: MatchQuizGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchQuizGameBinding.inflate(layoutInflater)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        val appTheme = sharedPref?.getString("themName", "DARK THEM")
        val themRef = appTheme?.let { ThemePicker().selectTheme(it) }
        if (themRef != null) { setTheme(themRef) }
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards.toExternal()
            val deck = it.deck.toExternal()
            viewModel.initOriginalCardList(cardList)
            startMatchQuizGame(cardList, deck)
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

    private fun onItemClicked(itemDetails: List<Any>, items: List<MatchQuizGameItemModel>) {

        val item: MatchQuizGameItemModel = itemDetails[0] as MatchQuizGameItemModel
        val lyItem: MaterialCardView = itemDetails[1] as MaterialCardView

        if (viewModel.isQuizComplete()) {
            Snackbar.make(binding.lyMatchQuizGameRoot, "You already finished the quiz", Snackbar.LENGTH_LONG).show()
            return
        }

        if (viewModel.isItemActive(item)) {
            Snackbar.make(binding.lyMatchQuizGameRoot, "Select another item please", Snackbar.LENGTH_LONG).show()
            return
        }

        if (viewModel.selectItem(item)) {
            Toast.makeText(this, "Match! Match! Match!", Toast.LENGTH_LONG).show()
            if (viewModel.isQuizComplete()) {
                Snackbar.make(binding.lyMatchQuizGameRoot, "Done Congratulations!", Snackbar.LENGTH_LONG).show()
                onQuizComplete(viewModel.cardLeft(), items)
            }
        }

        matchQuizGameRecyclerView.notifyDataSetChanged()

    }

    private fun onQuizComplete(cardsLeft: Int, onBoardItems: List<MatchQuizGameItemModel>) {
        binding.gameScoreContainer.visibility = View.VISIBLE
        binding.lyGameScore.apply {
            tvCardsLeftInDeck.text = getString(R.string.cards_left_match_quiz_score, "${cardsLeft}")
            btBackToDeck.setOnClickListener {
                startActivity(Intent(this@MatchQuizGameActivity, MainActivity::class.java))
            }
            btRestart.setOnClickListener {
                displayMatchQuizGameItems(onBoardItems)
            }
            btContinue.setOnClickListener {
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

    private fun startMatchQuizGame(cardList: List<ImmutableCard>, deck: ImmutableDeck) {
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }
}