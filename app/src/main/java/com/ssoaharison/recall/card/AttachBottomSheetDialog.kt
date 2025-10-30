package com.ssoaharison.recall.card

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssoaharison.recall.databinding.BottomSheetAttachBinding
import com.ssoaharison.recall.util.AttachRef.ATTACH_AUDIO_RECORD
import com.ssoaharison.recall.util.AttachRef.ATTACH_IMAGE_FROM_CAMERA
import com.ssoaharison.recall.util.AttachRef.ATTACH_IMAGE_FROM_GALERI

class AttachBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAttachBinding

    companion object {
        const val TAG = "AttachBottomSheetDialog"
        const val ATTACH_BUNDLE_KEY = "5"
        const val ATTACH_REQUEST_CODE = "500"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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