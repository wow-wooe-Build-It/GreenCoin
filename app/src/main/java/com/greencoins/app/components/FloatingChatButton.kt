package com.greencoins.app.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.greencoins.app.theme.AppColors

@Composable
fun FloatingChatButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .then(if (!isSystemInDarkTheme()) Modifier.shadow(8.dp, CircleShape, ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else Modifier.shadow(8.dp, CircleShape))
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = "Chat with GreenBot",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp),
        )
    }
}
