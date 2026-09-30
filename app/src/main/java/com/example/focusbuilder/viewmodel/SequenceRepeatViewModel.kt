package com.example.focusbuilder.viewmodel

import com.example.focusbuilder.util.SoundManager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class SequencePhase { PLAYBACK, INPUT }

class SequenceRepeatViewModel : ViewModel() {
    private val _gamePhase = MutableStateFlow(GamePhase.PLAYING)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _sequencePhase = MutableStateFlow(SequencePhase.PLAYBACK)
    val sequencePhase: StateFlow<SequencePhase> = _sequencePhase.asStateFlow()

    private val _activePad = MutableStateFlow<Int?>(null)
    val activePad: StateFlow<Int?> = _activePad.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    private val _mistakeCount = MutableStateFlow(0)
    val mistakeCount: StateFlow<Int> = _mistakeCount.asStateFlow()

    private val _successPad = MutableStateFlow<Int?>(null)
    val successPad: StateFlow<Int?> = _successPad.asStateFlow()

    private val _userProgressIndex = MutableStateFlow(0)
    val userProgressIndex: StateFlow<Int> = _userProgressIndex.asStateFlow()

    private val _currentSequenceLength = MutableStateFlow(2)
    val currentSequenceLength: StateFlow<Int> = _currentSequenceLength.asStateFlow()

    private var currentSequence = listOf<Int>()
    private var isProcessingTap = false

    init {
        startNextRound()
    }

    private fun startNextRound() {
        if (_currentSequenceLength.value > 5) {
            _gamePhase.value = GamePhase.COMPLETED
            return
        }
        
        _userProgressIndex.value = 0
        currentSequence = List(_currentSequenceLength.value) { Random.nextInt(4) }
        playSequence()
    }

    private fun playSequence() {
        _sequencePhase.value = SequencePhase.PLAYBACK
        _activePad.value = null
        
        viewModelScope.launch {
            delay(1000) // Brief pause before starting playback
            for (pad in currentSequence) {
                _activePad.value = pad
                delay(900) // Highlight duration increased to 900ms
                _activePad.value = null
                delay(400) // Pause between pads increased to 400ms
            }
            _sequencePhase.value = SequencePhase.INPUT
        }
    }

    fun replaySequence() {
        if (_sequencePhase.value == SequencePhase.INPUT && !isProcessingTap) {
            _userProgressIndex.value = 0 // Reset input progress
            playSequence()
        }
    }

    fun onPadTapped(padIndex: Int) {
        if (_gamePhase.value == GamePhase.COMPLETED || _sequencePhase.value == SequencePhase.PLAYBACK || isProcessingTap) {
            return
        }

        if (padIndex == currentSequence[_userProgressIndex.value]) {
            // Correct tap
            isProcessingTap = true
            viewModelScope.launch {
                _successPad.value = padIndex
                delay(150) // Brief flash for correct feedback
                _successPad.value = null
                
                _userProgressIndex.value += 1
                if (_userProgressIndex.value == currentSequence.size) {
                    // Successfully finished the sequence
                    _correctCount.value += 1
                    _currentSequenceLength.value += 1
                    SoundManager.playSuccess() // Play success!
                    delay(400) // Brief pause before new round
                    startNextRound()
                } else {
                    // Correct tap, but sequence not yet finished -> PLAY NEUTRAL TAP HERE ONLY!
                    SoundManager.playTap()
                }
                isProcessingTap = false
            }
        } else {
            // Mistake
            _mistakeCount.value += 1
            isProcessingTap = true
            SoundManager.playRetry() // Play retry sound!
            
            // Replay a FRESH sequence of the SAME length
            startNextRound()
        }
    }
}
