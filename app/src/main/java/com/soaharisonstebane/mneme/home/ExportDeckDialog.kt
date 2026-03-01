package com.soaharisonstebane.mneme.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.databinding.DialogExportDeckBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import kotlinx.coroutines.launch

class ExportDeckDialog: DialogFragment() {

    private var _binding: DialogExportDeckBinding? = null
    private val binding get() = _binding!!
    private val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    private val exportDeckViewModel: ExportDeckViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, ExportDeckViewModelFactory(repository))[ExportDeckViewModel::class.java]
    }



    private val deckId: String by lazy {
        requireArguments().getString(ARG_DECK_ID)!!
    }


    companion object {
        const val SEPARATOR_BUNDLE_KEY = "100"
        const val CARDS_TO_BE_EXPORTED_BUNDLE_KEY = "1000"
        const val ARG_DECK_ID = "arg_deck_id"

        fun newInstance(deckId: String) = ExportDeckDialog().apply {
            arguments = bundleOf(ARG_DECK_ID to deckId)
        }

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

        binding.inWarning.tvWarning.text = getString(R.string.warning_export_deck)


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                exportDeckViewModel.initCardsToExport(requireContext(), deckId)
                exportDeckViewModel.uiState.collect { uiState ->
                    binding.tvPreviewText.text =  uiState.preview
                    binding.swIncludeSubdecks.isChecked = uiState.includeSubdecks
                }
            }
        }

        binding.tieSeparator.addTextChangedListener { text ->
            exportDeckViewModel.onSeparatorChanged(text.toString())
        }

        binding.swIncludeSubdecks.setOnCheckedChangeListener { _, isChecked ->
            exportDeckViewModel.onIncludeSubdecksChanged(requireContext(), deckId, isChecked)
        }

        builder.setView(binding.root)
            .setPositiveButton(R.string.export) { dialog, _ ->
                sendCardsAndSeparator()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.bt_text_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    private fun sendCardsAndSeparator() {
        parentFragmentManager.setFragmentResult(
            CardFragment.REQUEST_EXPORT_DECK_CODE,
            bundleOf(
                SEPARATOR_BUNDLE_KEY to exportDeckViewModel.uiState.value.separator,
                CARDS_TO_BE_EXPORTED_BUNDLE_KEY to exportDeckViewModel.cards.value,
            )
        )
    }

}
