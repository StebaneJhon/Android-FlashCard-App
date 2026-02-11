package com.soaharisonstebane.mneme.card

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.DialogChooseCardsImportSourceBinding

class ImportCardsSourceDialog: DialogFragment() {

    private var _binding: DialogChooseCardsImportSourceBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val IMPORT_CARDS_SOURCE_BUNDLE_KEY = "200"
        const val IMPORT_FROM_DEVICE = "from device"
        const val IMPORT_FROM_OTHERS = "from others"
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChooseCardsImportSourceBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        binding.cvFromDevice.setOnClickListener {
            onImportFromClicked(NewCardDialog.REQUEST_CODE_CARD_IMPORT_SOURCE, IMPORT_FROM_DEVICE)
            dismiss()
        }

        binding.cvFromOtherSources.setOnClickListener {
            onImportFromClicked(NewCardDialog.REQUEST_CODE_CARD_IMPORT_SOURCE, IMPORT_FROM_OTHERS)
            dismiss()
        }

        binding.btDismiss.setOnClickListener {
            dismiss()
        }

        builder.setView(binding.root)
        return builder.create()
    }

    private fun onImportFromClicked(
        requestCode: String,
        importSource: String
    ) {
        parentFragmentManager.setFragmentResult(
            requestCode,
            bundleOf(IMPORT_CARDS_SOURCE_BUNDLE_KEY to importSource)
        )
    }

}