package com.ssoaharison.recall.helper.record

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.protoypehierarchicaldb.helper.record.AudioRecorder
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
): AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var isPaused: Boolean = false

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            isRecording = true
            isPaused = false
            recorder = this
        }
    }

    override fun pause() {
        isPaused = true
        recorder?.pause()
    }

    override fun resume() {
        isPaused = false
        recorder?.resume()
    }

    override fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
        isPaused = false
        isRecording = false
    }

    override fun isRecording(): Boolean {
        return isRecording
    }

    override fun isPaused(): Boolean {
        return isPaused
    }

    override fun getMaxAmplitude(): Float {
        return recorder?.maxAmplitude?.toFloat() ?: 0F
    }
}