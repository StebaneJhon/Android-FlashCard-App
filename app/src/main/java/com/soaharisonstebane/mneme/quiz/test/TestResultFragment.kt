package com.soaharisonstebane.mneme.quiz.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.soaharisonstebane.mneme.databinding.FragmentTestResultBinding


class TestResultFragment(
) : Fragment() {

    private var _binding: FragmentTestResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: TestResultRecyclerViewAdapter

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

//        lifecycleScope.launch {
//            testViewModel.getTestCards()
//            testViewModel.testCards
//                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
//                .collect { state ->
//                    when (state) {
//                        is UiState.Error -> {
//                            binding.testResultProgressBar.visibility = View.GONE
//                        }
//
//                        is UiState.Loading -> {
//                            binding.testResultProgressBar.visibility = View.VISIBLE
//                        }
//
//                        is UiState.Success -> {
//                            binding.testResultProgressBar.visibility = View.GONE
//                            displayResult(state.data)
//                        }
//                    }
//
//                    binding.rvTestResult.apply {
//                        adapter = recyclerViewAdapter
//                        setHasFixedSize(true)
//                        layoutManager = LinearLayoutManager(requireContext())
//                    }
//                }
//        }
//
//        binding.btBackToDeckTestResult.setOnClickListener {
//            onBackToDeck()
//        }
//
//        binding.btRetakeTestTestResult.setOnClickListener {
//            onRetakeTest()
//        }

    }

//    private fun displayResult(c: List<TestCardModel>) {
//
//        val totalCards = c.size
//        val knowCards = calculateKnownCards(c)
//        val missedCards = calculateMissedCards(knowCards, totalCards)
//
//        val knownCardsBackgroundColor = ArgbEvaluator().evaluate(
//            knowCards.toFloat() / totalCards,
//            ContextCompat.getColor(requireContext(), R.color.green50),
//            ContextCompat.getColor(requireContext(), R.color.green400),
//        ) as Int
//
//        val missedCardsBackgroundColor = ArgbEvaluator().evaluate(
//            missedCards.toFloat() / totalCards,
//            ContextCompat.getColor(requireContext(), R.color.red50),
//            ContextCompat.getColor(requireContext(), R.color.red400),
//        ) as Int
//
//        val textColorKnownCards =
//            if (totalCards / 2 < knowCards)
//                ContextCompat.getColor(requireContext(), R.color.green50)
//            else ContextCompat.getColor(requireContext(), R.color.green400)
//
//        val textColorMissedCards =
//            if (totalCards / 2 < missedCards)
//                ContextCompat.getColor(requireContext(), R.color.red50)
//            else ContextCompat.getColor(requireContext(), R.color.red400)
//
//        binding.tvTotalCardsSumTestResult.text = totalCards.toString()
//        binding.tvKnownCardsSumTestResult.apply {
//            text = knowCards.toString()
//            setTextColor(textColorKnownCards)
//        }
//        binding.tvKnownCardsTestResult.setTextColor(textColorKnownCards)
//        binding.tvTotalMissedCardsSumTestResult.apply {
//            text = missedCards.toString()
//            setTextColor(textColorMissedCards)
//        }
//        binding.tvTotalMissedCardsTestResult.setTextColor(textColorMissedCards)
//        binding.cvContainerKnownCards.background.setTint(knownCardsBackgroundColor)
//        binding.cvContainerTotalMissedCards.background.setTint(missedCardsBackgroundColor)
//
//        val pref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
//        val appTheme = pref?.getString("themName", "WHITE THEM") ?: "WHITE THEM"
//        recyclerViewAdapter = TestResultRecyclerViewAdapter(
//            requireContext(),
//            c,
//            appTheme
//        )
//    }
//
//    private fun calculateKnownCards(cards: List<TestCardModel>): Int {
//        var knownCards = 0
//        cards.forEach { card ->
//            var index = 0
//            var correctAnswer = 0
//            while (index < card.cardDefinition.size) {
//                val actualCard = card.cardDefinition[index]
//                if (actualCard.isSelected && actualCard.isCorrect == 0) {
//                    correctAnswer = 0
//                    testViewModel.submitResult(card, false)
//                    break
//                }
//                if (actualCard.isSelected && actualCard.isCorrect == 1 && actualCard.attachedCardId != actualCard.cardId){
//                    correctAnswer = 0
//                    testViewModel.submitResult(card, false)
//                    break
//                }
//                if (actualCard.isSelected && actualCard.isCorrect == 1 && actualCard.attachedCardId == actualCard.cardId) {
//                    correctAnswer++
//                }
//
//                index++
//            }
//            if (correctAnswer != 0) {
//                knownCards++
//                testViewModel.submitResult(card, true)
//                correctAnswer = 0
//            }
//        }
//        return knownCards
//    }
//
//    private fun onBackToDeck() {
//        setFragmentResult(
//            TEST_RESULT_REQUEST_KEY,
//            bundleOf(TEST_RESULT_BUNDLE_KEY to BACK_TO_DECK)
//        )
//        parentFragmentManager.popBackStack()
//    }
//
//    private fun onRetakeTest() {
//        setFragmentResult(TEST_RESULT_REQUEST_KEY, bundleOf(TEST_RESULT_BUNDLE_KEY to RETAKE_TEST))
//        parentFragmentManager.popBackStack()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        testViewModel.pauseTimer()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        testViewModel.pauseTimer()
//    }
//
//    private fun calculateMissedCards(knowCards: Int, totalCards: Int) = totalCards.minus(knowCards)

}