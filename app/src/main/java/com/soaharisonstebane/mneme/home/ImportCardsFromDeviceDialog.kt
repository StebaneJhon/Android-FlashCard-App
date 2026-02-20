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
import com.soaharisonstebane.mneme.databinding.DialogImportCardsBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import kotlin.getValue

class ImportCardsFromDeviceDialog: DialogFragment() {

    companion object {
        const val EXPORT_CARD_FROM_DEVICE_BUNDLE_KEY = "500"
    }

    private var _binding: DialogImportCardsBinding? = null
    private val binding get() = _binding!!

    lateinit var fileFormat: String
    lateinit var fileContentSeparator: String

    private val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val contextThemeWrapper = ContextThemeWrapper(requireActivity(), deckPathViewModel.getViewTheme())
        val themedInflater = LayoutInflater.from(contextThemeWrapper)

        _binding = DialogImportCardsBinding.inflate(themedInflater)

        val builder = MaterialAlertDialogBuilder(
            contextThemeWrapper,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        builder.setView(binding.root)
            .setNegativeButton(R.string.bt_text_cancel) {dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.search_file) {_, _ ->
                fileFormat = getFormat()
                fileContentSeparator = if (!binding.tieSeparator.text.isNullOrBlank()) { binding.tieSeparator.text.toString() } else ":"
                val cardImportFromDeviceModel = CardImportFromDeviceModel(fileFormat, fileContentSeparator)
                sendCardImportFromDeviceModel(NewCardDialog.REQUEST_CODE_IMPORT_CARD_FROM_DEVICE_SOURCE, cardImportFromDeviceModel)
            }
        return builder.create()
    }

    fun getFormat() = when(binding.rgFormat.checkedRadioButtonId) {
        R.id.rb_format_txt -> ".txt"
        else -> ".txt"
    }

    private fun sendCardImportFromDeviceModel(
        requestCode: String,
        deckImportFromDeviceModel: CardImportFromDeviceModel
    ) {
        parentFragmentManager.setFragmentResult(
            requestCode,
            bundleOf(EXPORT_CARD_FROM_DEVICE_BUNDLE_KEY to deckImportFromDeviceModel)
        )
        this.dismiss()
    }

}