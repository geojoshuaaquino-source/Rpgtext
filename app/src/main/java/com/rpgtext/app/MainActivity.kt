package com.rpgtext.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RpgApp() }
    }
}

@Composable
fun RpgApp() {
    // Temporary safe root while restoring the full UI from the known-good commit.
    // System bars are intentionally not consumed here; individual top-level surfaces should own their insets.
    Surface(modifier = Modifier.fillMaxSize()) {}
}
