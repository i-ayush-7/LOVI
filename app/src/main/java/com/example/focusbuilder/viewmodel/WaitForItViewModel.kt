package com.example.focusbuilder.viewmodel

import com.example.focusbuilder.util.SoundManager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class CircleColor { RED, GREEN }
enum class GamePhase { PLAYING, COMPLETED }
enum class TapFeedback { PULSE, SHAKE }

class WaitForItViewModel : ViewModel() {
    private val _currentColor = MutableStateFlow(CircleColor.RED)
    val currentColor: StateFlow<CircleColor> = _currentColor.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    private val _mistakeCount = MutableStateFlow(0)
    val mistakeCount: StateFlow<Int> = _mistakeCount.asStateFlow()

    private val _gamePhase = MutableStateFlow(GamePhase.PLAYING)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _feedbackEvent = MutableSharedFlow<TapFeedback>()
    val feedbackEvent: SharedFlow<TapFeedback> = _feedbackEvent.asSharedFlow()

    private var colorLoopJob: Job? = null
    private var isPaused = false

    init {
        startColorLoop()
    }

    private fun startColorLoop() {
        colorLoopJob?.cancel()
        colorLoopJob = viewModelScope.launch {
            while (_gamePhase.value == GamePhase.PLAYING) {
                delay(Random.nextLong(1500, 4000))
                if (!isPaused) {
                    _currentColor.value = if (_currentColor.value == CircleColor.RED) CircleColor.GREEN else CircleColor.RED
                }
            }
        }
    }

    fun onCircleTapped() {
        if (_gamePhase.value == GamePhase.COMPLETED || isPaused) return

        viewModelScope.launch {
            if (_currentColor.value == CircleColor.GREEN) {
                // Correct tap
                isPaused = true
                _correctCount.value += 1
                _feedbackEvent.emit(TapFeedback.PULSE)
                SoundManager.playSuccess()

                if (_correctCount.value >= 8) {
                    _gamePhase.value = GamePhase.COMPLETED
                } else {
                    delay(Random.nextLong(300, 500))
                    // Switch to a new random color state
                    _currentColor.value = if (Random.nextBoolean()) CircleColor.GREEN else CircleColor.RED
                    isPaused = false
                    startColorLoop() // Restart loop to reset the 1.5 - 4s timer
                }
            } else {
                // Incorrect tap (Red)
                _mistakeCount.value += 1
                _feedbackEvent.emit(TapFeedback.SHAKE)
                SoundManager.playRetry()
            }
        }
    }
}
