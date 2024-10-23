package com.ssoaharison.recall.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.ssoaharison.recall.databinding.QuizModeFragmentBinding
import com.ssoaharison.recall.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.MATCHING_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.MULTIPLE_CHOICE_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.TEST
import com.ssoaharison.recall.util.FlashCardMiniGameRef.WRITING_QUIZ
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuizModeBottomSheet: BottomSheetDialogFragment() {

    private lateinit var binding: QuizModeFragmentBinding

    companion object {
        const val TAG = "QuizModeBottomSheet"
        const val START_QUIZ_BUNDLE_KEY = "4"
        const val REQUEST_CODE_QUIZ_MODE = "300"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = QuizModeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btWritingQuizGame.setOnClickListener {
            sendQuizMode(
                REQUEST_CODE_QUIZ_MODE,
                START_QUIZ_BUNDLE_KEY,
                WRITING_QUIZ
            )
        }

        binding.btMatchingQuizGame.setOnClickListener {
            sendQuizMode(
                REQUEST_CODE_QUIZ_MODE,
                START_QUIZ_BUNDLE_KEY,
                MATCHING_QUIZ
            )
        }

        binding.btFlashCardGame.setOnClickListener {
            sendQuizMode(
                REQUEST_CODE_QUIZ_MODE,
                START_QUIZ_BUNDLE_KEY,
                FLASH_CARD_QUIZ
            )
        }

        binding.btQuiz.setOnClickListener {
            sendQuizMode(
                REQUEST_CODE_QUIZ_MODE,
                START_QUIZ_BUNDLE_KEY,
                QUIZ
            )
        }

        binding.multiChoiceQuizButton.setOnClickListener {
            sendQuizMode(
                REQUEST_CODE_QUIZ_MODE,
                START_QUIZ_BUNDLE_KEY,
                MULTIPLE_CHOICE_QUIZ
            )
        }

        binding.btTest.setOnClickListener {
            sendQuizMode(
                REQUEST_CODE_QUIZ_MODE,
                START_QUIZ_BUNDLE_KEY,
                TEST
            )
        }

    }

    private fun sendQuizMode(
        requestCode: String,
        bundleCode: String,
        quizMode: String
    ) {
        parentFragmentManager.setFragmentResult(requestCode, bundleOf(bundleCode to quizMode))
        dismiss()
    }

}