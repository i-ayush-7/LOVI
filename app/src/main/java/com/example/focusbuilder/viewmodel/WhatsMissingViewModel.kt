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

enum class WhatsMissingPhase { MEMORIZE, HIDDEN, INPUT }
enum class MemoryAnimal { FOX, RABBIT, OWL, BEAR, DEER, FROG }

data class MemorySlot(
    val id: Int,
    val animal: MemoryAnimal?,
    val isHidden: Boolean = false
)

data class AnswerOption(
    val animal: MemoryAnimal,
    val isErrorAccent: Boolean = false,
    val isSuccessAccent: Boolean = false
)

class WhatsMissingViewModel : ViewModel() {
    private val _gamePhase = MutableStateFlow(GamePhase.PLAYING)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _phase = MutableStateFlow(WhatsMissingPhase.MEMORIZE)
    val phase: StateFlow<WhatsMissingPhase> = _phase.asStateFlow()

    private val _slots = MutableStateFlow<List<MemorySlot>>(emptyList())
    val slots: StateFlow<List<MemorySlot>> = _slots.asStateFlow()

    private val _remainingAnimals = MutableStateFlow<List<MemoryAnimal>>(emptyList())
    val remainingAnimals: StateFlow<List<MemoryAnimal>> = _remainingAnimals.asStateFlow()

    private val _answerBank = MutableStateFlow<List<AnswerOption>>(emptyList())
    val answerBank: StateFlow<List<AnswerOption>> = _answerBank.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    private val _mistakeCount = MutableStateFlow(0)
    val mistakeCount: StateFlow<Int> = _mistakeCount.asStateFlow()

    private val _viewDuration = MutableStateFlow(3000L)
    val viewDuration: StateFlow<Long> = _viewDuration.asStateFlow()

    private val TOTAL_SLOTS = 6
    private var objectCount = 3
    private var currentMissingAnimal: MemoryAnimal? = null
    private var isProcessingTap = false

    init {
        startRound()
    }

    private fun startRound() {
        if (_correctCount.value >= 4) {
            _gamePhase.value = GamePhase.COMPLETED
            return
        }

        val allAnimals = MemoryAnimal.values().toList().shuffled()
        val roundAnimals = allAnimals.take(objectCount)
        
        val activeIndices = (0 until TOTAL_SLOTS).toList().shuffled().take(objectCount)
        currentMissingAnimal = roundAnimals.random()
        
        _remainingAnimals.value = roundAnimals.filter { it != currentMissingAnimal }

        // Build answer bank: remaining animals + missing animal + optional distractor
        val bankAnimals = roundAnimals.toMutableList()
        if (_correctCount.value > 0) { // Add 1 extra distractor after round 1
            val unusedAnimals = allAnimals.drop(objectCount)
            if (unusedAnimals.isNotEmpty()) {
                bankAnimals.add(unusedAnimals.first())
            }
        }
        _answerBank.value = bankAnimals.shuffled().map { AnswerOption(it) }

        var animalIndex = 0
        _slots.value = (0 until TOTAL_SLOTS).map { i ->
            if (i in activeIndices) {
                MemorySlot(id = i, animal = roundAnimals[animalIndex++])
            } else {
                MemorySlot(id = i, animal = null)
            }
        }
        
        _phase.value = WhatsMissingPhase.MEMORIZE
        isProcessingTap = false

        viewModelScope.launch {
            delay(_viewDuration.value)
            
            _phase.value = WhatsMissingPhase.HIDDEN
            _slots.value = _slots.value.map { it.copy(isHidden = true) }
            delay(500) // Brief all-hidden moment

            // Grid remains fully hidden in this new logic! We just move to INPUT phase.
            _phase.value = WhatsMissingPhase.INPUT
        }
    }

    fun onAnswerTapped(animal: MemoryAnimal) {
        if (_gamePhase.value == GamePhase.COMPLETED || _phase.value != WhatsMissingPhase.INPUT || isProcessingTap) {
            return
        }
        
        isProcessingTap = true
        viewModelScope.launch {
            if (animal == currentMissingAnimal) {
                SoundManager.playSuccess()
                // Correct tap
                _answerBank.value = _answerBank.value.map { option ->
                    if (option.animal == animal) option.copy(isSuccessAccent = true) else option
                }
                delay(1000)
                
                _correctCount.value += 1
                
                when (_correctCount.value) {
                    1 -> objectCount = 4
                    2 -> objectCount = 5
                    3 -> _viewDuration.value = 2000L // Harder: less time
                }
                
                startRound()
            } else {
                // Mistake
                _answerBank.value = _answerBank.value.map { option ->
                    if (option.animal == animal) {
                        option.copy(isErrorAccent = true)
                    } else if (option.animal == currentMissingAnimal) {
                        // Reveal the truly missing object so they learn
                        option.copy(isSuccessAccent = true)
                    } else {
                        option
                    }
                }
                _mistakeCount.value += 1
                delay(1500)
                
                startRound() // Replay exact same difficulty
            }
        }
    }
}
