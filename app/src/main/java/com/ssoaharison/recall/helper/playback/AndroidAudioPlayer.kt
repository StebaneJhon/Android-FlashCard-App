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

    override fun playFile(file: File) {
        isPlaying = true
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
    }

    override fun isPlaying(): Boolean {
        return isPlaying
    }
}