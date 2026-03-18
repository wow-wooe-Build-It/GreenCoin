package com.greencoins.app.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.UserRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.GlassCard
import com.greencoins.app.components.ImageWithFallback
import com.greencoins.app.screens.PersonalInformationScreen
import com.greencoins.app.theme.AppColors
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    showPersonalInfo: Boolean = false,
    onShowPersonalInfoChange: (Boolean) -> Unit = {},
    showImpactStatistics: Boolean = false,
    onShowImpactStatisticsChange: (Boolean) -> Unit = {},
    showRedemptionHistory: Boolean = false,
    onShowRedemptionHistoryChange: (Boolean) -> Unit = {},
) {
    var user by remember { mutableStateOf<io.github.jan.supabase.auth.user.UserInfo?>(null) }
    var userProfile by remember { mutableStateOf<com.greencoins.app.data.UserProfile?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.use { it.readBytes() }
                    if (bytes != null && user != null) {
                        val url = UserRepository.uploadAvatar(user!!.id, bytes)
                        UserRepository.updateProfile(user!!.id, avatarUrl = url)
                        userProfile = UserRepository.getProfile(user!!.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        user = AuthRepository.currentUser
        user?.id?.let { userId ->
            userProfile = UserRepository.getProfile(userId)
        }
    }

    BackHandler(enabled = showImpactStatistics || showRedemptionHistory || showPersonalInfo) {
        onShowImpactStatisticsChange(false)
        onShowRedemptionHistoryChange(false)
        onShowPersonalInfoChange(false)
    }

    if (showImpactStatistics && user != null) {
        ImpactStatisticsScreen(userId = user!!.id, onBack = { onShowImpactStatisticsChange(false) })
        return
    }
    if (showRedemptionHistory && user != null) {
        RedemptionHistoryScreen(userId = user!!.id, onBack = { onShowRedemptionHistoryChange(false) })
        return
    }
    if (showPersonalInfo && user != null) {
        PersonalInformationScreen(
            userId = user!!.id,
            userProfile = userProfile,
            emailFromAuth = user?.email,
            phoneFromAuth = user?.userMetadata?.get("phone")?.toString()?.replace("\"", ""),
            onBack = { onShowPersonalInfoChange(false) },
            onProfileUpdated = {
                scope.launch {
                    user?.id?.let { userId ->
                        userProfile = UserRepository.getProfile(userId)
                    }
                    onShowPersonalInfoChange(false)
                }
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = 96.dp, start = 24.dp, end = 24.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .padding(4.dp)
                        .background(AppColors.accent, RoundedCornerShape(48.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                ) {
                    ImageWithFallback(
                        src = userProfile?.avatarUrl ?: "https://www.pngkey.com/png/full/115-1150152_default-profile-picture-avatar-png-green.png",
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .background(AppColors.accent, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            tint = AppColors.black,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .offset(y = (-8).dp)
                        .background(AppColors.accent, RoundedCornerShape(9999.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text("LVL ${userProfile?.level ?: 1}", color = AppColors.black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                val displayName = userProfile?.fullName?.takeIf { it.isNotBlank() }
                    ?: user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
                    ?: "GreenCoin User"
                Text(displayName, color = AppColors.white, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(user?.email ?: "", color = AppColors.textSecondary, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Current Streak", color = AppColors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${userProfile?.streakCount ?: 0}", color = AppColors.white, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("DAYS", color = AppColors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Missions Completed", color = AppColors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${userProfile?.missionsCompleted ?: 0}", color = AppColors.white, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("TOTAL", color = AppColors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        listOf(
            Triple(Icons.Default.Person, "Personal Information", { onShowPersonalInfoChange(true) }),
            Triple(Icons.Default.Star, "Impact Statistics", { onShowImpactStatisticsChange(true) }),
            Triple(Icons.Default.ShoppingBag, "Redemption History", { onShowRedemptionHistoryChange(true) }),
        ).forEach { (icon, label, onClickAction) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(AppColors.border, RoundedCornerShape(24.dp))
                    .clickable { onClickAction() }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(label, color = AppColors.white, fontWeight = FontWeight.Medium)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.gray555, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLogout,
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                "Logout Session",
                color = AppColors.redLogout,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
