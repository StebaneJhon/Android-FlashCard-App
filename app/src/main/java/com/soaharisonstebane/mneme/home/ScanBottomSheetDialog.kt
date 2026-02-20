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
import com.soaharisonstebane.mneme.databinding.BottomSheetScanBinding
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModel
import com.soaharisonstebane.mneme.mainActivity.DeckPathViewModelFactory
import com.soaharisonstebane.mneme.util.ScanRef.AUDIO_TO_TEXT
import com.soaharisonstebane.mneme.util.ScanRef.IMAGE_FROM_CAMERA_TO_TEXT
import com.soaharisonstebane.mneme.util.ScanRef.IMAGE_FROM_GALERI_TO_TEXT
import kotlin.getValue

class ScanBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetScanBinding

    val deckPathViewModel: DeckPathViewModel by activityViewModels {
        val repository = (requireActivity().application as FlashCardApplication).repository
        DeckPathViewModelFactory(repository)
    }

    companion object {
        const val TAG = "AttachBottomSheetDialog"
        const val SCAN_BUNDLE_KEY = "6"
        const val SCAN_REQUEST_CODE = "600"
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
    ): View? {
        binding = BottomSheetScanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btAudioToText.setOnClickListener {
            onScan(AUDIO_TO_TEXT)
        }
        binding.btPhotoFormGaleriToText.setOnClickListener {
            onScan(IMAGE_FROM_GALERI_TO_TEXT)
        }
        binding.btPhotoFromCameraToText.setOnClickListener {
            onScan(IMAGE_FROM_CAMERA_TO_TEXT)
        }

    }

    private fun onScan(attach: String) {
        parentFragmentManager.setFragmentResult(SCAN_REQUEST_CODE,
            bundleOf(SCAN_BUNDLE_KEY to attach)
        )
        dismiss()
    }

}