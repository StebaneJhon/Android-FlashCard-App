package com.example.flashcard.quiz.flashCardGame

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.flashcard.R
import com.example.flashcard.databinding.LyFlashCardBottomSheetMenuBinding
import com.example.flashcard.deck.NewDeckDialog
import com.example.flashcard.util.FlashCardMiniGameRef.CHECKED_FILTER
import com.example.flashcard.util.FlashCardMiniGameRef.FILTER_BY_LEVEL
import com.example.flashcard.util.FlashCardMiniGameRef.FILTER_CREATION_DATE
import com.example.flashcard.util.FlashCardMiniGameRef.FILTER_RANDOM
import com.example.flashcard.util.FlashCardMiniGameRef.FLASH_CARD_MINI_GAME_REF
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FlashCardGameSettingsSheet: BottomSheetDialogFragment() {

    private var isFilterSectionRevealed = false
    private var isSpaceRepetitionSectionRevealed = false
    private var isCardOrientationSectionRevealed = false

    private lateinit var binding: LyFlashCardBottomSheetMenuBinding
    private lateinit var flashCardViewModel: FlashCardGameViewModel

    private var flashCardMiniGamePref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var listener: FlashCardGameSettingsSheet.BottomSheetDismissListener? = null

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
        val checkedFilter = flashCardMiniGamePref?.getString(CHECKED_FILTER, FILTER_RANDOM)
        checkedFilter?.apply {
            selectFilter(this)
        }

        val activity = requireActivity()
        flashCardViewModel = ViewModelProvider(activity)[FlashCardGameViewModel::class.java]

        isFilterSectionRevealed(false)
        isCardOrientationSectionRevealed(false)
        isSpaceRepetitionSectionRevealed(false)

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

        binding.rgRadioGroup.setOnCheckedChangeListener { group, checkedId ->
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

        binding.btApplyRestartFlashCardMiniGame.setOnClickListener {
            listener?.onBottomSheetDismissed()
            dismiss()
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onBottomSheetDismissed()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as BottomSheetDismissListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    interface BottomSheetDismissListener {
        fun onBottomSheetDismissed()
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
            binding.btRevealFilterSettings.setImageResource(R.drawable.icone_expand_more)
        }
        binding.rgRadioGroup.isVisible = isRevealed
        isFilterSectionRevealed = isRevealed
    }

    private fun isSpaceRepetitionSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            binding.btRevealSpaceRepetitionSettings.setImageResource(R.drawable.icon_expand_less)
        } else {
            binding.btRevealSpaceRepetitionSettings.setImageResource(R.drawable.icone_expand_more)
        }
        binding.cbUnknownCardOnly.isVisible = isRevealed
        binding.cbUnknownCardFirst.isVisible = isRevealed
        isSpaceRepetitionSectionRevealed = isRevealed
    }

    private fun isCardOrientationSectionRevealed(isRevealed: Boolean) {
        if (isRevealed) {
            binding.btRevealCardOrientationSettings.setImageResource(R.drawable.icon_expand_less)
        } else {
            binding.btRevealCardOrientationSettings.setImageResource(R.drawable.icone_expand_more)
        }
        binding.rgCardOrientationSection.isVisible = isRevealed
        isCardOrientationSectionRevealed = isRevealed
    }




    companion object {
        const val TAG = "ModalBottomSheet"
    }

}