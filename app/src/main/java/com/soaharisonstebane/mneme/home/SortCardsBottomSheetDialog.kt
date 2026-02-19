package com.soaharisonstebane.mneme.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.BottomSheetDialogSortCardsPrefBinding
import com.soaharisonstebane.mneme.util.CardSortOptions.SORT_CARD_ALPHABETICALLY
import com.soaharisonstebane.mneme.util.CardSortOptions.SORT_CARD_BY_CREATION_DATE
import com.soaharisonstebane.mneme.util.CardSortOptions.SORT_CARD_BY_LEVEL

class SortCardsBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetDialogSortCardsPrefBinding
    private val actualSortCardsPref: String by lazy {
        requireArguments().getString(ARG_SORT_CARD_PREF).orEmpty()
    }

    companion object {
        const val TAG = "SortCardsBottomSheetDialog"
        const val SORT_CARD_PREF_REQUEST_CODE = "900"
        const val SORT_CARD_PREF_BUNDLE_KEY = "9"
        private const val ARG_SORT_CARD_PREF = "sort_card_pref"
        fun newInstance(actualSortCardsPref: String) = SortCardsBottomSheetDialog().apply {
            arguments = bundleOf(ARG_SORT_CARD_PREF to actualSortCardsPref)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDialogSortCardsPrefBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (actualSortCardsPref) {
            SORT_CARD_BY_CREATION_DATE -> binding.rbCardSortByCreationDate.isChecked = true
            SORT_CARD_BY_LEVEL -> binding.rbCardSortByCardLevel.isChecked = true
            SORT_CARD_ALPHABETICALLY -> binding.rbCardSortAlphabetically.isChecked = true
            else -> binding.rbCardSortByCreationDate.isChecked = true
        }
        binding.rgCardSortPref.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_card_sort_by_creation_date -> {
                    onSortCards(SORT_CARD_BY_CREATION_DATE)
                }
                R.id.rb_card_sort_by_card_level -> {
                    onSortCards(SORT_CARD_BY_LEVEL)
                }
                R.id.rb_card_sort_alphabetically -> {
                    onSortCards(SORT_CARD_ALPHABETICALLY)
                }
            }
        }
    }

    private fun onSortCards(sortCardsPref: String) {
        parentFragmentManager.setFragmentResult(SORT_CARD_PREF_REQUEST_CODE, bundleOf(SORT_CARD_PREF_BUNDLE_KEY to sortCardsPref))
        dismiss()
    }

}