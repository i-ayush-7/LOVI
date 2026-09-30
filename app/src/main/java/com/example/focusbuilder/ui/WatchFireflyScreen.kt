package com.example.focusbuilder.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusbuilder.viewmodel.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.key
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.focusbuilder.R

@Composable
fun WatchFireflyScreen(
    onNavigateHome: () -> Unit,
    viewModel: WatchFireflyViewModel = viewModel()
) {
    val gamePhase by viewModel.gamePhase.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val flowerCount by viewModel.flowerCount.collectAsState()
    val targetFlowerIds by viewModel.targetFlowerIds.collectAsState()
    val currentFireflyId by viewModel.currentFireflyFlowerId.collectAsState()
    val startFireflyId by viewModel.startFireflyFlowerId.collectAsState()
    val fireflyState by viewModel.fireflyState.collectAsState()
    val lastOutcome by viewModel.lastOutcome.collectAsState()
    
    val correctCatches by viewModel.correctCatches.collectAsState()
    val falseCatches by viewModel.falseCatches.collectAsState()
    val missedCatches by viewModel.missedCatches.collectAsState()
    val instructionVisible by viewModel.instructionVisible.collectAsState()
    val levelUpVisible by viewModel.levelUpVisible.collectAsState()

    if (gamePhase == GamePhase.COMPLETED) {
        GameCompleteScreen(
            title = "You stayed focused!",
            statText = "Caught: $correctCatches · Missed: $missedCatches · False: $falseCatches",
            onNavigateHome = onNavigateHome
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Level Indicator
            Text(
                text = "Level $currentLevel of 6",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Instruction Banner
            AnimatedVisibility(
                visible = instructionVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Watch for the glowing target flower${if (targetFlowerIds.size > 1) "s" else ""}!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Scoreboard
        Text(
            text = "Caught: $correctCatches · Missed: $missedCatches · False: $falseCatches",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        // Scene Area
        Box(
            modifier = Modifier
                .size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            val radius = 110.dp
            
            // Continuous drift animation
            val infiniteTransition = rememberInfiniteTransition(label = "flower_drift")
            val timeValue by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = (2 * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(20000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "time"
            )

            // Animate angles smoothly so everything (flowers and firefly) stays in sync
            val animatedAngles = mutableListOf<Float>()
            for (i in 0 until flowerCount) {
                key(i) {
                    val targetBaseAngle = (i * 2 * PI / flowerCount) - PI / 2
                    val baseAngle by animateFloatAsState(
                        targetValue = targetBaseAngle.toFloat(),
                        animationSpec = tween(2000, easing = LinearOutSlowInEasing),
                        label = "baseAngle_$i"
                    )
                    
                    val driftMultiplier = if (currentLevel >= 3) (currentLevel - 2) * 0.12f else 0f
                    val drift = (sin(timeValue + i * (PI / 3)).toFloat()) * driftMultiplier
                    animatedAngles.add(baseAngle + drift)
                }
            }
            
            // Draw flowers
            for (i in 0 until flowerCount) {
                val angle = animatedAngles.getOrNull(i) ?: 0f
                val offsetX = (cos(angle) * radius.value).dp
                val offsetY = (sin(angle) * radius.value).dp
                val isTarget = targetFlowerIds.contains(i)
                
                // TODO(Stitch): PENDING LEAF ASSET
                // Once the leaf asset is generated, replace this conditional block with a single Image() call
                // using `R.drawable.leaf` for the false case.
                if (isTarget) {
                    Image(
                        painter = painterResource(id = R.drawable.flower_target),
                        contentDescription = "Glowing target flower",
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(40.dp)
                            .background(
                                color = Color(0xFF81C784), // Soft pastel green
                                shape = RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 4.dp, bottomStart = 4.dp)
                            )
                    )
                }
            }
            
            // Draw Firefly
            val driftProgress = remember { Animatable(1f) }
            LaunchedEffect(currentFireflyId) {
                if (currentLevel >= 3) {
                    driftProgress.snapTo(0f)
                    driftProgress.animateTo(1f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                }
            }

            val startAngle = animatedAngles.getOrNull(startFireflyId) ?: 0f
            val targetAngle = animatedAngles.getOrNull(currentFireflyId) ?: 0f

            val startX = (cos(startAngle) * radius.value)
            val startY = (sin(startAngle) * radius.value)
            val endX = (cos(targetAngle) * radius.value)
            val endY = (sin(targetAngle) * radius.value)

            val fireflyOffsetX: androidx.compose.ui.unit.Dp
            val fireflyOffsetY: androidx.compose.ui.unit.Dp

            if (currentLevel >= 3) {
                val t = driftProgress.value
                val midX = (startX + endX) / 2f
                val midY = (startY + endY) / 2f
                val dist = kotlin.math.sqrt(midX*midX + midY*midY)
                val normX = if (dist > 0) midX / dist else 0f
                val normY = if (dist > 0) midY / dist else 0f
                val push = 40f // Arc outwards by 40dp
                val cpX = midX + normX * push
                val cpY = midY + normY * push
                
                val invT = 1f - t
                fireflyOffsetX = (invT * invT * startX + 2 * invT * t * cpX + t * t * endX).dp
                fireflyOffsetY = (invT * invT * startY + 2 * invT * t * cpY + t * t * endY).dp
            } else {
                val animatedX by animateDpAsState(targetValue = endX.dp, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                val animatedY by animateDpAsState(targetValue = endY.dp, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                fireflyOffsetX = animatedX
                fireflyOffsetY = animatedY
            }
            
            val fireflyDrawable = if (fireflyState == FireflyState.FEEDBACK) {
                when (lastOutcome) {
                    CatchOutcome.CORRECT -> R.drawable.firefly_correct
                    CatchOutcome.FALSE -> R.drawable.firefly_false
                    CatchOutcome.MISSED -> R.drawable.firefly_missed
                    else -> R.drawable.firefly_base
                }
            } else {
                R.drawable.firefly_base
            }
            
            val fireflyContentDesc = if (fireflyState == FireflyState.FEEDBACK) {
                when (lastOutcome) {
                    CatchOutcome.CORRECT -> "Happy firefly celebrating correct catch"
                    CatchOutcome.FALSE -> "Dim firefly showing incorrect catch"
                    CatchOutcome.MISSED -> "Amber firefly showing missed target"
                    else -> "Glowing firefly character"
                }
            } else {
                "Glowing firefly character"
            }
            
            Image(
                painter = painterResource(id = fireflyDrawable),
                contentDescription = fireflyContentDesc,
                modifier = Modifier
                    .offset(x = fireflyOffsetX, y = fireflyOffsetY)
                    .size(48.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Catch Button
        val buttonColor = when {
            fireflyState == FireflyState.FEEDBACK && lastOutcome == CatchOutcome.CORRECT -> Color.Green
            fireflyState == FireflyState.FEEDBACK && lastOutcome == CatchOutcome.FALSE -> Color.Gray
            fireflyState == FireflyState.FEEDBACK && lastOutcome == CatchOutcome.MISSED -> Color.Red
            else -> MaterialTheme.colorScheme.primary
        }
        
        val buttonText = when {
            fireflyState == FireflyState.FEEDBACK && lastOutcome == CatchOutcome.CORRECT -> "You stayed focused!"
            fireflyState == FireflyState.FEEDBACK && lastOutcome == CatchOutcome.FALSE -> "Watch closely."
            fireflyState == FireflyState.FEEDBACK && lastOutcome == CatchOutcome.MISSED -> "Watch closely."
            else -> "CATCH!"
        }

        Button(
            onClick = { viewModel.onCatchTapped() },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Full-screen Level Up Overlay
    AnimatedVisibility(
        visible = levelUpVisible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Level $currentLevel!",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

}
