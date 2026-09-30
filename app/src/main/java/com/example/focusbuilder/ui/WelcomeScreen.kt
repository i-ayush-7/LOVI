package com.example.focusbuilder.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    var childName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Lovi",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Small moments. Stronger tomorrows.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.focusbuilder.R.drawable.illust_welcome),
            contentDescription = "A friendly baby deer resting under a pastel tree",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedTextField(
            value = childName,
            onValueChange = { childName = it },
            label = { Text("Child's First Name", style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
            shape = CircleShape,
            textStyle = MaterialTheme.typography.titleLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                val prefs = context.getSharedPreferences("LoviPrefs", Context.MODE_PRIVATE)
                prefs.edit().putString("child_name", childName.trim()).apply()
                onGetStarted()
            },
            enabled = childName.isNotBlank(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Get Started", style = MaterialTheme.typography.labelLarge)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
