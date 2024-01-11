package com.example.flashcard.quiz.matchQuizGame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.flashcard.backend.Model.ImmutableCard
import com.example.flashcard.backend.Model.ImmutableDeck
import com.example.flashcard.backend.Model.MatchQuizGameItemModel
import com.example.flashcard.backend.Model.toExternal
import com.example.flashcard.backend.entities.relations.DeckWithCards
import com.example.flashcard.databinding.ActivityMatchQuizGameBinding
import com.example.flashcard.util.ThemePicker
import com.example.flashcard.util.UiState
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
        matchQuizGameRecyclerView = MatchQuizGameAdapter(this@MatchQuizGameActivity, items, viewModel.boardSize) {

            //Toast.makeText(this, "${it.text} Clicked", Toast.LENGTH_LONG).show()
            onItemClicked(it)

        }
        binding.rvMatchingGame.apply {
            adapter = matchQuizGameRecyclerView
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun onItemClicked(item: MatchQuizGameItemModel) {

        if (viewModel.selectItem(item)) {
            Toast.makeText(this, "Match! Match! Match!", Toast.LENGTH_LONG).show()
            matchQuizGameRecyclerView.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "XXX! XXX! XXX!", Toast.LENGTH_LONG).show()
        }

    }

    private fun startMatchQuizGame(cardList: List<ImmutableCard>, deck: ImmutableDeck) {
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
        viewModel.updateBoard()
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
    }
}