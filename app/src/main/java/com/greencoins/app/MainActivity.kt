package com.greencoins.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.greencoins.app.data.ThemePreferences
import com.greencoins.app.theme.GreenCoinsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle deep link for Supabase Auth
        com.greencoins.app.data.SupabaseManager.handleDeepLink(intent)

        setContent {
            val isDarkTheme by ThemePreferences.isDarkThemeState
            LaunchedEffect(Unit) {
                ThemePreferences.load(this@MainActivity)
            }
            GreenCoinsTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    GreenCoinsApp()
                }
            }
        }
    }
}
