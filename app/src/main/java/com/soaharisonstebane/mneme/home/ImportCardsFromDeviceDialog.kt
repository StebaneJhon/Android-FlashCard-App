package com.soaharisonstebane.mneme.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.backend.entities.relations.CardWithContentAndDefinitions
import com.soaharisonstebane.mneme.databinding.DialogImportCardsBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import kotlinx.coroutines.launch
import kotlin.getValue

class ImportCardsFromDeviceDialog: DialogFragment() {

    private var _binding: DialogImportCardsBinding? = null
    private val binding get() = _binding!!

    private val fileUri: Uri by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(ARG_FILE_URI, Uri::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable<Uri>(ARG_FILE_URI)!!
        }
    }


    private val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    private val importCardsFromDeviceViewModel: ImportCardsFromDeviceViewModel by viewModels()

    companion object {
        const val TAG = "ImportCardsFromDeviceDialog"
        const val ARG_FILE_URI = "file_uri"
        const val CARDS_FROM_URI_REQUEST_CODE = "5000"
        const val CARDS_FROM_URI_BUNDLE_CODE = "5000"

        fun newInstance(fileUri: Uri) = ImportCardsFromDeviceDialog().apply {
            arguments = bundleOf(ARG_FILE_URI to fileUri)
        }
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                importCardsFromDeviceViewModel.uiState.collect { uiState ->
                    if (uiState.cards.isNotEmpty()) {
                        val previewAdapter = ImportedCardsFronDevicePreviewRecyclerViewAdapter(uiState.cards)
                        binding.rvPreview.apply {
                            visibility = View.VISIBLE
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = previewAdapter
                            setHasFixedSize(true)
                        }
                    } else {
                        binding.rvPreview.visibility = View.GONE
                    }
                    importCardsFromDeviceViewModel.onSeparatorChanged(
                        separator = uiState.separator,
                        uri = fileUri,
                        deckId = deckPathViewModel.currentDeck.value.deckId,
                        context = requireContext(),
                    )
                }
            }
        }

        binding.tieSeparator.addTextChangedListener { text ->
            importCardsFromDeviceViewModel.updateSeparator(text.toString())
        }

        builder.setView(binding.root)
            .setNegativeButton(R.string.bt_text_cancel) {dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.search_file) {dialog, _ ->
                if (importCardsFromDeviceViewModel.uiState.value.cards.isEmpty()) {
                    dialog.dismiss()
                } else {
                    onImport(importCardsFromDeviceViewModel.uiState.value.cards)
                }
            }
        return builder.create()
    }

    private fun onImport(cards: List<CardWithContentAndDefinitions>) {
        parentFragmentManager.setFragmentResult(CARDS_FROM_URI_REQUEST_CODE, bundleOf(CARDS_FROM_URI_BUNDLE_CODE to cards))
        this.dismiss()
    }

}