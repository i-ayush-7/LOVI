package com.example.focusbuilder.viewmodel

import com.example.focusbuilder.util.SoundManager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class FireflyState { DRIFTING, LANDED, FEEDBACK }
enum class CatchOutcome { NONE, CORRECT, FALSE, MISSED }

class WatchFireflyViewModel : ViewModel() {

    private val _gamePhase = MutableStateFlow(GamePhase.PLAYING)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _flowerCount = MutableStateFlow(4)
    val flowerCount: StateFlow<Int> = _flowerCount.asStateFlow()

    private val _targetFlowerIds = MutableStateFlow<Set<Int>>(emptySet())
    val targetFlowerIds: StateFlow<Set<Int>> = _targetFlowerIds.asStateFlow()

    private val _currentFireflyFlowerId = MutableStateFlow(0)
    val currentFireflyFlowerId: StateFlow<Int> = _currentFireflyFlowerId.asStateFlow()
    
    private val _startFireflyFlowerId = MutableStateFlow(0)
    val startFireflyFlowerId: StateFlow<Int> = _startFireflyFlowerId.asStateFlow()

    private val _levelUpVisible = MutableStateFlow(false)
    val levelUpVisible: StateFlow<Boolean> = _levelUpVisible.asStateFlow()
    
    private val _fireflyState = MutableStateFlow(FireflyState.DRIFTING)
    val fireflyState: StateFlow<FireflyState> = _fireflyState.asStateFlow()

    private val _lastOutcome = MutableStateFlow(CatchOutcome.NONE)
    val lastOutcome: StateFlow<CatchOutcome> = _lastOutcome.asStateFlow()

    private val _correctCatches = MutableStateFlow(0)
    val correctCatches: StateFlow<Int> = _correctCatches.asStateFlow()

    private val _falseCatches = MutableStateFlow(0)
    val falseCatches: StateFlow<Int> = _falseCatches.asStateFlow()

    private val _missedCatches = MutableStateFlow(0)
    val missedCatches: StateFlow<Int> = _missedCatches.asStateFlow()

    private val _instructionVisible = MutableStateFlow(true)
    val instructionVisible: StateFlow<Boolean> = _instructionVisible.asStateFlow()

    private var roundIndex = 0
    private val TOTAL_LEVELS = 6
    private var totalLandingsRound = 8
    
    private var hasTappedThisLanding = false
    private var loopJob: Job? = null

    init {
        startLevel()
    }

    private fun startLevel() {
        if (roundIndex >= TOTAL_LEVELS) {
            _gamePhase.value = GamePhase.COMPLETED
            return
        }

        _currentLevel.value = roundIndex + 1

        val targetCount: Int
        when (roundIndex) {
            0 -> { _flowerCount.value = 4; totalLandingsRound = 8; targetCount = 1 }
            1 -> { _flowerCount.value = 5; totalLandingsRound = 10; targetCount = 1 }
            2 -> { _flowerCount.value = 6; totalLandingsRound = 12; targetCount = 2 }
            3 -> { _flowerCount.value = 7; totalLandingsRound = 12; targetCount = 2 }
            4 -> { _flowerCount.value = 8; totalLandingsRound = 14; targetCount = 3 }
            5 -> { _flowerCount.value = 8; totalLandingsRound = 16; targetCount = 3 }
            else -> { _flowerCount.value = 4; totalLandingsRound = 8; targetCount = 1 }
        }

        val allIds = (0 until _flowerCount.value).toMutableList()
        allIds.shuffle()
        _targetFlowerIds.value = allIds.take(targetCount).toSet()
        
        val initialStart = Random.nextInt(_flowerCount.value)
        _startFireflyFlowerId.value = initialStart
        _currentFireflyFlowerId.value = initialStart
        _lastOutcome.value = CatchOutcome.NONE
        
        startFireflyLoop()
    }

    private fun startFireflyLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            _instructionVisible.value = true
            _fireflyState.value = FireflyState.DRIFTING
            delay(3000) // Give child time to read instruction and see target
            _instructionVisible.value = false
            
            var landings = 0
            while (landings < totalLandingsRound) {
                // 1. Drift
                _startFireflyFlowerId.value = _currentFireflyFlowerId.value
                _fireflyState.value = FireflyState.DRIFTING
                _lastOutcome.value = CatchOutcome.NONE
                hasTappedThisLanding = false
                
                // Pick next flower, ensure it's different from current
                var nextFlower = Random.nextInt(_flowerCount.value)
                while (nextFlower == _currentFireflyFlowerId.value) {
                    nextFlower = Random.nextInt(_flowerCount.value)
                }
                _currentFireflyFlowerId.value = nextFlower
                
                delay(1500) // Drift time: nice and slow
                
                // 2. Landed
                _fireflyState.value = FireflyState.LANDED
                delay(2000) // Wait time on flower
                
                // 3. Check for miss
                if (!hasTappedThisLanding && _targetFlowerIds.value.contains(_currentFireflyFlowerId.value)) {
                    _missedCatches.value += 1
                    _lastOutcome.value = CatchOutcome.MISSED
                        SoundManager.playRetry()
                    _fireflyState.value = FireflyState.FEEDBACK
                    delay(1000) // Show missed feedback briefly
                } else if (_lastOutcome.value != CatchOutcome.NONE) {
                    // Let the tap feedback linger for a moment before taking off
                    delay(500)
                }
                
                landings++
            }
            
            roundIndex++
            if (roundIndex < TOTAL_LEVELS) {
                _currentLevel.value = roundIndex + 1 // Early update for Level Up banner text
                _levelUpVisible.value = true
                delay(2500)
                _levelUpVisible.value = false
                startLevel()
            } else {
                _gamePhase.value = GamePhase.COMPLETED
            }
        }
    }

    fun onCatchTapped() {
        if (_gamePhase.value == GamePhase.COMPLETED) return
        if (_fireflyState.value != FireflyState.LANDED) return
        if (hasTappedThisLanding) return
        
        hasTappedThisLanding = true

        _fireflyState.value = FireflyState.FEEDBACK
        
        if (_targetFlowerIds.value.contains(_currentFireflyFlowerId.value)) {
            _correctCatches.value += 1
            _lastOutcome.value = CatchOutcome.CORRECT
            SoundManager.playSuccess()
        } else {
            _falseCatches.value += 1
            _lastOutcome.value = CatchOutcome.FALSE
            SoundManager.playRetry()
        }
    }

    override fun onCleared() {
        loopJob?.cancel()
        super.onCleared()
    }
}
