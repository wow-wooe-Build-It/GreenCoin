package com.greencoins.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.greencoins.app.data.Screen
import com.greencoins.app.theme.AppColors

private data class NavItem(
    val id: Screen,
    val icon: ImageVector,
    val isAction: Boolean = false,
)

/**
 * Preserved exactly: home, shop, plus (action), challenges, profile; active state indicator.
 */
@Composable
fun BottomNav(
    active: Screen,
    onChange: (Screen) -> Unit,
) {
    val navItems = listOf(
        NavItem(Screen.Home, Icons.Default.Home),
        NavItem(Screen.Shop, Icons.Default.ShoppingBag),
        NavItem(Screen.Plus, Icons.Default.Add, isAction = true),
        NavItem(Screen.Challenges, Icons.Default.EmojiEvents),
        NavItem(Screen.Profile, Icons.Default.Person),
    )
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(AppColors.bg)
                .border(1.dp, AppColors.border),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navItems.forEach { item ->
                if (item.isAction) {
                    Spacer(modifier = Modifier.size(56.dp))
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onChange(item.id) },
                            )
                            .padding(8.dp),
                    ) {
                        Box {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (active == item.id) AppColors.accent else AppColors.textSecondary,
                            )
                            if (active == item.id) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset(y = 4.dp)
                                        .size(4.dp)
                                        .background(AppColors.accent, CircleShape),
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // FAB (Center +)
        IconButton(
            onClick = { onChange(Screen.Plus) },
            modifier = Modifier
               .align(Alignment.Center)
                .offset(y = (-12).dp)
                .size(56.dp)
                .background(AppColors.accent, CircleShape),
        ) {
             Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = AppColors.black,
            )
        }
    }
}
