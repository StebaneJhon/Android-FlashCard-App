package com.ssoaharison.recall.util

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Nullable
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.backend.models.ImmutableDeck
import com.ssoaharison.recall.databinding.QuizModeFragmentBinding
import com.ssoaharison.recall.util.FlashCardMiniGameRef.FLASH_CARD_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.MATCHING_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.MULTIPLE_CHOICE_QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.QUIZ
import com.ssoaharison.recall.util.FlashCardMiniGameRef.TEST
import com.ssoaharison.recall.util.FlashCardMiniGameRef.WRITING_QUIZ
import com.ssoaharison.recall.util.ThemeConst.DARK_THEME


class QuizModeBottomSheet(
    private val deck: ExternalDeck
): BottomSheetDialogFragment() {

    private lateinit var binding: QuizModeFragmentBinding

    companion object {
        const val TAG = "QuizModeBottomSheet"
        const val START_QUIZ_BUNDLE_KEY = "4"
        const val REQUEST_CODE_QUIZ_MODE = "300"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val themePicker = ThemePicker()
        val sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
        val appThemeName = sharedPref?.getString("themName", "WHITE THEM")
        val appTheme = themePicker.selectTheme(appThemeName)
        val contextThemeWrapper = if (!deck.deckColorCode.isNullOrBlank()) {
            val deckTheme = if (appThemeName == DARK_THEME) {
                themePicker.selectDarkThemeByDeckColorCode(deck.deckColorCode, themePicker.getDefaultTheme())
            } else {
                themePicker.selectThemeByDeckColorCode(deck.deckColorCode, themePicker.getDefaultTheme())
            }
            ContextThemeWrapper(activity, deckTheme)
        } else {

            ContextThemeWrapper(activity, appTheme!!)
        }
        return BottomSheetDialog(contextThemeWrapper, theme)
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

//        binding.btWritingQuizGame.setOnClickListener {
//            sendQuizMode(
//                REQUEST_CODE_QUIZ_MODE,
//                START_QUIZ_BUNDLE_KEY,
//                WRITING_QUIZ
//            )
//        }
//
//        binding.btMatchingQuizGame.setOnClickListener {
//            sendQuizMode(
//                REQUEST_CODE_QUIZ_MODE,
//                START_QUIZ_BUNDLE_KEY,
//                MATCHING_QUIZ
//            )
//        }
//
//        binding.btFlashCardGame.setOnClickListener {
//            sendQuizMode(
//                REQUEST_CODE_QUIZ_MODE,
//                START_QUIZ_BUNDLE_KEY,
//                FLASH_CARD_QUIZ
//            )
//        }

        binding.btQuiz.setOnClickListener {
            sendQuizMode(
                REQUEST_CODE_QUIZ_MODE,
                START_QUIZ_BUNDLE_KEY,
                QUIZ
            )
        }

//        binding.multiChoiceQuizButton.setOnClickListener {
//            sendQuizMode(
//                REQUEST_CODE_QUIZ_MODE,
//                START_QUIZ_BUNDLE_KEY,
//                MULTIPLE_CHOICE_QUIZ
//            )
//        }
//
//        binding.btTest.setOnClickListener {
//            sendQuizMode(
//                REQUEST_CODE_QUIZ_MODE,
//                START_QUIZ_BUNDLE_KEY,
//                TEST
//            )
//        }

    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val inflater = super.onGetLayoutInflater(savedInstanceState)
        var contextThemeWrapper: Context? = null
        val themePicker = ThemePicker()
        if (!deck.deckColorCode.isNullOrBlank()) {
            val deckTheme = themePicker.selectThemeByDeckColorCode(deck.deckColorCode, themePicker.getDefaultTheme())
            contextThemeWrapper = ContextThemeWrapper(requireContext(), deckTheme)
        } else {
            val sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
            val appTheme = themePicker.selectTheme(sharedPref?.getString("themName", "WHITE THEM"))
            contextThemeWrapper = ContextThemeWrapper(requireContext(), appTheme!!)
        }
        return inflater.cloneInContext(contextThemeWrapper)
    }

    override fun getTheme(): Int {
//        var contextThemeWrapper: Context? = null
        val themePicker = ThemePicker()
        if (!deck.deckColorCode.isNullOrBlank()) {
            val deckTheme = themePicker.selectThemeByDeckColorCode(deck.deckColorCode, themePicker.getDefaultTheme())
            return deckTheme
        } else {
            val sharedPref = activity?.getSharedPreferences("settingsPref", Context.MODE_PRIVATE)
            val appTheme = themePicker.selectTheme(sharedPref?.getString("themName", "WHITE THEM"))
           return  appTheme!!
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