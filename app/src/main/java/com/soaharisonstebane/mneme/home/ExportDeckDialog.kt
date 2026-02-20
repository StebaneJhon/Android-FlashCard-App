package com.soaharisonstebane.mneme.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.databinding.DialogExportDeckBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory

class ExportDeckDialog: DialogFragment() {

    companion object {
        const val EXPORT_DECK_BUNDLE_KEY = "100"
    }

    private var _binding: DialogExportDeckBinding? = null
    private val binding get() = _binding!!

    private lateinit var fileFormat: String
    private lateinit var fileContentSeparator: String
    private val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val contextThemeWrapper = ContextThemeWrapper(requireActivity(), deckPathViewModel.getViewTheme())
        val themedInflater = LayoutInflater.from(contextThemeWrapper)

        _binding = DialogExportDeckBinding.inflate(themedInflater)

        val builder = MaterialAlertDialogBuilder(
            contextThemeWrapper,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        builder.setView(binding.root)
            .setPositiveButton(R.string.export) { _, _ ->
                fileFormat = getFormat()
                fileContentSeparator = if (!binding.tieSeparator.text.isNullOrBlank()) { binding.tieSeparator.text.toString() } else ":"

                val deckExportModel = DeckExportModel(fileFormat, fileContentSeparator, binding.swIncludeSubdecks.isChecked)
                sendDeckExportModel(CardFragment.REQUEST_EXPORT_DECK_CODE, deckExportModel)
            }
            .setNegativeButton(R.string.bt_text_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    private fun getFormat() = when(binding.rgFormat.checkedRadioButtonId) {
        R.id.rb_format_txt -> ".txt"
        else -> ".txt"
    }

    private fun sendDeckExportModel(
        requestCode: String,
        deckExportModel: DeckExportModel
    ) {
        parentFragmentManager.setFragmentResult(
            requestCode,
            bundleOf(EXPORT_DECK_BUNDLE_KEY to deckExportModel)
        )
        this.dismiss()
    }

}