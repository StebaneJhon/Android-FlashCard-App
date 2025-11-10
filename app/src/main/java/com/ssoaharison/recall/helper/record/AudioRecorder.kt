package com.example.protoypehierarchicaldb.helper.record

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
    fun isRecording(): Boolean
}