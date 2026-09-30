package com.example.focusbuilder.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusbuilder.R
import com.example.focusbuilder.viewmodel.*

@Composable
fun RuleSwitchSortScreen(
    onNavigateHome: () -> Unit,
    viewModel: RuleSwitchSortViewModel = viewModel()
) {
    val gamePhase by viewModel.gamePhase.collectAsState()
    val currentRule by viewModel.currentRule.collectAsState()
    val currentObject by viewModel.currentObject.collectAsState()
    val ruleCorrectStreak by viewModel.ruleCorrectStreak.collectAsState()
    val mistakeCount by viewModel.mistakeCount.collectAsState()
    val totalCorrect by viewModel.totalCorrect.collectAsState()
    val showNewRuleBanner by viewModel.showNewRuleBanner.collectAsState()
    val correctBinHint by viewModel.correctBinHint.collectAsState()
    val successfulBin by viewModel.successfulBin.collectAsState()
    
    val leftBinContents by viewModel.leftBinContents.collectAsState()
    val rightBinContents by viewModel.rightBinContents.collectAsState()
    val showBlockCompleteCelebration by viewModel.showBlockCompleteCelebration.collectAsState()

    if (gamePhase == GamePhase.COMPLETED) {
        GameCompleteScreen(
            title = "Great switching!",
            statText = "Correct Sorts: $totalCorrect · Mistakes: $mistakeCount",
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
        
        // Progress Indicator for current rule block (4 dots)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(14.dp)
                        .background(
                            color = if (i <= ruleCorrectStreak) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Rule Indicator
        Text(
            text = "Sort by: ${currentRule.name}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = showNewRuleBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NEW RULE!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Center Object
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentObject != null && !showNewRuleBanner && !showBlockCompleteCelebration) {
                SortObjectView(obj = currentObject!!)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bins
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SortBinView(
                bin = SortBin.LEFT,
                currentRule = currentRule,
                isHinted = correctBinHint == SortBin.LEFT,
                isSuccessful = successfulBin == SortBin.LEFT,
                isCelebrating = showBlockCompleteCelebration,
                contents = leftBinContents,
                onClick = { viewModel.onBinTapped(SortBin.LEFT) }
            )
            
            SortBinView(
                bin = SortBin.RIGHT,
                currentRule = currentRule,
                isHinted = correctBinHint == SortBin.RIGHT,
                isSuccessful = successfulBin == SortBin.RIGHT,
                isCelebrating = showBlockCompleteCelebration,
                contents = rightBinContents,
                onClick = { viewModel.onBinTapped(SortBin.RIGHT) }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun getBerryImageRes(obj: SortObject): Int {
    return when {
        obj.color == SortColor.RED && obj.shape == SortShape.CIRCLE -> R.drawable.illust_berry_red_round
        obj.color == SortColor.RED && obj.shape == SortShape.SQUARE -> R.drawable.illust_berry_red_star
        obj.color == SortColor.BLUE && obj.shape == SortShape.CIRCLE -> R.drawable.illust_berry_blue_round
        else -> R.drawable.illust_berry_blue_star
    }
}

@Composable
fun getBerryContentDescription(obj: SortObject): String {
    return when {
        obj.color == SortColor.RED && obj.shape == SortShape.CIRCLE -> "Red round berry"
        obj.color == SortColor.RED && obj.shape == SortShape.SQUARE -> "Red star berry"
        obj.color == SortColor.BLUE && obj.shape == SortShape.CIRCLE -> "Blue round berry"
        else -> "Blue star berry"
    }
}

@Composable
fun SortObjectView(obj: SortObject) {
    val infiniteTransition = rememberInfiniteTransition(label = "wiggle")
    val wiggleOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggleOffset"
    )
    
    Image(
        painter = painterResource(id = getBerryImageRes(obj)),
        contentDescription = getBerryContentDescription(obj),
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                translationY = wiggleOffset
                rotationZ = wiggleOffset / 2f
            }
    )
}

@Composable
fun SortBinView(
    bin: SortBin, 
    currentRule: SortRule, 
    isHinted: Boolean, 
    isSuccessful: Boolean,
    isCelebrating: Boolean,
    contents: List<SortObject>,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCelebrating) 1.25f else if (isSuccessful || isHinted) 1.1f else 1.0f,
        animationSpec = if (isCelebrating) spring(dampingRatio = 0.5f) else tween(200),
        label = "binScale"
    )
    
    val bgColor = if (isCelebrating) Color.Yellow.copy(alpha = 0.4f)
                  else if (isSuccessful) Color.Green.copy(alpha = 0.3f) 
                  else if (isHinted) Color.Green.copy(alpha = 0.6f) 
                  else MaterialTheme.colorScheme.surfaceVariant

    val borderColor = if (isHinted || isCelebrating) Color.Green else Color.Transparent

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .scale(scale)
            .background(bgColor, RoundedCornerShape(24.dp))
            .border(4.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.SpaceBetween, 
            modifier = Modifier.fillMaxHeight()
        ) {
            // Main Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (currentRule == SortRule.COLOR) {
                    val indicatorRes = if (bin == SortBin.LEFT) R.drawable.illust_berry_red_round else R.drawable.illust_berry_blue_round
                    Image(
                        painter = painterResource(id = indicatorRes),
                        contentDescription = "Color rule indicator",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val text = if (bin == SortBin.LEFT) "RED" else "BLUE"
                    Text(text, fontWeight = FontWeight.Bold)
                } else {
                    val indicatorRes = if (bin == SortBin.LEFT) R.drawable.illust_berry_red_round else R.drawable.illust_berry_red_star
                    Image(
                        painter = painterResource(id = indicatorRes),
                        contentDescription = "Shape rule indicator",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color.Gray),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val text = if (bin == SortBin.LEFT) "ROUND" else "STAR"
                    Text(text, fontWeight = FontWeight.Bold)
                }
            }
            
            // Contents cluster
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                contents.forEach { obj ->
                    Image(
                        painter = painterResource(id = getBerryImageRes(obj)),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(2.dp).size(20.dp)
                    )
                }
            }
        }
    }
}
