package com.example.flashcard.deck

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.flashcard.R
import com.example.flashcard.util.OpenTriviaQuizCategoryHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class UploadOpenTriviaQuizDialog() : DialogFragment() {

    private var tvCategory: AutoCompleteTextView? = null
    private var tvDifficulty: AutoCompleteTextView? = null
    private var tvType: AutoCompleteTextView? = null
    private var tvNumber: TextInputEditText? = null
    private var btDismiss: MaterialButton? = null
    private var btUpload: Button? = null
    private var btCancel: Button? = null
    private var deckName: TextInputEditText? = null

    companion object {
        const val OPEN_TRIVIA_QUIZ_MODEL_BUNDLE_KEY = "100"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.ly_dialog_upload_deck_with_cards, null)

        tvCategory = view?.findViewById(R.id.tv_category)
        tvDifficulty = view?.findViewById(R.id.tv_difficulty)
        tvType = view?.findViewById(R.id.tv_type)
        tvNumber = view?.findViewById(R.id.tv_question_number)
        btDismiss = view?.findViewById(R.id.bt_back_to_quiz)
        btUpload = view?.findViewById(R.id.bt_upload)
        btCancel = view?.findViewById(R.id.bt_cancel)
        deckName = view?.findViewById(R.id.tv_deck_name)

        (tvCategory as? MaterialAutoCompleteTextView)?.setSimpleItems(OpenTriviaQuizCategoryHelper().getCategories())
        (tvDifficulty as? MaterialAutoCompleteTextView)?.setSimpleItems(OpenTriviaQuizCategoryHelper().getDifficulty())
        (tvType as? MaterialAutoCompleteTextView)?.setSimpleItems(OpenTriviaQuizCategoryHelper().getType())

        btDismiss?.setOnClickListener { dismiss() }
        btUpload?.setOnClickListener {
            val questionSum = tvNumber?.text
            if (questionSum.isNullOrEmpty() || questionSum.toString().toInt() < 5) {
                tvNumber?.error = "Questions must be 5 or more for a better experience."
            } else {
                val settingsModel =
                    OpenTriviaQuizCategoryHelper().selectCategory(tvCategory?.text.toString())
                        ?.let { it1 ->
                            OpenTriviaQuizModel(
                                deckName = deckName?.text.toString(),
                                number = questionSum.toString().toInt(),
                                category = it1,
                                difficulty = OpenTriviaQuizCategoryHelper().encodeDifficulty(
                                    tvDifficulty?.text.toString()
                                ).lowercase(),
                                type = OpenTriviaQuizCategoryHelper().decodeType(tvType?.text.toString())
                            )
                        }
                if (settingsModel != null) {
                    sendOpenTriviaQuizMode(DeckFragment.REQUEST_CODE, settingsModel)
                }
                dismiss()
            }
        }
        btCancel?.setOnClickListener { dismiss() }

        builder.setView(view)
        return builder.create()
    }


    private fun sendOpenTriviaQuizMode(
        requestCode: String,
        openTriviaQuizModel: OpenTriviaQuizModel
    ) {
        parentFragmentManager.setFragmentResult(requestCode, bundleOf(OPEN_TRIVIA_QUIZ_MODEL_BUNDLE_KEY to openTriviaQuizModel))
    }

}