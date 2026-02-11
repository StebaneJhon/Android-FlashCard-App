package com.soaharisonstebane.mneme.card

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.DialogTranslationOptionsBinding

class TranslationOptionsDialog: DialogFragment() {

    private var _binding: DialogTranslationOptionsBinding? = null
    private val binding get() = _binding!!

    private val options: List<String> by lazy {
        arguments?.getStringArrayList(ARG_OPTIONS) ?: emptyList()
    }

    private lateinit var translationOptionsRecyclerViewAdapter: TranslationOptionsRecyclerViewAdapter

    companion object {

        const val TAG = "TranslationOptionsDialog"
        const val TRANSLATION_OPTIONS_BUNDLE_KEY = "8"
        const val ARG_OPTIONS = "options"

        fun newInstance(options: List<String>) = TranslationOptionsDialog().apply {
            arguments = bundleOf(ARG_OPTIONS to options)
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogTranslationOptionsBinding.inflate(layoutInflater)
        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        translationOptionsRecyclerViewAdapter = TranslationOptionsRecyclerViewAdapter(options) {
            onChoseOption(it)
            dismiss()
        }

        binding.rvOptions.apply {
            adapter = translationOptionsRecyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        builder.setView(binding.root)
        return builder.create()
    }

    private fun onChoseOption(option: String) {
        parentFragmentManager.setFragmentResult(
            NewCardDialog.REQUEST_CODE_TRANSLATION_OPTION,
            bundleOf(TRANSLATION_OPTIONS_BUNDLE_KEY to option)
        )
    }

}