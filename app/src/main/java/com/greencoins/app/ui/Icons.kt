package com.greencoins.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.greencoins.app.data.MissionIcon

fun MissionIcon.toImageVector(): ImageVector = when (this) {
    MissionIcon.TreePine -> Icons.Default.Star   // Nature-like; Material has Park/Nature
    MissionIcon.Recycle -> Icons.Default.Eco     // Recycle -> Eco
    MissionIcon.Leaf -> Icons.Default.Eco
    MissionIcon.Users -> Icons.Default.People
    MissionIcon.Trash2 -> Icons.Default.Delete
    MissionIcon.Zap -> Icons.Default.Star        // Bolt in extended
}

// Use material-icons-extended for Bolt if needed
object NavIcons {
    val Home = Icons.Default.Home
    val ShoppingBag = Icons.Default.ShoppingBag
    val Plus = Icons.Default.Add
    val Trophy = Icons.Default.EmojiEvents
    val User = Icons.Default.Person
    val Help = Icons.Default.Help
    val ArrowRight = Icons.Default.ArrowForward
    val Camera = Icons.Default.CameraAlt
    val MapPin = Icons.Default.Place
    val CheckCircle = Icons.Default.CheckCircle
    val ChevronRight = Icons.Default.ChevronRight
    val Search = Icons.Default.Search
}
