package com.greencoins.app.components

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.bg)
            .border(1.dp, AppColors.border),
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
                    .shadow(15.dp, CircleShape)
                    .background(AppColors.accent, CircleShape),
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
                color = AppColors.white,
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
                    .background(AppColors.border, RoundedCornerShape(9999.dp))
                    .border(1.dp, AppColors.gray333, RoundedCornerShape(9999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(AppColors.accent, CircleShape),
                ) {}
                Text(
                    text = "%,d".format(coins),
                    color = AppColors.accent,
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
                    tint = AppColors.textSecondary,
                )
            }
        }
    }
}
