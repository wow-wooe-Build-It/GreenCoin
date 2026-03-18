package com.greencoins.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greencoins.app.theme.AppColors

/**
 * Preserved exactly: bg rgba(31,31,31,0.6), backdrop-blur, border rgba(255,255,255,0.05), rounded-[32px].
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) else Modifier
            )
            .background(AppColors.cardGlass, RoundedCornerShape(32.dp))
            .border(1.dp, AppColors.borderLight, RoundedCornerShape(32.dp)),
    ) {
        content()
    }
}
