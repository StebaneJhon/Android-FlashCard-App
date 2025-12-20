package com.ssoaharison.recall.helper.playback

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.example.protoypehierarchicaldb.helper.playback.AudioPlayer
import java.io.File

class AndroidAudioPlayer(
    private val context: Context
): AudioPlayer {

    private var player: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var hasPlayed: Boolean = false

    override fun playFile(file: File) {
        isPlaying = true
        hasPlayed = true
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
        isPlaying = false
        hasPlayed = false
    }

    override fun pause() {
        player?.pause()
        isPlaying = false
    }

    override fun play() {
        player?.start()
        isPlaying = true
    }

    override fun isPlaying(): Boolean {
        return isPlaying
    }

    override fun hasPlayed(): Boolean {
        return hasPlayed
    }

    override fun getDuration(): Float {
        return player?.duration?.toFloat() ?: 0f
    }

    override fun getCurrentPosition(): Float {
        return player?.currentPosition?.toFloat() ?: 0f
    }

    override fun onCompletion(listener: () -> Unit) {
        player?.setOnCompletionListener {
            stop()
            listener()
        }

    }

}