package com.ssoaharison.recall.card

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ssoaharison.recall.R

class EditDeckNameDialog(): AppCompatDialogFragment() {

    private var deckNameField: TextInputEditText? = null
    private var deckNameLayout: TextInputLayout? = null

    private val currentDeckName: String by lazy {
        requireArguments().getString(ARG_CURRENT_DECK_NAME)!!
    }

    companion object {
        const val TAG = "EditDeckNameDialog"
        const val ARG_CURRENT_DECK_NAME = "current_deck_name"
        const val CURRENT_DECK_NAME_REQUEST_CODE = "1200"
        const val CURRENT_DECK_NAME_BUNDLE_CODE = "12"

        fun newInstance(currentDeckName: String) = EditDeckNameDialog().apply {
            arguments = bundleOf(ARG_CURRENT_DECK_NAME to currentDeckName)
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edit_deck_name, null)

        deckNameField = view?.findViewById(R.id.tie_deck_name)
        deckNameLayout = view?.findViewById(R.id.til_deck_name)

        deckNameField?.setText(currentDeckName)

        builder.setView(view)
            .setTitle(getString(R.string.edit_deck_name))
            .setNegativeButton(getString(R.string.bt_text_cancel)) {_, _ -> dismiss()}
            .setPositiveButton(getString(R.string.ok)) {_, _ ->
                val newDeckName = deckNameField?.text.toString()
                if (newDeckName.isEmpty() || newDeckName.isBlank()) {
                    deckNameLayout?.error = getString(R.string.error_message_on_missing_deck_name)
                } else {
                    onUpdateDeckName(newDeckName)
                }
            }
        return builder.create()
    }

    private fun onUpdateDeckName(deckName: String) {
        parentFragmentManager.setFragmentResult(CURRENT_DECK_NAME_REQUEST_CODE, bundleOf(CURRENT_DECK_NAME_BUNDLE_CODE to deckName))
        dismiss()
    }

}