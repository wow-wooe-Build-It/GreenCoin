package com.greencoins.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Light theme: surfaceContainer. Dark theme: AppColors.border */
@Composable
fun themeCardBgColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
        MaterialTheme.colorScheme.surfaceContainer
    else AppColors.border

/** Light theme: onSurface. Dark theme: AppColors.white */
@Composable
fun themeOnSurfaceTextColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
        MaterialTheme.colorScheme.onSurface
    else AppColors.white

/** Light theme: onSurfaceVariant. Dark theme: AppColors.textSecondary */
@Composable
fun themeOnSurfaceVariantTextColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
        MaterialTheme.colorScheme.onSurfaceVariant
    else AppColors.textSecondary

/** Light theme: surface. Dark theme: AppColors.bg */
@Composable
fun themePageBgColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
        MaterialTheme.colorScheme.surface
    else AppColors.bg

/** Light theme: outlineVariant. Dark theme: AppColors.gray333 */
@Composable
fun themeMutedBgColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    else AppColors.gray333

/** Light theme: outlineVariant. Dark theme: AppColors.border (for icon boxes etc) */
@Composable
fun themeIconBgColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    else AppColors.border

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = AppColors.black,
    background = AppColors.bg,
    onBackground = AppColors.white,
    surface = AppColors.bg,
    onSurface = AppColors.white,
    surfaceContainer = AppColors.border,
    surfaceContainerHighest = AppColors.gray333,
    outline = AppColors.border,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00C853),
    onPrimary = AppColors.black,
    background = Color(0xFFF7F9F7),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFF7F9F7),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF5C5C5C),
    surfaceContainer = Color.White,
    surfaceContainerHighest = Color(0xFFE8EAE8),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFE8EAE8),
)

@Composable
fun GreenCoinsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
