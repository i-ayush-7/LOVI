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

enum class SortRule { COLOR, SHAPE }
enum class SortColor { RED, BLUE }
enum class SortShape { CIRCLE, SQUARE }
enum class SortBin { LEFT, RIGHT }

data class SortObject(val color: SortColor, val shape: SortShape)

class RuleSwitchSortViewModel : ViewModel() {

    private val _gamePhase = MutableStateFlow(GamePhase.PLAYING)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _currentRule = MutableStateFlow(SortRule.COLOR)
    val currentRule: StateFlow<SortRule> = _currentRule.asStateFlow()

    private val _currentObject = MutableStateFlow<SortObject?>(null)
    val currentObject: StateFlow<SortObject?> = _currentObject.asStateFlow()

    private val _ruleCorrectStreak = MutableStateFlow(0)
    val ruleCorrectStreak: StateFlow<Int> = _ruleCorrectStreak.asStateFlow()

    private val _mistakeCount = MutableStateFlow(0)
    val mistakeCount: StateFlow<Int> = _mistakeCount.asStateFlow()

    private val _totalCorrect = MutableStateFlow(0)
    val totalCorrect: StateFlow<Int> = _totalCorrect.asStateFlow()

    private val _showNewRuleBanner = MutableStateFlow(false)
    val showNewRuleBanner: StateFlow<Boolean> = _showNewRuleBanner.asStateFlow()

    private val _correctBinHint = MutableStateFlow<SortBin?>(null)
    val correctBinHint: StateFlow<SortBin?> = _correctBinHint.asStateFlow()

    private val _successfulBin = MutableStateFlow<SortBin?>(null)
    val successfulBin: StateFlow<SortBin?> = _successfulBin.asStateFlow()

    private val _leftBinContents = MutableStateFlow<List<SortObject>>(emptyList())
    val leftBinContents: StateFlow<List<SortObject>> = _leftBinContents.asStateFlow()

    private val _rightBinContents = MutableStateFlow<List<SortObject>>(emptyList())
    val rightBinContents: StateFlow<List<SortObject>> = _rightBinContents.asStateFlow()

    private val _showBlockCompleteCelebration = MutableStateFlow(false)
    val showBlockCompleteCelebration: StateFlow<Boolean> = _showBlockCompleteCelebration.asStateFlow()

    private var ruleBlocksCompleted = 0
    private val STREAK_TO_SWITCH = 4
    private val TOTAL_BLOCKS = 2

    private var isProcessingTap = false

    init {
        generateNewObject()
    }

    private fun generateNewObject() {
        var newObj: SortObject
        do {
            newObj = SortObject(
                color = if (Random.nextBoolean()) SortColor.RED else SortColor.BLUE,
                shape = if (Random.nextBoolean()) SortShape.CIRCLE else SortShape.SQUARE
            )
        } while (newObj == _currentObject.value)
        
        _currentObject.value = newObj
        _correctBinHint.value = null
        _successfulBin.value = null
        isProcessingTap = false
    }

    fun onBinTapped(bin: SortBin) {
        if (_gamePhase.value == GamePhase.COMPLETED || isProcessingTap || _showNewRuleBanner.value) {
            return
        }

        val obj = _currentObject.value ?: return
        isProcessingTap = true

        val correctBin = when (_currentRule.value) {
            SortRule.COLOR -> if (obj.color == SortColor.RED) SortBin.LEFT else SortBin.RIGHT
            SortRule.SHAPE -> if (obj.shape == SortShape.CIRCLE) SortBin.LEFT else SortBin.RIGHT
        }

        if (bin == correctBin) {
            SoundManager.playSuccess() // Immediate success!
            _successfulBin.value = bin
            _totalCorrect.value += 1
            _ruleCorrectStreak.value += 1
            
            // Add to bin contents
            if (bin == SortBin.LEFT) {
                _leftBinContents.value = _leftBinContents.value + obj
            } else {
                _rightBinContents.value = _rightBinContents.value + obj
            }
            
            viewModelScope.launch {
                delay(600)
                if (_ruleCorrectStreak.value >= STREAK_TO_SWITCH) {
                    ruleBlocksCompleted += 1
                    
                    _showBlockCompleteCelebration.value = true
                    delay(1000)
                    _showBlockCompleteCelebration.value = false
                    
                    if (ruleBlocksCompleted >= TOTAL_BLOCKS) {
                        _gamePhase.value = GamePhase.COMPLETED
                    } else {
                        // Switch rule
                        _ruleCorrectStreak.value = 0
                        _currentRule.value = if (_currentRule.value == SortRule.COLOR) SortRule.SHAPE else SortRule.COLOR
                        _leftBinContents.value = emptyList()
                        _rightBinContents.value = emptyList()
                        _showNewRuleBanner.value = true
                        delay(2000)
                        _showNewRuleBanner.value = false
                        generateNewObject()
                    }
                } else {
                    generateNewObject()
                }
            }
        } else {
            SoundManager.playRetry() // Immediate retry/mistake!
            _mistakeCount.value += 1
            _correctBinHint.value = correctBin
            
            viewModelScope.launch {
                delay(1200)
                generateNewObject()
            }
        }
    }
}
