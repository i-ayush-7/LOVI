package com.example.focusbuilder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun GameIntroScreen(
    instructionText: String = "Watch carefully!",
    onReady: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // Actual generated illustration
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.focusbuilder.R.drawable.illust_game_intro),
            contentDescription = "A friendly owl character waiting to start the game",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier.size(250.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = instructionText,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onReady,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Ready!", style = MaterialTheme.typography.labelLarge)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
