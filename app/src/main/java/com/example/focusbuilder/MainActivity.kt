package com.example.focusbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.focusbuilder.navigation.AppNavigation
import com.example.focusbuilder.ui.theme.FocusBuilderTheme
import com.example.focusbuilder.util.SoundManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize centralized sound pool
        SoundManager.init(applicationContext)
        
        // Initialize RevenueCat
        com.example.focusbuilder.billing.RevenueCatManager.init(applicationContext)
        
        setContent {
            FocusBuilderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}
