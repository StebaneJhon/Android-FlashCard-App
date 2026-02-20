package com.soaharisonstebane.mneme.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.databinding.BottomSheetDialogSortDecksPrefBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import com.soaharisonstebane.mneme.util.DeckRef.DECK_SORT_ALPHABETICALLY
import com.soaharisonstebane.mneme.util.DeckRef.DECK_SORT_BY_CARD_SUM
import com.soaharisonstebane.mneme.util.DeckRef.DECK_SORT_BY_CREATION_DATE
import kotlin.getValue

class SortDecksBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetDialogSortDecksPrefBinding

    private val actualSortDecksPref: String by lazy {
        requireArguments().getString(ARG_SORT_DECK_PREF).orEmpty()
    }

    val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    companion object {
        const val TAG = "SortDecksBottomSheetDialog"
        const val SORT_DECK_PREF_REQUEST_CODE = "1000"
        const val SORT_DECK_PREF_BUNDLE_KEY = "10"
        private const val ARG_SORT_DECK_PREF = "sort_card_pref"
        fun newInstance(actualSortDecksPref: String) = SortDecksBottomSheetDialog().apply {
            arguments = bundleOf(ARG_SORT_DECK_PREF to actualSortDecksPref)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewTheme = deckPathViewModel.getViewTheme()
        val contextThemeWrapper = ContextThemeWrapper(requireActivity(), viewTheme)
        return BottomSheetDialog(contextThemeWrapper)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDialogSortDecksPrefBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (actualSortDecksPref) {
            DECK_SORT_BY_CREATION_DATE -> binding.rbDeckSortByCreationDate.isChecked = true
            DECK_SORT_BY_CARD_SUM -> binding.rbDeckSortByCardSum.isChecked = true
            DECK_SORT_ALPHABETICALLY -> binding.rbDeckSortAlphabetically.isChecked = true
            else -> binding.rbDeckSortByCreationDate.isChecked = true
        }
        binding.rgDeckSortPref.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_deck_sort_by_creation_date -> {
                    onSortCards(DECK_SORT_BY_CREATION_DATE)
                }
                R.id.rb_deck_sort_by_card_sum -> {
                    onSortCards(DECK_SORT_BY_CARD_SUM)
                }
                R.id.rb_deck_sort_alphabetically -> {
                    onSortCards(DECK_SORT_ALPHABETICALLY)
                }
            }
        }
    }

    private fun onSortCards(sortCardsPref: String) {
        parentFragmentManager.setFragmentResult(SORT_DECK_PREF_REQUEST_CODE, bundleOf(SORT_DECK_PREF_BUNDLE_KEY to sortCardsPref))
        dismiss()
    }

}