package com.greencoins.app.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.greencoins.app.theme.AppColors
import com.greencoins.app.ui.NavIcons

/**
 * Preserved exactly: logo, coins display, help button.
 */
@Composable
fun Header(
    coins: Int,
    onHelp: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outline),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.padding(start = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .then(if (!isSystemInDarkTheme()) Modifier.shadow(8.dp, CircleShape, ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else Modifier.shadow(15.dp, CircleShape))
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.greencoins.app.R.drawable.greencoin_logo_transparent),
                    contentDescription = "GreenCoins Logo",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "GreenCoins",
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
        Row(
            modifier = Modifier.padding(end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(9999.dp))
                    .border(1.dp, colorScheme.outline, RoundedCornerShape(9999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                ) {}
                Text(
                    text = "%,d".format(coins),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            IconButton(
                onClick = onHelp,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = com.greencoins.app.ui.NavIcons.Help,
                    contentDescription = "Help",
                    tint = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
