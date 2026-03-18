package com.greencoins.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = AppColors.black,
    surface = AppColors.bg,
    onSurface = AppColors.white,
    outline = AppColors.border,
)

@Composable
fun GreenCoinsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
