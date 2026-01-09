package com.ssoaharison.recall.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssoaharison.recall.backend.entities.Deck
import com.ssoaharison.recall.backend.models.ExternalDeck
import com.ssoaharison.recall.databinding.BottomSheetDialogCurrentDeckDetailsBinding
import com.ssoaharison.recall.deck.ColorModel
import com.ssoaharison.recall.util.DeckColorCategorySelector
import com.ssoaharison.recall.util.DeckColorPickerAdapter
import com.ssoaharison.recall.util.parcelable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class CurrentDeckDetailsBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetDialogCurrentDeckDetailsBinding

    private val currentDeck: ExternalDeck by lazy {
        requireArguments().parcelable<ExternalDeck>(ARG_CURRENT_DECK)!!
    }

    private val currentDeckDetailsViewModel: CurrentDeckDetailsViewModel by viewModels()

    private lateinit var deckColorPickerAdapter: DeckColorPickerAdapter



    companion object {
        const val TAG = "CurrentDeckDetailsBottomSheetDialog"
        const val ARG_CURRENT_DECK = "current_deck"
        const val CURRENT_DECK_DETAILS_REQUEST_CODE = "1100"
        const val CURRENT_DECK_DETAILS_BUNDLE_KEY = "11"
        fun newInstance(currentDeck: ExternalDeck) = CurrentDeckDetailsBottomSheetDialog().apply {
            arguments = bundleOf(ARG_CURRENT_DECK to currentDeck)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDialogCurrentDeckDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deckColorCategorySelector = DeckColorCategorySelector()

        lifecycleScope.launch {
            delay(50)
            currentDeckDetailsViewModel.initColorSelection(deckColorCategorySelector.getColors(), currentDeck.deckColorCode)
            currentDeckDetailsViewModel.colorSelectionList.collect { colors ->
                displayColorPicker(colors)
            }
        }

    }

    private fun displayColorPicker(colors: List<ColorModel>) {
        deckColorPickerAdapter = DeckColorPickerAdapter(
            context = requireContext(),
            listOfColors = colors,
            onColorClicked = { color ->
                currentDeckDetailsViewModel.selectColor(color.id)
                deckColorPickerAdapter.notifyDataSetChanged()
            }
        )
        binding.rvDeckColorPicker.apply {
            adapter = deckColorPickerAdapter
            layoutManager = GridLayoutManager(requireContext(), 6, GridLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun onUpdateCurrentDeck(updatedDeck: Deck) {
        parentFragmentManager.setFragmentResult(CURRENT_DECK_DETAILS_REQUEST_CODE, bundleOf(CURRENT_DECK_DETAILS_BUNDLE_KEY to updatedDeck))
        dismiss()
    }

}