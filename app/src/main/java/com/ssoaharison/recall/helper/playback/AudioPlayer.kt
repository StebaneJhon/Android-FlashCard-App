package com.example.protoypehierarchicaldb.helper.playback

import java.io.File


interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
    fun pause()
    fun play()
    fun isPlaying(): Boolean
    fun hasPlayed(): Boolean
    fun getDuration(): Float
    fun getCurrentPosition(): Float
    fun onCompletion(listener: () -> Unit)
}