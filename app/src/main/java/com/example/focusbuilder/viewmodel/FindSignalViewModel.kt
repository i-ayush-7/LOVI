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

enum class SignalColor { RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE }
enum class SignalShape { CIRCLE, SQUARE, TRIANGLE, STAR, DIAMOND }
enum class SignalRuleType { COLOR, SHAPE }

data class SignalObject(
    val id: Int,
    val color: SignalColor,
    val shape: SignalShape,
    val isTarget: Boolean,
    val isFound: Boolean = false,
    val showMistake: Boolean = false
)

class FindSignalViewModel : ViewModel() {

    private val _gamePhase = MutableStateFlow(GamePhase.PLAYING)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _gridObjects = MutableStateFlow<List<SignalObject>>(emptyList())
    val gridObjects: StateFlow<List<SignalObject>> = _gridObjects.asStateFlow()

    private val _currentRuleType = MutableStateFlow(SignalRuleType.COLOR)
    val currentRuleType: StateFlow<SignalRuleType> = _currentRuleType.asStateFlow()

    private val _targetColor = MutableStateFlow<SignalColor?>(null)
    val targetColor: StateFlow<SignalColor?> = _targetColor.asStateFlow()

    private val _targetShape = MutableStateFlow<SignalShape?>(null)
    val targetShape: StateFlow<SignalShape?> = _targetShape.asStateFlow()

    private val _gridRows = MutableStateFlow(4)
    val gridRows: StateFlow<Int> = _gridRows.asStateFlow()
    
    private val _gridCols = MutableStateFlow(4)
    val gridCols: StateFlow<Int> = _gridCols.asStateFlow()

    private val _targetsFound = MutableStateFlow(0)
    val targetsFound: StateFlow<Int> = _targetsFound.asStateFlow()

    private val _totalTargets = MutableStateFlow(0)
    val totalTargets: StateFlow<Int> = _totalTargets.asStateFlow()

    private val _mistakeCount = MutableStateFlow(0)
    val mistakeCount: StateFlow<Int> = _mistakeCount.asStateFlow()

    private val _totalCorrectTaps = MutableStateFlow(0)
    val totalCorrectTaps: StateFlow<Int> = _totalCorrectTaps.asStateFlow()
    
    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private var roundIndex = 0
    private val TOTAL_ROUNDS = 6
    private var isTransitioning = false

    // Difficulty params
    private var distractorVariety = 2 // Start with 2 colors/shapes

    init {
        startNewRound()
    }

    private fun startNewRound() {
        if (roundIndex >= TOTAL_ROUNDS) {
            _gamePhase.value = GamePhase.COMPLETED
            return
        }
        
        _currentLevel.value = roundIndex + 1

        // Alternate rule type
        val ruleType = if (roundIndex % 2 == 0) SignalRuleType.COLOR else SignalRuleType.SHAPE
        _currentRuleType.value = ruleType

        // Progression (6 levels, incrementing 1 dimension at a time)
        if (roundIndex == 0) {
            _gridRows.value = 4
            distractorVariety = 2
        } else if (roundIndex == 1) {
            distractorVariety = 3
        } else if (roundIndex == 2) {
            _gridRows.value = 5
        } else if (roundIndex == 3) {
            distractorVariety = 4
        } else if (roundIndex == 4) {
            _gridRows.value = 6
        } else if (roundIndex == 5) {
            distractorVariety = 5
        }

        val targetCount = Random.nextInt(3, 5) // 3 or 4 targets
        _totalTargets.value = targetCount
        _targetsFound.value = 0

        val totalCells = _gridRows.value * _gridCols.value
        val newObjects = mutableListOf<SignalObject>()

        val colors = SignalColor.values().toList().shuffled().take(distractorVariety)
        val shapes = SignalShape.values().toList().shuffled().take(distractorVariety)

        val targetCol = colors.first()
        val targetSha = shapes.first()

        if (ruleType == SignalRuleType.COLOR) {
            _targetColor.value = targetCol
            _targetShape.value = null
        } else {
            _targetColor.value = null
            _targetShape.value = targetSha
        }

        var targetsAdded = 0

        for (i in 0 until totalCells) {
            // Need to place exactly targetCount targets
            val remainingCells = totalCells - i
            val remainingTargets = targetCount - targetsAdded
            
            val isTarget = if (remainingTargets > 0 && (Random.nextFloat() < (remainingTargets.toFloat() / remainingCells) || remainingTargets == remainingCells)) {
                targetsAdded++
                true
            } else {
                false
            }

            var color: SignalColor
            var shape: SignalShape

            if (isTarget) {
                if (ruleType == SignalRuleType.COLOR) {
                    color = targetCol
                    shape = shapes.random()
                } else {
                    color = colors.random()
                    shape = targetSha
                }
            } else {
                if (ruleType == SignalRuleType.COLOR) {
                    // Distractor for color rule: must NOT be targetCol
                    color = colors.filter { it != targetCol }.random()
                    shape = shapes.random()
                } else {
                    // Distractor for shape rule: must NOT be targetSha
                    color = colors.random()
                    shape = shapes.filter { it != targetSha }.random()
                }
            }

            newObjects.add(SignalObject(id = i, color = color, shape = shape, isTarget = isTarget))
        }

        _gridObjects.value = newObjects
        isTransitioning = false
    }

    fun onObjectTapped(id: Int) {
        if (_gamePhase.value == GamePhase.COMPLETED || isTransitioning) return

        val currentList = _gridObjects.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) return
        val obj = currentList[index]

        if (obj.isFound || obj.showMistake) return

        if (obj.isTarget) {
            SoundManager.playSuccess()
            _totalCorrectTaps.value += 1
            _targetsFound.value += 1
            currentList[index] = obj.copy(isFound = true)
            _gridObjects.value = currentList

            if (_targetsFound.value >= _totalTargets.value) {
                isTransitioning = true
                viewModelScope.launch {
                    delay(1000)
                    roundIndex++
                    startNewRound()
                }
            }
        } else {

            SoundManager.playRetry()
            _mistakeCount.value += 1
            currentList[index] = obj.copy(showMistake = true)
            _gridObjects.value = currentList
            
            viewModelScope.launch {
                delay(600)
                val tempList = _gridObjects.value.toMutableList()
                val resetIndex = tempList.indexOfFirst { it.id == id }
                if (resetIndex != -1) {
                    tempList[resetIndex] = tempList[resetIndex].copy(showMistake = false)
                    _gridObjects.value = tempList
                }
            }
        }
    }
}
