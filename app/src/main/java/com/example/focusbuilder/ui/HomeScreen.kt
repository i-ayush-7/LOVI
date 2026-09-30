package com.example.focusbuilder.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWaitForIt: () -> Unit = {},
    onNavigateToSequenceRepeat: () -> Unit = {},
    onNavigateToWhatsMissing: () -> Unit = {},
    onNavigateToRuleSwitchSort: () -> Unit = {},
    onNavigateToFindSignal: () -> Unit = {},
    onNavigateToWatchFirefly: () -> Unit = {},
    onNavigateToParentDashboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("LoviPrefs", Context.MODE_PRIVATE)
    val childName = prefs.getString("child_name", null)
    val greeting = if (childName.isNullOrBlank()) "Hi there!" else "Hi $childName!"

    Scaffold(
        bottomBar = {
            FloatingBottomBar()
        },
        containerColor = Color(0xFFFDFBF7) // Soft cream background matching reference
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = com.example.focusbuilder.R.drawable.lovi_logo),
                    contentDescription = "Lovi Logo",
                    modifier = Modifier.height(64.dp).padding(start = 4.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = onNavigateToParentDashboard,
                    modifier = Modifier.background(Color(0xFFEFEFEF), CircleShape)
                ) {
                    Text("\uD83D\uDC64", style = MaterialTheme.typography.titleMedium)
                }
            }

            // 2. Greeting
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A2F)
            )
            Text(
                text = "Ready to grow your focus today?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1E3A2F).copy(alpha = 0.7f)
            )

            // 3. Hero Card
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = com.example.focusbuilder.R.drawable.hero_banner),
                        contentDescription = "Hero Banner",
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.3f)
                            .offset(x = (-60).dp, y = (-20).dp),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.55f)
                            .align(Alignment.CenterEnd)
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCCFDFBF7), Color(0xFFFDFBF7))
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = CircleShape,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("\u2600\uFE0F", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.width(4.dp))
                                Text("Today's Focus", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Text(
                            text = "A calm mind for a brighter tomorrow",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1E3A2F),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Button(
                            onClick = onNavigateToWaitForIt,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CB895)),
                            shape = CircleShape
                        ) {
                            Text("Start", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("\u2192", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // 4. Skill Badges Row
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkillBadge(com.example.focusbuilder.R.drawable.icon_stay, "Stay")
                SkillBadge(com.example.focusbuilder.R.drawable.icon_notice, "Notice")
                SkillBadge(com.example.focusbuilder.R.drawable.icon_remember, "Remember")
                SkillBadge(com.example.focusbuilder.R.drawable.icon_wait, "Wait")
                SkillBadge(com.example.focusbuilder.R.drawable.icon_switch, "Switch")
            }

            // 5. Your Journey Section
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Journey",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A2F)
                )
                Text(
                    text = "Today's activities >",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 32.dp), // Extra padding for bottom bar
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                JourneyGameCard(1, "Sequence Repeat", com.example.focusbuilder.R.drawable.illust_frog, onNavigateToSequenceRepeat)
                JourneyGameCard(2, "Find the Signal", com.example.focusbuilder.R.drawable.illust_deer, onNavigateToFindSignal)
                JourneyGameCard(3, "Wait For It", com.example.focusbuilder.R.drawable.illust_lantern_go, onNavigateToWaitForIt)
                JourneyGameCard(4, "Watch the Firefly", com.example.focusbuilder.R.drawable.firefly_base, onNavigateToWatchFirefly)
                JourneyGameCard(5, "What's Missing", com.example.focusbuilder.R.drawable.illust_fox, onNavigateToWhatsMissing)
                JourneyGameCard(6, "Rule Switch Sort", com.example.focusbuilder.R.drawable.illust_basket, onNavigateToRuleSwitchSort)
            }
        }
    }
}

@Composable
fun FloatingBottomBar() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(72.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(iconStr = "\uD83C\uDFE0", label = "Today", selected = true, onClick = {})
            BottomNavItem(
                iconStr = "\uD83C\uDF0D",
                label = "World",
                selected = false,
                onClick = { Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show() }
            )
            BottomNavItem(
                iconStr = "\uD83E\uDDED",
                label = "Explore",
                selected = false,
                onClick = { Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show() }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    iconStr: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF1E3A2F) else Color.Gray
    val bgColor = if (selected) Color(0xFFE4F3E8) else Color.Transparent
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(bgColor)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(iconStr, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 2.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SkillBadge(iconRes: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun JourneyGameCard(number: Int, title: String, imageRes: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(110.dp)) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFE6F0EE)), // Soft fallback color matching palette
                contentScale = ContentScale.Crop
            )
            // Number Badge
            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .size(26.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5CB895) // Thematic green
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A2F),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
