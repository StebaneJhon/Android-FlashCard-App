package com.example.flashcard.quiz.flashCardGame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.flashcard.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FlashCardGameSettingsSheet: BottomSheetDialogFragment() {

    private var btRevealFilterSettings: ImageButton? = null
    private var btRevealSpaceRepetitionSettings: ImageButton? = null
    private var btRevealCardOrientationSettings: ImageButton? = null
    private var rgFilterSettings: RadioGroup? = null
    private var rgCardOrientationSettings: RadioGroup? = null
    private var cbUnknownCardFirst: CheckBox? = null
    private var cbUnknownCardOnly: CheckBox? = null
    var isFilterSectionRevealed = false
    var isSpaceRepetitionSectionRevealed = false
    var isCardOrientationSectionRevealed = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ly_flash_card_bottom_sheet_menu, container, false)

        btRevealFilterSettings = view.findViewById(R.id.bt_reveal_filter_settings)
        btRevealSpaceRepetitionSettings = view.findViewById(R.id.bt_reveal_space_repetition_settings)
        btRevealCardOrientationSettings = view.findViewById(R.id.bt_reveal_card_orientation_settings)
        rgFilterSettings = view.findViewById(R.id.rg_radioGroup)
        rgCardOrientationSettings = view.findViewById(R.id.rg_card_orientation_section)
        cbUnknownCardFirst = view.findViewById(R.id.cb_unknown_card_first)
        cbUnknownCardOnly = view.findViewById(R.id.cb_unknown_card_only)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isFilterSectionRevealed(false)
        isCardOrientationSectionRevealed(false)
        isSpaceRepetitionSectionRevealed(false)

       btRevealFilterSettings?.setOnClickListener {
           isFilterSectionRevealed(!isFilterSectionRevealed)
       }

        btRevealCardOrientationSettings?.setOnClickListener {
            isCardOrientationSectionRevealed(!isCardOrientationSectionRevealed)
        }

        btRevealSpaceRepetitionSettings?.setOnClickListener {
            isSpaceRepetitionSectionRevealed(!isSpaceRepetitionSectionRevealed)
        }

    }

    fun isFilterSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            btRevealFilterSettings?.setImageResource(R.drawable.icon_expand_less)
        } else {
            btRevealFilterSettings?.setImageResource(R.drawable.icone_expand_more)
        }
        rgFilterSettings?.isVisible = isRevealed
        isFilterSectionRevealed = isRevealed
    }

    fun isSpaceRepetitionSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            btRevealSpaceRepetitionSettings?.setImageResource(R.drawable.icon_expand_less)
        } else {
            btRevealSpaceRepetitionSettings?.setImageResource(R.drawable.icone_expand_more)
        }
        cbUnknownCardOnly?.isVisible = isRevealed
        cbUnknownCardFirst?.isVisible = isRevealed
        isSpaceRepetitionSectionRevealed = isRevealed
    }

    fun isCardOrientationSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            btRevealCardOrientationSettings?.setImageResource(R.drawable.icon_expand_less)
        } else {
            btRevealCardOrientationSettings?.setImageResource(R.drawable.icone_expand_more)
        }
        rgCardOrientationSettings?.isVisible = isRevealed
        isCardOrientationSectionRevealed = isRevealed
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

}