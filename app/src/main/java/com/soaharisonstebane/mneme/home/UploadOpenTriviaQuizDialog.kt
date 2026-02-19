package com.soaharisonstebane.mneme.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.LyDialogUploadDeckWithCardsBinding
import com.soaharisonstebane.mneme.helper.OpenTriviaQuizCategoryHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UploadOpenTriviaQuizDialog : DialogFragment() {

    private var _binding: LyDialogUploadDeckWithCardsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val OPEN_TRIVIA_QUIZ_MODEL_BUNDLE_KEY = "100"
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = LyDialogUploadDeckWithCardsBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        val categories = OpenTriviaQuizCategoryHelper().getCategories()
        val arrayAdapterCategory = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.tvCategory.apply {
            setAdapter(arrayAdapterCategory)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.filter_spinner_dropdown_background,
                    requireActivity().theme
                )
            )
        }

        val difficulties = OpenTriviaQuizCategoryHelper().getDifficulty()
        val arrayAdapterDifficulties = ArrayAdapter(requireContext(), R.layout.dropdown_item, difficulties)
        binding.tvDifficulty.apply {
            setAdapter(arrayAdapterDifficulties)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.filter_spinner_dropdown_background,
                    requireActivity().theme
                )
            )
        }

        val types = OpenTriviaQuizCategoryHelper().getType()
        val arrayAdapterTypes = ArrayAdapter(requireContext(), R.layout.dropdown_item, types)
        binding.tvType.apply {
            setAdapter(arrayAdapterTypes)
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.filter_spinner_dropdown_background,
                    requireActivity().theme
                )
            )
        }

        binding.btBackToQuiz.setOnClickListener { dismiss() }
        binding.btUpload.setOnClickListener {
            if (checkForError()) {
                val settingsModel = OpenTriviaQuizCategoryHelper()
                    .selectCategory(binding.tvCategory.text.toString())
                    ?.let { it1 ->
                        OpenTriviaQuizModel(
                            number = binding.tvQuestionNumber.text.toString().toInt(),
                            category = it1,
                            difficulty = OpenTriviaQuizCategoryHelper().encodeDifficulty(
                                binding.tvDifficulty.text.toString()
                            ).lowercase(),
                            type = OpenTriviaQuizCategoryHelper().decodeType(binding.tvType.text.toString())
                        )
                    }
                if (settingsModel != null) {
                    sendOpenTriviaQuizMode(CardFragment.REQUEST_CODE, settingsModel)
                }
                dismiss()
            }
        }
        binding.btCancel.setOnClickListener { dismiss() }

        builder.setView(binding.root)
        return builder.create()
    }

    private fun checkForError(): Boolean {
        if (binding.tvQuestionNumber.text.toString().isBlank()) {
            binding.tvQuestionNumber.error =
                getString(R.string.error_message_dialog_upload_deck_with_cards_on_missing_card_sum)
            return false
        }
        if (binding.tvQuestionNumber.text.toString().toInt() <= 0) {
            binding.tvQuestionNumber.error =
                getString(R.string.error_message_dialog_upload_deck_with_cards_on_to_few_questions)
            return false
        }
        if (binding.tvQuestionNumber.text.toString().toInt() > 50) {
            binding.tvQuestionNumber.error =
                getString(R.string.error_message_dialog_upload_deck_with_cards_on_to_many_questions)
            return false
        }
        return true
    }


    private fun sendOpenTriviaQuizMode(
        requestCode: String,
        openTriviaQuizModel: OpenTriviaQuizModel
    ) {
        parentFragmentManager.setFragmentResult(
            requestCode,
            bundleOf(OPEN_TRIVIA_QUIZ_MODEL_BUNDLE_KEY to openTriviaQuizModel)
        )
    }

}