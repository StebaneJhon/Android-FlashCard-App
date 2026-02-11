package com.soaharisonstebane.mneme.card

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.DialogImportCardsBinding

class ImportCardsFromDeviceDialog: DialogFragment() {

    companion object {
        const val EXPORT_CARD_FROM_DEVICE_BUNDLE_KEY = "500"
    }

    private var _binding: DialogImportCardsBinding? = null
    private val binding get() = _binding!!

    lateinit var fileFormat: String
    lateinit var fileContentSeparator: String

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogImportCardsBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        binding.btGetFile.setOnClickListener {
            fileFormat = getFormat()
            fileContentSeparator = if (!binding.tieSeparator.text.isNullOrBlank()) { binding.tieSeparator.text.toString() } else ":"
            val cardImportFromDeviceModel = CardImportFromDeviceModel(fileFormat, fileContentSeparator)
            sendCardImportFromDeviceModel(NewCardDialog.REQUEST_CODE_IMPORT_CARD_FROM_DEVICE_SOURCE, cardImportFromDeviceModel)
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