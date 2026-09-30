package com.example.focusbuilder.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusbuilder.viewmodel.GamePhase
import com.example.focusbuilder.viewmodel.SequencePhase
import com.example.focusbuilder.viewmodel.SequenceRepeatViewModel

@Composable
fun SequenceRepeatScreen(
    onNavigateHome: () -> Unit,
    viewModel: SequenceRepeatViewModel = viewModel()
) {
    val gamePhase by viewModel.gamePhase.collectAsState()
    val sequencePhase by viewModel.sequencePhase.collectAsState()
    val activePad by viewModel.activePad.collectAsState()
    val successPad by viewModel.successPad.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()
    val mistakeCount by viewModel.mistakeCount.collectAsState()
    val userProgressIndex by viewModel.userProgressIndex.collectAsState()
    val currentSequenceLength by viewModel.currentSequenceLength.collectAsState()

    if (gamePhase == GamePhase.COMPLETED) {
        GameCompleteScreen(
            title = "You remembered!",
            statText = "Completed sequences: $correctCount · Mistakes: $mistakeCount",
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

            // Scoreboard Indicator (Dots) - Total Rounds Progress
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
            
            // Phase indicator
            Text(
                text = if (sequencePhase == SequencePhase.PLAYBACK) "Watch..." else "Your turn!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Input Progress Trail (taps within current sequence)
            Row(
                modifier = Modifier.height(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show a dot for each step in the current sequence
                for (i in 0 until currentSequenceLength) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .background(
                                color = if (i < userProgressIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val padColors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow)

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PadBox(
                    color = padColors[0],
                    isHighlighted = activePad == 0,
                    isSuccess = successPad == 0,
                    onClick = { viewModel.onPadTapped(0) }
                )
                PadBox(
                    color = padColors[1],
                    isHighlighted = activePad == 1,
                    isSuccess = successPad == 1,
                    onClick = { viewModel.onPadTapped(1) }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PadBox(
                    color = padColors[2],
                    isHighlighted = activePad == 2,
                    isSuccess = successPad == 2,
                    onClick = { viewModel.onPadTapped(2) }
                )
                PadBox(
                    color = padColors[3],
                    isHighlighted = activePad == 3,
                    isSuccess = successPad == 3,
                    onClick = { viewModel.onPadTapped(3) }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Watch Again Button
            Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.Center) {
                if (sequencePhase == SequencePhase.INPUT) {
                    Button(onClick = { viewModel.replaySequence() }) {
                        Text("Watch Again")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PadBox(color: Color, isHighlighted: Boolean, isSuccess: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.3f else if (isSuccess) 1.15f else 1f,
        animationSpec = tween(150),
        label = "padScale"
    )
    
    val bgColor = if (isSuccess) {
        Color.White // Brief flash to white/bright for success
    } else if (isHighlighted) {
        color.copy(alpha = 1f)
    } else {
        color.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(bgColor, shape = CircleShape)
            .border(
                width = if (isHighlighted) 4.dp else 0.dp,
                color = if (isHighlighted) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}
