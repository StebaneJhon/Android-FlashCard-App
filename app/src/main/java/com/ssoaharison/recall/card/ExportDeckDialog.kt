package com.ssoaharison.recall.card

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.DialogExportDeckBinding

class ExportDeckDialog: DialogFragment() {

    companion object {
        const val EXPORT_DECK_BUNDLE_KEY = "100"
    }

    private var _binding: DialogExportDeckBinding? = null
    private val binding get() = _binding!!

    lateinit var fileFormat: String
    lateinit var fileContentSeparator: String

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogExportDeckBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        binding.btExport.setOnClickListener {
            fileFormat = getFormat()
            fileContentSeparator = if (!binding.tieSeparator.text.isNullOrBlank()) { binding.tieSeparator.text.toString() } else ":"

            val deckExportModel = DeckExportModel(fileFormat, fileContentSeparator, binding.swIncludeSubdecks.isChecked)
            sendDeckExportModel(CardFragment.REQUEST_EXPORT_DECK_CODE, deckExportModel)
        }

        binding.btCancel.setOnClickListener {
            this.dismiss()
        }

        builder.setView(binding.root)
        return builder.create()
    }

    fun getFormat() = when(binding.rgFormat.checkedRadioButtonId) {
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