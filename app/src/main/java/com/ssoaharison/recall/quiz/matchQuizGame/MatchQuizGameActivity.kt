package com.ssoaharison.recall.quiz.matchQuizGame

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.models.ImmutableCard
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.backend.models.ImmutableDeckWithCards
import com.ssoaharison.recall.backend.models.MatchQuizGameItemModel
import com.ssoaharison.recall.databinding.ActivityMatchQuizGameBinding
import com.ssoaharison.recall.mainActivity.MainActivity
import com.ssoaharison.recall.util.MatchQuizGameClickStatus
import com.ssoaharison.recall.util.ThemePicker
import com.ssoaharison.recall.util.UiState
import com.google.android.material.snackbar.Snackbar
import com.ssoaharison.recall.quiz.flashCardGame.FlashCardGameActivity
import com.ssoaharison.recall.quiz.flashCardGame.FlashCardGameActivity.Companion
import com.ssoaharison.recall.util.BoardSizes.BOARD_SIZE_1
import com.ssoaharison.recall.util.BoardSizes.BOARD_SIZE_2
import com.ssoaharison.recall.util.BoardSizes.BOARD_SIZE_3
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME
import com.ssoaharison.recall.util.parcelable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatchQuizGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchQuizGameBinding

    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var matchingQuizSettingsSharedPref: SharedPreferences? = null
    private var matchingQuizSettingsSharedPrefEditor: SharedPreferences.Editor? = null

    private var deckWithCards: ImmutableDeckWithCards? = null
    private lateinit var matchQuizGameRecyclerView: MatchQuizGameAdapter

    private val viewModel: MatchQuizGameViewModel by viewModels()

    private var firstSelectedItemInfo: MatchingQuizGameSelectedItemInfo? = null

    private var matchingQuizJob: Job? = null

    companion object {
        const val DECK_ID_KEY = "Deck_id_key"
        private const val TAG = "MatchQuizGameActivity"
        const val REQUEST_KEY_SETTINGS = "400"
        const val BOARD_SIZE = "board_size"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
        matchingQuizSettingsSharedPref =
            getSharedPreferences("matchingQuizSettingsPref", Context.MODE_PRIVATE)
        matchingQuizSettingsSharedPrefEditor = matchingQuizSettingsSharedPref?.edit()
        val themePicker = ThemePicker()
        val appTheme = sharedPref?.getString("themName", "WHITE THEM")
        val themRef = ThemePicker().selectTheme(appTheme)

        deckWithCards = intent?.parcelable(FlashCardGameActivity.DECK_ID_KEY)

        val deckColorCode = deckWithCards?.deck?.deckColorCode

        if (deckColorCode.isNullOrBlank() && themRef != null) {
            setTheme(themRef)
        } else if (themRef != null && !deckColorCode.isNullOrBlank()) {
            val deckTheme = if (appTheme == DARK_THEME) {
                themePicker.selectDarkThemeByDeckColorCode(deckColorCode, themRef)
            } else {
                themePicker.selectThemeByDeckColorCode(deckColorCode, themRef)
            }
            setTheme(deckTheme)
        } else {
            setTheme(themePicker.getDefaultTheme())
        }

        binding = ActivityMatchQuizGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deckWithCards = intent?.parcelable(DECK_ID_KEY)
        deckWithCards?.let {
            val cardList = it.cards
            val deck = it.deck
            if (!cardList.isNullOrEmpty() && deck != null) {
                viewModel.initOriginalCardList(cardList)
                startMatchQuizGame(cardList, deck)
            }
        }

        binding.topAppBar.apply {
            title = getString(R.string.bt_match_quiz_mode_text)
            subtitle = getString(R.string.title_flash_card_game, viewModel.deck.deckName)
            setNavigationOnClickListener { finish() }
        }

        setUpBoard()

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.mn_bt_settings) {

                showMatchingQuizSettings()

                true
            } else {
                false
            }
        }

    }

    private fun showMatchingQuizSettings() {
        val settings = BottomSheetMatchingQuizSettings()
        settings.show(supportFragmentManager, BottomSheetMatchingQuizSettings.TAG)
        supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY_SETTINGS,
            this
        ) { requestKey, bundle ->
            val result = bundle.getString(BottomSheetMatchingQuizSettings.BUNDLE_KEY_BOARD_SIZE)
            if (!result.isNullOrBlank()) {
                viewModel.initQuiz()
                setBoardSize(result)
                setUpBoard()
            }
        }
    }

    private fun setBoardSize(board: String) {
        matchingQuizSettingsSharedPrefEditor?.apply {
            putString(BOARD_SIZE, board)
            apply()
        }
    }

    private fun displayMatchQuizGameItems(items: List<MatchQuizGameItemModel>) {
        binding.gameScoreContainer.visibility = View.GONE
        binding.rvMatchingGame.visibility = View.VISIBLE

        val boardSize = when {
            items.size.div(2) == getBoardSize().getCardCount() -> {
                getBoardSize()
            }
            items.size.div(2) == MatchQuizGameBorderSize.BOARD_2.getCardCount() -> {
                MatchQuizGameBorderSize.BOARD_2
            }
            items.size.div(2) == MatchQuizGameBorderSize.BOARD_1.getCardCount() -> {
                MatchQuizGameBorderSize.BOARD_1
            }
            else -> {
                null
            }
        }
        boardSize?.let {
            updateBoard(items, boardSize)
        }
    }

    private fun updateBoard(items: List<MatchQuizGameItemModel>, boardSize: MatchQuizGameBorderSize) {
        viewModel.initOnBoardItems(items.toMutableList())
        matchQuizGameRecyclerView =
            MatchQuizGameAdapter(this@MatchQuizGameActivity, items, boardSize) {
                onItemClicked(it, items)
            }
        binding.rvMatchingGame.apply {
            adapter = matchQuizGameRecyclerView
            layoutManager = GridLayoutManager(this@MatchQuizGameActivity, boardSize.getWidth())
            setHasFixedSize(true)
        }
    }

    private fun getBoardSize(): MatchQuizGameBorderSize {
        return when (matchingQuizSettingsSharedPref?.getString(BOARD_SIZE, BOARD_SIZE_1)) {
            BOARD_SIZE_1 -> {
                MatchQuizGameBorderSize.BOARD_1
            }

            BOARD_SIZE_2 -> {
                MatchQuizGameBorderSize.BOARD_2
            }

            BOARD_SIZE_3 -> {
                MatchQuizGameBorderSize.BOARD_3
            }

            else -> {
                MatchQuizGameBorderSize.BOARD_1
            }
        }
    }

    private fun onItemClicked(
        itemDetails: MatchingQuizGameSelectedItemInfo,
        items: List<MatchQuizGameItemModel>
    ) {
        if (viewModel.isQuizComplete(getBoardSize())) {
            Snackbar.make(
                binding.lyMatchQuizGameRoot,
                getString(R.string.message_on_quiz_already_done),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        if (viewModel.isItemActive(itemDetails.item) && firstSelectedItemInfo != null) {
            Snackbar.make(
                binding.lyMatchQuizGameRoot,
                getString(R.string.error_message_select_another_item),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        when (viewModel.selectItem(itemDetails.item)) {
            MatchQuizGameClickStatus.FIRST_TRY -> {
                activateItem(itemDetails)
                firstSelectedItemInfo = itemDetails
            }

            MatchQuizGameClickStatus.MATCH -> {
                disableItem(itemDetails)
                disableItem(firstSelectedItemInfo!!)
                if (viewModel.isQuizComplete(getBoardSize())) {
                    Snackbar.make(
                        binding.lyMatchQuizGameRoot,
                        getString(R.string.message_on_quiz_done),
                        Snackbar.LENGTH_LONG
                    ).show()
                    onQuizComplete(items)
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

    private fun onQuizComplete(onBoardItems: List<MatchQuizGameItemModel>) {
        binding.gameScoreContainer.visibility = View.VISIBLE
        binding.rvMatchingGame.visibility = View.GONE
        binding.lyGameScore.apply {
            tvMoveNumberSumLayout.text = viewModel.getNumMove().toString()
            tvMissedMoveSumLayout.text = viewModel.getNumMiss().toString()
            tvTotalCardSumScoreLayout.text = viewModel.getOnBoardCardSum(getBoardSize()).toString()

            val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getNumMove().toFloat() / viewModel.getOnBoardCardSum(getBoardSize()),
                ContextCompat.getColor(this@MatchQuizGameActivity, R.color.green50),
                ContextCompat.getColor(this@MatchQuizGameActivity, R.color.green400),
            ) as Int

            val missedCardsBackgroundColor = ArgbEvaluator().evaluate(
                viewModel.getNumMiss().toFloat() / viewModel.getOnBoardCardSum(getBoardSize()),
                ContextCompat.getColor(this@MatchQuizGameActivity, R.color.red50),
                ContextCompat.getColor(this@MatchQuizGameActivity, R.color.red400),
            ) as Int

            val textColorKnownCards =
                if (viewModel.getOnBoardCardSum(getBoardSize()) / 2 < viewModel.getNumMove())
                    ContextCompat.getColor(this@MatchQuizGameActivity, R.color.green50)
                else ContextCompat.getColor(this@MatchQuizGameActivity, R.color.green400)

            val textColorMissedCards =
                if (viewModel.getOnBoardCardSum(getBoardSize()) / 2 < viewModel.getNumMiss())
                    ContextCompat.getColor(this@MatchQuizGameActivity, R.color.red50)
                else ContextCompat.getColor(this@MatchQuizGameActivity, R.color.red400)

            tvMissedMoveSumLayout.setTextColor(textColorMissedCards)
            tvMissedMoveLayout.setTextColor(textColorMissedCards)
            tvMoveNumberSumLayout.setTextColor(textColorKnownCards)
            tvMoveNumberLayout.setTextColor(textColorKnownCards)

            cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
            cvContainerMissedCards.background.setTint(missedCardsBackgroundColor)

            btBackToDeck.setOnClickListener {
                startActivity(Intent(this@MatchQuizGameActivity, MainActivity::class.java))
            }
            btRestart.setOnClickListener {
                displayMatchQuizGameItems(onBoardItems)
            }
            if (viewModel.cardLeft() < 0) {
                btContinue.visibility = View.GONE
            } else {
                btContinue.apply {
                    text =
                        getString(R.string.cards_left_match_quiz_score, "${viewModel.cardLeft()}")
                    setOnClickListener {
                        setUpBoard()
                    }
                }
            }

        }
    }

    private fun setUpBoard() {
        matchingQuizJob?.cancel()
        matchingQuizJob = lifecycleScope.launch {
            viewModel.updateBoardCards(getBoardSize())
            viewModel
                .actualCards
                .collect { state ->
                    when (state) {
                        is UiState.Error -> showToast(state.errorMessage)
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            displayMatchQuizGameItems(state.data)
                        }
                    }
                }
        }
    }

    private fun showToast(state: String) {
        Toast.makeText(
            this@MatchQuizGameActivity,
            state,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun startMatchQuizGame(cardList: List<ImmutableCard?>, deck: ImmutableDeck) {
        viewModel.initCardList(cardList)
        viewModel.initDeck(deck)
    }

}