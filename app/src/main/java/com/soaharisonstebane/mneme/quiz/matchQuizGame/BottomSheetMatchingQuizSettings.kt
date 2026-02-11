package com.soaharisonstebane.mneme.quiz.matchQuizGame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.BottomSheetMatchingQuizSettingsBinding
import com.soaharisonstebane.mneme.quiz.matchQuizGame.MatchQuizGameActivity.Companion.BOARD_SIZE
import com.soaharisonstebane.mneme.util.BoardSizes.BOARD_SIZE_1
import com.soaharisonstebane.mneme.util.BoardSizes.BOARD_SIZE_2
import com.soaharisonstebane.mneme.util.BoardSizes.BOARD_SIZE_3

class BottomSheetMatchingQuizSettings : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetMatchingQuizSettingsBinding
    private var matchingQuizSettingsSharedPref: SharedPreferences? = null
    private var matchingQuizSettingsSharedPrefEditor: SharedPreferences.Editor? = null

    companion object {
        const val TAG = "BottomSheetMatchingQuizSettings"
        const val BUNDLE_KEY_BOARD_SIZE = "40"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetMatchingQuizSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        matchingQuizSettingsSharedPref = context?.getSharedPreferences(
            "matchingQuizSettingsPref",
            Context.MODE_PRIVATE
        )
        matchingQuizSettingsSharedPrefEditor = matchingQuizSettingsSharedPref?.edit()

        when (matchingQuizSettingsSharedPref?.getString(BOARD_SIZE, BOARD_SIZE_1)) {
            BOARD_SIZE_1 -> {
                binding.rbBoardSize1.isChecked = true
            }
            BOARD_SIZE_2 -> {
                binding.rbBoardSize2.isChecked = true
            }
            BOARD_SIZE_3 -> {
                binding.rbBoardSize3.isChecked = true
            }
        }

        binding.btApplyRestartMatchingQuiz.setOnClickListener {
            when (binding.radioGroup.checkedRadioButtonId) {
                R.id.rb_board_size_1 -> {
                    senBoardSize(BOARD_SIZE_1)
                }
                R.id.rb_board_size_2 -> {
                    senBoardSize(BOARD_SIZE_2)
                }
                R.id.rb_board_size_3 -> {
                    senBoardSize(BOARD_SIZE_3)
                }
            }
        }
    }

    private fun senBoardSize(boardSize: String) {
        parentFragmentManager.setFragmentResult(
            MatchQuizGameActivity.REQUEST_KEY_SETTINGS,
            bundleOf(BUNDLE_KEY_BOARD_SIZE to boardSize)
        )
        dismiss()
    }

}