package com.soaharisonstebane.mneme.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.databinding.QuizModeFragmentBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.soaharisonstebane.mneme.util.FlashCardMiniGameRef.QUIZ
import kotlin.getValue


class QuizModeBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: QuizModeFragmentBinding

    val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    companion object {
        const val TAG = "QuizModeBottomSheet"
        const val START_QUIZ_BUNDLE_KEY = "4"
        const val REQUEST_CODE_QUIZ_MODE = "300"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewTheme = deckPathViewModel.getViewTheme()
        val contextThemeWrapper = ContextThemeWrapper(requireActivity(), viewTheme)
        return BottomSheetDialog(contextThemeWrapper)
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