package com.example.focusbuilder.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusbuilder.viewmodel.*

@Composable
fun FindSignalScreen(
    onNavigateHome: () -> Unit,
    viewModel: FindSignalViewModel = viewModel()
) {
    val gamePhase by viewModel.gamePhase.collectAsState()
    val gridObjects by viewModel.gridObjects.collectAsState()
    val gridCols by viewModel.gridCols.collectAsState()
    val targetsFound by viewModel.targetsFound.collectAsState()
    val totalTargets by viewModel.totalTargets.collectAsState()
    val currentRuleType by viewModel.currentRuleType.collectAsState()
    val targetColor by viewModel.targetColor.collectAsState()
    val targetShape by viewModel.targetShape.collectAsState()
    val mistakeCount by viewModel.mistakeCount.collectAsState()
    val totalCorrectTaps by viewModel.totalCorrectTaps.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()

    if (gamePhase == GamePhase.COMPLETED) {
        GameCompleteScreen(
            title = "You spotted it!",
            statText = "Found: $totalCorrectTaps · Mistakes: $mistakeCount",
            onNavigateHome = onNavigateHome
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Level $currentLevel of 6",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Rule Banner
        val ruleText = if (currentRuleType == SignalRuleType.COLOR) {
            "Find the ${targetColor?.name} ones!"
        } else {
            "Find the ${targetShape?.name}S!"
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ruleText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Live Scoreboard
        Text(
            text = "$targetsFound of $totalTargets found",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridCols),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(gridObjects) { obj ->
                SignalObjectView(
                    obj = obj,
                    onClick = { viewModel.onObjectTapped(obj.id) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SignalObjectView(obj: SignalObject, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (obj.isFound) 0.8f else 1.0f,
        animationSpec = tween(300),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (obj.isFound) 0.5f else 1.0f,
        label = "alpha"
    )

    val color = when (obj.color) {
        SignalColor.RED -> Color.Red
        SignalColor.BLUE -> Color.Blue
        SignalColor.GREEN -> Color.Green
        SignalColor.YELLOW -> Color.Yellow
        SignalColor.PURPLE -> Color(0xFF9C27B0)
        SignalColor.ORANGE -> Color(0xFFFF9800)
    }

    val drawColor = if (obj.showMistake) Color.LightGray else color
    val borderColor = if (obj.showMistake) Color.Red else Color.Transparent
    val borderWidth = if (obj.showMistake) 4.dp else 0.dp

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .alpha(alpha)
            .background(Color.Transparent)
            .clickable(enabled = !obj.isFound && !obj.showMistake) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            when (obj.shape) {
                SignalShape.CIRCLE -> {
                    drawCircle(color = drawColor, radius = w / 2f, center = Offset(cx, cy))
                    if (borderWidth > 0.dp) {
                        drawCircle(color = borderColor, radius = w / 2f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth.toPx()))
                    }
                }
                SignalShape.SQUARE -> {
                    val rectSize = w * 0.85f
                    val topLeft = Offset(cx - rectSize / 2f, cy - rectSize / 2f)
                    drawRect(color = drawColor, topLeft = topLeft, size = Size(rectSize, rectSize))
                    if (borderWidth > 0.dp) {
                        drawRect(color = borderColor, topLeft = topLeft, size = Size(rectSize, rectSize), style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth.toPx()))
                    }
                }
                SignalShape.TRIANGLE -> {
                    val path = Path().apply {
                        moveTo(cx, cy - h / 2f)
                        lineTo(cx + w / 2f, cy + h / 2f)
                        lineTo(cx - w / 2f, cy + h / 2f)
                        close()
                    }
                    drawPath(path = path, color = drawColor)
                    if (borderWidth > 0.dp) {
                        drawPath(path = path, color = borderColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth.toPx()))
                    }
                }
                SignalShape.DIAMOND -> {
                    val path = Path().apply {
                        moveTo(cx, cy - h / 2f)
                        lineTo(cx + w / 2f, cy)
                        lineTo(cx, cy + h / 2f)
                        lineTo(cx - w / 2f, cy)
                        close()
                    }
                    drawPath(path = path, color = drawColor)
                    if (borderWidth > 0.dp) {
                        drawPath(path = path, color = borderColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth.toPx()))
                    }
                }
                SignalShape.STAR -> {
                    val path = Path().apply {
                        val numPoints = 5
                        val outerRadius = w / 2f
                        val innerRadius = outerRadius * 0.4f
                        var angle = -Math.PI / 2.0
                        val angleStep = Math.PI / numPoints
                        for (i in 0 until numPoints * 2) {
                            val r = if (i % 2 == 0) outerRadius else innerRadius
                            val x = cx + (r * cos(angle)).toFloat()
                            val y = cy + (r * sin(angle)).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                            angle += angleStep
                        }
                        close()
                    }
                    drawPath(path = path, color = drawColor)
                    if (borderWidth > 0.dp) {
                        drawPath(path = path, color = borderColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth.toPx()))
                    }
                }
            }
        }

        if (obj.isFound) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
