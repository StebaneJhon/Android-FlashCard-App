package com.soaharisonstebane.mneme.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.backend.models.ExternalCardWithContentAndDefinitions
import com.soaharisonstebane.mneme.databinding.DialogExportDeckBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import kotlinx.coroutines.launch

class ExportDeckDialog: DialogFragment() {

    private var _binding: DialogExportDeckBinding? = null
    private val binding get() = _binding!!

    private lateinit var fileFormat: String
    private lateinit var fileContentSeparator: String
    private val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    private val exportDeckViewModel: ExportDeckViewModel by viewModels()

    private val cards: List<ExternalCardWithContentAndDefinitions> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelableArrayList(ARG_CARDS, ExternalCardWithContentAndDefinitions::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelableArrayList<ExternalCardWithContentAndDefinitions>(ARG_CARDS)!!
        }
    }

    private val areMoreCards: Boolean by lazy {
        requireArguments().getBoolean(ARG_ARE_MORE_CARDS)
    }

    companion object {
        const val EXPORT_DECK_BUNDLE_KEY = "100"
        const val ARG_CARDS = "arg_cards"
        const val ARG_ARE_MORE_CARDS = "arg_are_more_cards"

        fun newInstance(cards: List<ExternalCardWithContentAndDefinitions>, areMoreCards: Boolean) = ExportDeckDialog().apply {
            arguments = bundleOf(ARG_CARDS to ArrayList(cards), ARG_ARE_MORE_CARDS to areMoreCards)
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

        binding.tvAreMoreCards.isVisible = areMoreCards

        exportDeckViewModel.setPreviewText(cards, ":")

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                exportDeckViewModel.uiState.collect { uiState ->
                    binding.tvPreviewText.text =  uiState.preview
                }
            }
        }

        binding.tieSeparator.addTextChangedListener { text ->
            exportDeckViewModel.onSeparatorChanged(text.toString(), cards)
        }

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
