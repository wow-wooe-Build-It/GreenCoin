package com.greencoins.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.greencoins.app.theme.AppColors

/**
 * Preserved exactly for dark: bg rgba(31,31,31,0.6), backdrop-blur, border rgba(255,255,255,0.05), rounded-[32px].
 * Light theme: white background, 6.dp elevation.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLightTheme = colorScheme.surface.luminance() > 0.5f
    val cardColor = if (isLightTheme) colorScheme.surfaceContainer else AppColors.cardGlass
    val borderColor = if (isLightTheme) colorScheme.outlineVariant.copy(alpha = 0.5f) else AppColors.borderLight
    val elevation = if (isLightTheme) 6.dp else 0.dp

    Box(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) else Modifier
            )
            .then(if (isLightTheme) Modifier.shadow(elevation, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.08f)) else Modifier)
            .background(cardColor, RoundedCornerShape(32.dp))
            .border(1.dp, borderColor, RoundedCornerShape(32.dp)),
    ) {
        content()
    }
}
