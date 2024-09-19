package com.example.flashcard.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.flashcard.R
import com.example.flashcard.databinding.LyFlashCardBottomSheetMenuBinding
import com.example.flashcard.util.FlashCardMiniGameRef.CARD_ORIENTATION_BACK_AND_FRONT
import com.example.flashcard.util.FlashCardMiniGameRef.CHECKED_CARD_ORIENTATION
import com.example.flashcard.util.FlashCardMiniGameRef.CHECKED_FILTER
import com.example.flashcard.util.FlashCardMiniGameRef.FILTER_BY_LEVEL
import com.example.flashcard.util.FlashCardMiniGameRef.FILTER_CREATION_DATE
import com.example.flashcard.util.FlashCardMiniGameRef.FILTER_RANDOM
import com.example.flashcard.util.FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF
import com.example.flashcard.util.FlashCardMiniGameRef.CARD_ORIENTATION_FRONT_AND_BACK
import com.example.flashcard.util.FlashCardMiniGameRef.IS_UNKNOWN_CARD_FIRST
import com.example.flashcard.util.FlashCardMiniGameRef.IS_UNKNOWN_CARD_ONLY
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MiniGameSettingsSheet: BottomSheetDialogFragment() {

    private var isFilterSectionRevealed = false
    private var isSpaceRepetitionSectionRevealed = false
    private var isCardOrientationSectionRevealed = false

    private lateinit var binding: LyFlashCardBottomSheetMenuBinding
    private var flashCardMiniGamePref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var listener: SettingsApplication? = null

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    interface SettingsApplication {
        fun onSettingsApplied()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LyFlashCardBottomSheetMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        flashCardMiniGamePref = activity?.getSharedPreferences(FLASH_CARD_MINI_GAME_REF, Context.MODE_PRIVATE)
        editor = flashCardMiniGamePref?.edit()
        initSettingState()

        /*
        val activity = requireActivity()
        flashCardViewModel = ViewModelProvider(activity)[FlashCardGameViewModel::class.java]

         */

       binding.btRevealFilterSettings.setOnClickListener {
           isFilterSectionRevealed(!isFilterSectionRevealed)
       }
        binding.btRevealCardOrientationSettings.setOnClickListener {
            isCardOrientationSectionRevealed(!isCardOrientationSectionRevealed)
        }

        binding.btRevealSpaceRepetitionSettings.setOnClickListener {
            isSpaceRepetitionSectionRevealed(!isSpaceRepetitionSectionRevealed)
        }

        binding.rlFilterTitleSection.setOnClickListener {
            isFilterSectionRevealed(!isFilterSectionRevealed)
        }

        binding.rlCardOrientationTitleSection.setOnClickListener {
            isCardOrientationSectionRevealed(!isCardOrientationSectionRevealed)
        }

        binding.rlSpaceRepetitionTitleSection.setOnClickListener {
            isSpaceRepetitionSectionRevealed(!isSpaceRepetitionSectionRevealed)
        }

        binding.rgRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.rb_random -> {
                    selectFilter(FILTER_RANDOM)
                }
                R.id.rb_creation_date -> {
                    selectFilter(FILTER_CREATION_DATE)
                }
                R.id.rb_filter_by_lv -> {
                    selectFilter(FILTER_BY_LEVEL)
                }
            }
        }

        binding.rgCardOrientationSection.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_front_back) {
                selectCardOrientation(CARD_ORIENTATION_FRONT_AND_BACK)
            } else {
                selectCardOrientation(CARD_ORIENTATION_BACK_AND_FRONT)
            }
        }

        binding.btApplyRestartFlashCardMiniGame.setOnClickListener {
            listener?.onSettingsApplied()
            dismiss()
        }

        binding.cbUnknownCardOnly.setOnCheckedChangeListener { _, isChecked ->
            isUnknownCardOnlyBoxChecked(isChecked)
        }
        binding.cbUnknownCardFirst.setOnCheckedChangeListener { _, isChecked ->
            isUnknownCardFirstBoxChecked(isChecked)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as SettingsApplication
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    private fun initSettingState() {

        isFilterSectionRevealed(false)
        isCardOrientationSectionRevealed(false)
        isSpaceRepetitionSectionRevealed(false)

        val checkedFilter = flashCardMiniGamePref?.getString(CHECKED_FILTER, FILTER_RANDOM)
        val checkedCardOrientation = flashCardMiniGamePref?.getString(
            CHECKED_CARD_ORIENTATION,
            CARD_ORIENTATION_FRONT_AND_BACK
        )
        val cbUnknownCardOnlyCheckState =
            flashCardMiniGamePref?.getBoolean(IS_UNKNOWN_CARD_ONLY, false)
        val cbUnknownCardFirstCheckState =
            flashCardMiniGamePref?.getBoolean(IS_UNKNOWN_CARD_FIRST, true)
        checkedFilter?.apply {
            selectFilter(this)
        }
        checkedCardOrientation?.apply {
            selectCardOrientation(this)
        }
        cbUnknownCardFirstCheckState?.apply {
            isUnknownCardFirstBoxChecked(this)
        }
        cbUnknownCardOnlyCheckState?.apply {
            isUnknownCardOnlyBoxChecked(this)
        }
    }

    private fun isUnknownCardFirstBoxChecked(checkedState: Boolean) {
        binding.cbUnknownCardFirst.isChecked = checkedState
        editor?.apply {
            putBoolean(IS_UNKNOWN_CARD_FIRST, checkedState)
            apply()
        }
    }

    private fun isUnknownCardOnlyBoxChecked(checkedState: Boolean) {
        binding.cbUnknownCardOnly.isChecked = checkedState
        editor?.apply {
            putBoolean(IS_UNKNOWN_CARD_ONLY, checkedState)
            apply()
        }
    }

    private fun selectCardOrientation(orientation: String) {
        if (orientation == CARD_ORIENTATION_FRONT_AND_BACK) {
            binding.rbFrontBack.isChecked = true
        } else {
            binding.rbBackFront.isChecked = true
        }
        editor?.apply {
            putString(CHECKED_CARD_ORIENTATION, orientation)
            apply()
        }
    }

    private fun selectFilter(filter: String) {
        when(filter) {
            FILTER_RANDOM -> {
                binding.rbRandom.isChecked = true
            }
            FILTER_CREATION_DATE -> {
                binding.rbCreationDate.isChecked = true
            }
            FILTER_BY_LEVEL -> {
                binding.rbFilterByLv.isChecked = true
            }
        }
        editor?.apply {
            putString(CHECKED_FILTER, filter)
            apply()
        }
    }

    private fun isFilterSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            binding.btRevealFilterSettings.setImageResource(R.drawable.icon_expand_less)
        } else {
            binding.btRevealFilterSettings.setImageResource(R.drawable.icon_expand_more)
        }
        binding.rgRadioGroup.isVisible = isRevealed
        isFilterSectionRevealed = isRevealed
    }

    private fun isSpaceRepetitionSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            binding.btRevealSpaceRepetitionSettings.setImageResource(R.drawable.icon_expand_less)
        } else {
            binding.btRevealSpaceRepetitionSettings.setImageResource(R.drawable.icon_expand_more)
        }
        binding.cbUnknownCardOnly.isVisible = isRevealed
        binding.cbUnknownCardFirst.isVisible = isRevealed
        isSpaceRepetitionSectionRevealed = isRevealed
    }

    private fun isCardOrientationSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            binding.btRevealCardOrientationSettings.setImageResource(R.drawable.icon_expand_less)
        } else {
            binding.btRevealCardOrientationSettings.setImageResource(R.drawable.icon_expand_more)
        }
        binding.rgCardOrientationSection.isVisible = isRevealed
        isCardOrientationSectionRevealed = isRevealed
    }

}