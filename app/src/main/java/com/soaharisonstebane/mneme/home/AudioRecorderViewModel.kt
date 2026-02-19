package com.soaharisonstebane.mneme.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudioRecorderViewModel: ViewModel() {

    private var duration = 0L

    private val _isTimerOn = MutableStateFlow(false)
    val isTimerOn: StateFlow<Boolean> = _isTimerOn.asStateFlow()

    private val _time = MutableStateFlow("00:00.0")
    val time: StateFlow<String> = _time.asStateFlow()

    private var audioDuration = ""


    private var delay = 100L

    suspend fun startTimer() {
        _isTimerOn.update { true }
        while (isTimerOn.value) {
            delay(delay)
            duration += delay
            _time.update {
                formatTime(duration)
            }
            audioDuration = time.value.dropLast(3)
        }
    }

    fun stopTimer() {
        _isTimerOn.update { false }
        duration = 0L
        _time.update { formatTime(duration) }
    }

    fun pauseTimer() {
        _isTimerOn.update { false }
    }

    fun formatTime(time: Long): String {
        val mills = time % 1000
        val seconds = (time / 1000) % 60
        val minutes = (time / (1000 * 60)) % 60
        val hours = (time / (1000 * 60 * 60))

        return if (hours > 0) {
            "%02d:%02d:%02d:%02d".format(hours, minutes, seconds, mills/10)
        } else{
            "%02d:%02d:%02d".format(minutes, seconds, mills/10)
        }
    }

    fun getAudioDuration() = audioDuration
}