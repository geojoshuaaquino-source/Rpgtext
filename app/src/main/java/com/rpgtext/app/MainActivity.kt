package com.rpgtext.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RpgApp()
        }
    }
}

@Composable
fun RpgApp() {
    MaterialTheme {
        val systemBars = WindowInsets.systemBars.asPaddingValues()
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = systemBars.calculateTopPadding(),
                    bottom = systemBars.calculateBottomPadding()
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            // Existing game UI should be rendered here.
        }
    }
}
