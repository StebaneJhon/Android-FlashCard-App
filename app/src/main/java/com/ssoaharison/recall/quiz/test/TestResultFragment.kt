package com.ssoaharison.recall.quiz.test

import android.animation.ArgbEvaluator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.FragmentTestResultBinding
import com.ssoaharison.recall.util.TestResultAction.BACK_TO_DECK
import com.ssoaharison.recall.util.TestResultAction.RETAKE_TEST
import com.ssoaharison.recall.util.UiState
import kotlinx.coroutines.launch


class TestResultFragment(
) : Fragment() {

    private var _binding: FragmentTestResultBinding? = null
    private val binding get() = _binding!!

    lateinit var recyclerViewAdapter: TestResultRecyclerViewAdapter

    private val testViewModel: TestViewModel by activityViewModels()

    companion object {
        const val TAG = "TestResultFragment"
        const val TEST_RESULT_REQUEST_KEY = "600"
        const val TEST_RESULT_BUNDLE_KEY = "6"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            testViewModel.getTestCards()
            testViewModel.testCards
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state ->
                    when (state) {
                        is UiState.Error -> {
                            binding.testResultProgressBar.visibility = View.GONE
                        }

                        is UiState.Loading -> {
                            binding.testResultProgressBar.visibility = View.VISIBLE
                        }

                        is UiState.Success -> {
                            binding.testResultProgressBar.visibility = View.GONE
                            displayResult(state.data)
                        }
                    }

                    binding.rvTestResult.apply {
                        adapter = recyclerViewAdapter
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(requireContext())
                    }
                }
        }

        binding.btBackToDeckTestResult.setOnClickListener {
            onBackToDeck()
        }

        binding.btRetakeTestTestResult.setOnClickListener {
            onRetakeTest()
        }

    }

    private fun displayResult(c: List<TestCardModel>) {

        val totalCards = c.size
        val knowCards = calculateKnownCards(c)
        val missedCards = calculateMissedCards(knowCards, totalCards)

        val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
            knowCards.toFloat() / totalCards,
            ContextCompat.getColor(requireContext(), R.color.green50),
            ContextCompat.getColor(requireContext(), R.color.green400),
        ) as Int

        val missedCardsBackgroundColor = ArgbEvaluator().evaluate(
            missedCards.toFloat() / totalCards,
            ContextCompat.getColor(requireContext(), R.color.red50),
            ContextCompat.getColor(requireContext(), R.color.red400),
        ) as Int

        val textColorKnownCards =
            if (totalCards / 2 < knowCards)
                ContextCompat.getColor(requireContext(), R.color.green50)
            else ContextCompat.getColor(requireContext(), R.color.green400)

        val textColorMissedCards =
            if (totalCards / 2 < missedCards)
                ContextCompat.getColor(requireContext(), R.color.red50)
            else ContextCompat.getColor(requireContext(), R.color.red400)

        binding.tvScoreTitleTestResult.text = getString(R.string.flashcard_score_title_text, "Test")
        binding.tvTotalCardsSumTestResult.text = totalCards.toString()
        binding.tvKnownCardsSumTestResult.apply {
            text = knowCards.toString()
            setTextColor(textColorKnownCards)
        }
        binding.tvKnownCardsTestResult.setTextColor(textColorKnownCards)
        binding.tvTotalMissedCardsSumTestResult.apply {
            text = missedCards.toString()
            setTextColor(textColorMissedCards)
        }
        binding.tvTotalMissedCardsTestResult.setTextColor(textColorMissedCards)
        binding.cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
        binding.cvContainerTotalMissedCards.background.setTint(missedCardsBackgroundColor)

        recyclerViewAdapter = TestResultRecyclerViewAdapter(
            requireContext(),
            c
        )
    }

    private fun calculateKnownCards(cards: List<TestCardModel>): Int {
        var knownCards = 0
        cards.forEach { card ->
            var index = 0
            var correctAnswer = 0
            while (index < card.cardDefinition.size) {
                val actualCard = card.cardDefinition[index]
                if (actualCard.isSelected && actualCard.isCorrect == 0) {
                    correctAnswer = 0
                    testViewModel.submitResult(card, false)
                    break
                }
                if (actualCard.isSelected && actualCard.isCorrect == 1 && actualCard.attachedCardId != actualCard.cardId){
                    correctAnswer = 0
                    testViewModel.submitResult(card, false)
                    break
                }
                if (actualCard.isSelected && actualCard.isCorrect == 1 && actualCard.attachedCardId == actualCard.cardId) {
                    correctAnswer++
                }

                index++
            }
            if (correctAnswer != 0) {
                knownCards++
                testViewModel.submitResult(card, true)
                correctAnswer = 0
            }
        }
        return knownCards
    }

    private fun onBackToDeck() {
        setFragmentResult(
            TEST_RESULT_REQUEST_KEY,
            bundleOf(TEST_RESULT_BUNDLE_KEY to BACK_TO_DECK)
        )
        parentFragmentManager.popBackStack()
    }

    private fun onRetakeTest() {
        setFragmentResult(TEST_RESULT_REQUEST_KEY, bundleOf(TEST_RESULT_BUNDLE_KEY to RETAKE_TEST))
        parentFragmentManager.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        testViewModel.pauseTimer()
    }

    override fun onStart() {
        super.onStart()
        testViewModel.pauseTimer()
    }

    private fun calculateMissedCards(knowCards: Int, totalCards: Int) = totalCards.minus(knowCards)

}