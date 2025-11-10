package com.example.protoypehierarchicaldb.helper.playback

import java.io.File


interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
    fun isPlaying(): Boolean
}