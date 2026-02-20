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
import com.soaharisonstebane.mneme.backend.FlashCardApplication
import com.soaharisonstebane.mneme.databinding.BottomSheetAttachBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import com.soaharisonstebane.mneme.util.AttachRef.ATTACH_AUDIO_RECORD
import com.soaharisonstebane.mneme.util.AttachRef.ATTACH_IMAGE_FROM_CAMERA
import com.soaharisonstebane.mneme.util.AttachRef.ATTACH_IMAGE_FROM_GALERI
import kotlin.getValue

class AttachBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAttachBinding

    val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    companion object {
        const val TAG = "AttachBottomSheetDialog"
        const val ATTACH_BUNDLE_KEY = "5"
        const val ATTACH_REQUEST_CODE = "500"
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
        binding = BottomSheetAttachBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btRecordAudio.setOnClickListener {
            onAttach(ATTACH_AUDIO_RECORD)
        }
        binding.btPickAPhoto.setOnClickListener {
            onAttach(ATTACH_IMAGE_FROM_GALERI)
        }
        binding.btTakeAPhoto.setOnClickListener {
            onAttach(ATTACH_IMAGE_FROM_CAMERA)
        }

    }

    private fun onAttach(attach: String) {
        parentFragmentManager.setFragmentResult(ATTACH_REQUEST_CODE,
            bundleOf(ATTACH_BUNDLE_KEY to attach)
        )
        dismiss()
    }

}