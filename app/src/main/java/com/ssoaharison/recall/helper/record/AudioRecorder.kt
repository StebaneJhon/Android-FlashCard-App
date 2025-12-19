package com.example.protoypehierarchicaldb.helper.record

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun pause()
    fun resume()
    fun stop()
    fun isRecording(): Boolean
    fun isPaused(): Boolean
    fun getMaxAmplitude(): Float?
}