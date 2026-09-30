package com.example.focusbuilder.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusbuilder.R
import com.example.focusbuilder.viewmodel.CircleColor
import com.example.focusbuilder.viewmodel.GamePhase
import com.example.focusbuilder.viewmodel.TapFeedback
import com.example.focusbuilder.viewmodel.WaitForItViewModel
import kotlinx.coroutines.launch

@Composable
fun WaitForItScreen(
    onNavigateHome: () -> Unit,
    viewModel: WaitForItViewModel = viewModel()
) {
    val currentColor by viewModel.currentColor.collectAsState()
    val gamePhase by viewModel.gamePhase.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()

    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.feedbackEvent.collect { feedback ->
            when (feedback) {
                TapFeedback.PULSE -> {
                    launch {
                        scale.animateTo(1.05f, animationSpec = tween(150))
                        scale.animateTo(1f, animationSpec = tween(150))
                    }
                }
                TapFeedback.SHAKE -> {
                    launch {
                        offsetX.animateTo(15f, animationSpec = tween(50))
                        offsetX.animateTo(-15f, animationSpec = tween(50))
                        offsetX.animateTo(15f, animationSpec = tween(50))
                        offsetX.animateTo(0f, animationSpec = tween(50))
                    }
                }
            }
        }
    }

    if (gamePhase == GamePhase.COMPLETED) {
        // Reuse the GameCompleteScreen template natively, passing in the real stat!
        GameCompleteScreen(
            title = "Nice waiting!",
            statText = "Correct taps: $correctCount",
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
            
            // Progress Indicator (dots)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..8) {
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
            
            Spacer(modifier = Modifier.weight(1f))

            // The massive hitbox containing the state-aware illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offsetX.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.onCircleTapped()
                    },
                contentAlignment = Alignment.Center
            ) {
                val imageRes = if (currentColor == CircleColor.GREEN) {
                    R.drawable.illust_lantern_go
                } else {
                    R.drawable.illust_lantern_stop
                }
                
                val contentDesc = if (currentColor == CircleColor.GREEN) {
                    "Pip the fawn holding a bright green glowing lantern indicating Go!"
                } else {
                    "Pip the fawn holding a sleepy red lantern indicating Stop"
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = contentDesc,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
