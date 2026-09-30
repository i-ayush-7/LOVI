package com.example.focusbuilder.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.focusbuilder.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusbuilder.viewmodel.AnswerOption
import com.example.focusbuilder.viewmodel.GamePhase
import com.example.focusbuilder.viewmodel.MemoryAnimal
import com.example.focusbuilder.viewmodel.MemorySlot
import com.example.focusbuilder.viewmodel.WhatsMissingPhase
import com.example.focusbuilder.viewmodel.WhatsMissingViewModel

@Composable
fun WhatsMissingScreen(
    onNavigateHome: () -> Unit,
    viewModel: WhatsMissingViewModel = viewModel()
) {
    val gamePhase by viewModel.gamePhase.collectAsState()
    val phase by viewModel.phase.collectAsState()
    val slots by viewModel.slots.collectAsState()
    val remainingAnimals by viewModel.remainingAnimals.collectAsState()
    val answerBank by viewModel.answerBank.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()
    val mistakeCount by viewModel.mistakeCount.collectAsState()
    val viewDuration by viewModel.viewDuration.collectAsState()

    if (gamePhase == GamePhase.COMPLETED) {
        GameCompleteScreen(
            title = "Good memory!",
            statText = "Rounds cleared: $correctCount · Mistakes: $mistakeCount",
            onNavigateHome = onNavigateHome
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Round Scoreboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == correctCount + 1) 16.dp else 12.dp)
                            .background(
                                color = if (i <= correctCount) MaterialTheme.colorScheme.primary 
                                        else if (i == correctCount + 1) MaterialTheme.colorScheme.secondary 
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val phaseText = when(phase) {
                WhatsMissingPhase.MEMORIZE -> "Memorize..."
                WhatsMissingPhase.HIDDEN -> "..."
                WhatsMissingPhase.INPUT -> "What's missing?"
            }
            Text(
                text = phaseText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Visual Countdown for Memorize Phase
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (phase == WhatsMissingPhase.MEMORIZE) {
                    val progress = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(1f) }
                    androidx.compose.runtime.LaunchedEffect(phase) {
                        progress.snapTo(1f)
                        progress.animateTo(
                            targetValue = 0f,
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = viewDuration.toInt(),
                                easing = androidx.compose.animation.core.LinearEasing
                            )
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.value)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (phase == WhatsMissingPhase.INPUT) {
                // Show the Answer Bank below
                Text("Answer Bank:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(16.dp))
                
                // Wrap answer bank in rows if it gets large, but for 3-6 items a row or wrap is fine
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    answerBank.forEach { option ->
                        AnswerOptionView(option = option, onClick = { viewModel.onAnswerTapped(option.animal) })
                    }
                }
                
            } else {
                // 3x2 Grid of fixed slots during MEMORIZE / HIDDEN
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0..2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            for (col in 0..1) {
                                val slotIndex = row * 2 + col
                                val slot = slots.getOrNull(slotIndex)
                                if (slot != null) {
                                    MemorySlotView(slot = slot)
                                }
                            }
                        }
                    }
                }
            }
            
        }
    }
}

@Composable
fun MemorySlotView(slot: MemorySlot) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .size(140.dp)
            .background(bgColor, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (slot.animal != null && !slot.isHidden) {
            AnimalIcon(animal = slot.animal, size = 110.dp)
        }
    }
}

@Composable
fun AnswerOptionView(option: AnswerOption, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (option.isSuccessAccent) 1.05f else if (option.isErrorAccent) 0.95f else 1f,
        animationSpec = tween(200),
        label = "answerScale"
    )

    val bgColor = if (option.isSuccessAccent) {
        Color.Green.copy(alpha = 0.3f)
    } else if (option.isErrorAccent) {
        Color.Red.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(bgColor, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimalIcon(animal = option.animal, size = 130.dp)
    }
}

@Composable
fun AnimalIcon(animal: MemoryAnimal, size: androidx.compose.ui.unit.Dp = 60.dp) {
    val imageRes = when (animal) {
        MemoryAnimal.FOX -> R.drawable.illust_fox
        MemoryAnimal.RABBIT -> R.drawable.illust_rabbit
        MemoryAnimal.OWL -> R.drawable.illust_owl
        MemoryAnimal.BEAR -> R.drawable.illust_bear
        MemoryAnimal.DEER -> R.drawable.illust_deer
        MemoryAnimal.FROG -> R.drawable.illust_frog
    }
    
    val contentDesc = when (animal) {
        MemoryAnimal.FOX -> "Fox"
        MemoryAnimal.RABBIT -> "Rabbit"
        MemoryAnimal.OWL -> "Owl"
        MemoryAnimal.BEAR -> "Bear"
        MemoryAnimal.DEER -> "Deer"
        MemoryAnimal.FROG -> "Frog"
    }
    
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDesc,
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit
    )
}
