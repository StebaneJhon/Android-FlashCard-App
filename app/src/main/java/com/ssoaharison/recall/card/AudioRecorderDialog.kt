package com.ssoaharison.recall.card

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.DialogAudioRecorderBinding
import com.ssoaharison.recall.helper.AudioModel
import com.ssoaharison.recall.helper.record.AndroidAudioRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID


class AudioRecorderDialog: DialogFragment() {

    private var _binding: DialogAudioRecorderBinding? = null
    private val binding get() = _binding!!

    private val audioRecorderViewModel: AudioRecorderViewModel by viewModels()

    private var recorderJob: Job? = null
    private var streamTimeJob: Job? = null

    private var newAudioModel: AudioModel? = null

    val audioName = "${UUID.randomUUID()}.mp3"

    private val recorder by lazy {
        AndroidAudioRecorder(requireContext())
    }

    companion object {
        const val AUDIO_RECORDER_BUNDLE_KEY = "100"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAudioRecorderBinding.inflate(layoutInflater)

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioRecorderViewModel.time.collect { time ->
                    binding.tvTimer.text = time
                    binding.waveformView.addAmplitude(recorder.getMaxAmplitude())
                }
            }
        }

        binding.btPlayPause.setOnClickListener {
            when {
                recorder.isPaused() -> onResumeRecorder()
                recorder.isRecording() -> onPauseRecorder()
                else -> onStartRecorder()
            }
        }
        binding.btDone.setOnClickListener {
            onStopRecorder()
            onDone(AudioModel(audioName, audioRecorderViewModel.getAudioDuration()))
            dismiss()
        }
        binding.btCancel.setOnClickListener {
            onStopRecorder()
            File(audioName).delete()
            dismiss()
        }

        builder.setView(binding.root)
        return builder.create()
    }

    private fun onStartRecorder() {
        lifecycleScope.launch {

            File(requireContext().filesDir, audioName).also {
                recorder.start(it)
//                newAudioModel = AudioModel(it, audioRecorderViewModel.getAudioDuration())
            }
            binding.btPlayPause.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.icon_pause)
            audioRecorderViewModel.startTimer()
        }
    }

    private fun onStopRecorder() {
        binding.btPlayPause.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.icon_play)
        recorder.stop()
        audioRecorderViewModel.stopTimer()
        recorderJob?.cancel()
    }

    private fun onPauseRecorder() {
        recorder.pause()
        binding.btPlayPause.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.icon_play)
        audioRecorderViewModel.pauseTimer()
    }

    private fun onResumeRecorder() {
        lifecycleScope.launch {
            binding.btPlayPause.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.icon_pause)
            recorder.resume()
            audioRecorderViewModel.startTimer()
        }
    }

    private fun onDone(audioModel: AudioModel?) {
        parentFragmentManager.setFragmentResult(
            NewCardDialog.REQUEST_CODE_AUDIO_RECORDER,
            bundleOf(AUDIO_RECORDER_BUNDLE_KEY to audioModel)
        )
    }

}