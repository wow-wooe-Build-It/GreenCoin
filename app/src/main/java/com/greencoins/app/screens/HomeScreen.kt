package com.greencoins.app.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.GlassCard
import com.greencoins.app.components.ImageWithFallback
import com.greencoins.app.data.Challenge
import com.greencoins.app.data.ChallengeDetailData
import com.greencoins.app.data.ChallengeDetailRepository
import com.greencoins.app.data.ChallengeRepository
import com.greencoins.app.data.Mission
import com.greencoins.app.data.MissionRepository
import androidx.compose.material3.MaterialTheme
import com.greencoins.app.theme.AppColors
import com.greencoins.app.theme.GreenCoinsTheme
import com.greencoins.app.theme.themeCardBgColor
import com.greencoins.app.theme.themeIconBgColor
import com.greencoins.app.theme.themeMutedBgColor
import com.greencoins.app.theme.themeOnSurfaceTextColor
import com.greencoins.app.theme.themeOnSurfaceVariantTextColor
import com.greencoins.app.ui.toImageVector

import com.greencoins.app.data.UserRepository
import com.greencoins.app.data.UserProfile
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.LevelData

@Composable
fun HomeScreen(
    onMissionSelect: (String) -> Unit,
    onChallengeClick: (ChallengeDetailData) -> Unit = {},
    onCommunityVerificationClick: () -> Unit = {},
    refreshHeader: () -> Unit = {}
) {
    var missions by remember { mutableStateOf<List<Mission>>(emptyList()) }
    var challenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var weeklyStreak by remember { mutableStateOf<List<Boolean>>(emptyList()) }
    var calculatedStreak by remember { mutableStateOf(0) }
    var levelsTable by remember { mutableStateOf<List<LevelData>>(emptyList()) }

    LaunchedEffect(Unit) {
        val user = AuthRepository.currentUser
        missions = MissionRepository.getMissions()
        challenges = ChallengeRepository.getChallenges()
        levelsTable = UserRepository.getLevels()
        if (user != null) {
            userProfile = UserRepository.getProfile(user.id)
            weeklyStreak = UserRepository.getWeeklyStreak(user.id)
            calculatedStreak = UserRepository.getCalculatedStreak(user.id)
            refreshHeader()
        }
    }

    val textColor = themeOnSurfaceTextColor()
    val textSecondaryColor = themeOnSurfaceVariantTextColor()
    val iconBg = themeIconBgColor()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 24.dp, bottom = 96.dp, start = 24.dp, end = 24.dp),
    ) {
        StreakProgressCard(
            streakDays = calculatedStreak,
            userLevel = userProfile?.level ?: 1,
            missionsCompleted = userProfile?.missionsCompleted ?: 0,
            weeklyProgress = if (weeklyStreak.size == 7) weeklyStreak else listOf(false, false, false, false, false, false, false),
            levelsTable = levelsTable
        )
        Spacer(modifier = Modifier.height(40.dp))
        // Daily Missions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Daily Missions",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "NEW REFRESH IN 4H",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            missions.take(2).forEach { m ->
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onMissionSelect(m.id) },
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(iconBg)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = m.icon.toImageVector(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(m.title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(m.description ?: "", color = textSecondaryColor, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("+${m.gcReward}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.size(4.dp))
                            Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            missions.drop(2).take(2).forEach { m ->
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onMissionSelect(m.id) },
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(iconBg)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = m.icon.toImageVector(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(m.title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(m.description ?: "", color = textSecondaryColor, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("+${m.gcReward}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.size(4.dp))
                            Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Featured Missions",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(24.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCommunityVerificationClick,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Community Verification",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                    Text(
                        "Verify eco actions in your area and earn GC",
                        color = textSecondaryColor,
                        fontSize = 10.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+5 GC", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.size(4.dp))
                        Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Active Challenges",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            challenges.forEach { c ->
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(160.dp)
                        .padding(4.dp)
                        .clickable { onChallengeClick(ChallengeDetailRepository.toDetail(c)) },
                ) {
                    val isPreview = LocalInspectionMode.current

                    if (isPreview) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(c.title, color = textColor)
                        }
                    } else {
                        ImageWithFallback(
                            src = c.coverImageUrl ?: "",
                            contentDescription = c.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black,
                                    )
                                )
                            ),
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(9999.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "WIN ${c.rewardGc} COINS",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(c.title, color = textColor, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onChallengeClick(ChallengeDetailRepository.toDetail(c)) },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.2f)),
                        ) {
                            Text("Join", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreakProgressCard(
    streakDays: Int = 0,
    userLevel: Int = 1,
    missionsCompleted: Int = 0,
    weeklyProgress: List<Boolean> = listOf(false, false, false, false, false, false, false),
    levelsTable: List<LevelData> = emptyList()
) {
    val cardBg = themeCardBgColor()
    val textColor = themeOnSurfaceTextColor()
    val textSecondaryColor = themeOnSurfaceVariantTextColor()
    val mutedBg = themeMutedBgColor()
    val iconBg = themeIconBgColor()
    val levelThresholds = mapOf(
        1 to 0,
        2 to 5,
        3 to 15,
        4 to 30,
        5 to 50,
        6 to 75,
        7 to 100,
        8 to 150,
        9 to 250,
        10 to 500
    )
    val levelTitles = mapOf(
        1 to "Seed",
        2 to "Sprout",
        3 to "Eco Explorer",
        4 to "Green Guardian",
        5 to "Earth Champion",
        6 to "Nature Defender",
        7 to "Planet Savior",
        8 to "Climate Hero",
        9 to "Eco Legend",
        10 to "Gaia's Avatar"
    )
    
    // Find effective level based on actual missionsCompleted
    var effectiveLevel = 1
    var effectiveTitle = "Seed"
    var currentTierStart = 0
    var nextTierTarget = 10
    
    if (levelsTable.isNotEmpty()) {
        for (i in levelsTable.indices) {
            val lvl = levelsTable[i]
            if (missionsCompleted >= lvl.missionsRequired) {
                effectiveLevel = lvl.levelNumber
                effectiveTitle = lvl.title
                currentTierStart = lvl.missionsRequired
                if (i + 1 < levelsTable.size) {
                    nextTierTarget = levelsTable[i + 1].missionsRequired
                } else {
                    nextTierTarget = lvl.missionsRequired // Max level
                }
            } else {
                break
            }
        }
    } else {
        for (i in levelData.indices) {
            if (missionsCompleted >= levelData[i].first) {
                effectiveLevel = i + 1
                effectiveTitle = levelData[i].second
                currentTierStart = levelData[i].first
                if (i + 1 < levelData.size) {
                    nextTierTarget = levelData[i + 1].first
                } else {
                    nextTierTarget = levelData[i].first // Max level
                }
            } else {
                break
            }
        }
    }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    // Calendar: Sunday=1, Monday=2, ... Saturday=7. Map to Mon=0..Sun=6.
    val currentDayIndex = ((java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7)

    var effectiveLevel = 1
    for (i in 1..10) {
        if (missionsCompleted >= (levelThresholds[i] ?: 0)) {
            effectiveLevel = i
        } else {
            break
        }
    }

    val nextLevel = (effectiveLevel + 1).coerceAtMost(10)
    val missionsRequiredForNextLevel = levelThresholds[nextLevel] ?: 500
    val currentLevelThreshold = levelThresholds[effectiveLevel] ?: 0
    
    val progressRange = (missionsRequiredForNextLevel - currentLevelThreshold).coerceAtLeast(1)
    val currentProgress = (missionsCompleted - currentLevelThreshold).coerceAtLeast(0)
    
    val rawProgress = if (effectiveLevel >= 10) 1f else (currentProgress / progressRange.toFloat())
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "missionsProgress",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(24.dp), clip = false)
            .background(cardBg, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                center = androidx.compose.ui.geometry.Offset.Zero,
                                radius = 80f,
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = "Streak",
                        tint = AppColors.black,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = "STREAK",
                        color = textSecondaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "$streakDays DAYS",
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.Whatshot,
                contentDescription = "Activity",
                tint = textSecondaryColor,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dayLabels.forEachIndexed { index, label ->
                val daysAgo = currentDayIndex - index
                val isCompleted = if (daysAgo in 0..6) {
                    weeklyProgress.getOrNull(6 - daysAgo) == true
                } else {
                    false
                }
                val isCurrent = index == currentDayIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    when {
                        isCompleted -> {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Completed",
                                    tint = AppColors.black,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }

                        isCurrent -> {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.Transparent, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                    ),
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(mutedBg, CircleShape),
                            )
                        }
                    }

                    Text(
                        text = label,
                        color = textSecondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "LEVEL $effectiveLevel",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = levelTitles[effectiveLevel] ?: "Eco Hero",
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = if (effectiveLevel >= 10) "$missionsCompleted total missions" else "$missionsCompleted / $missionsRequiredForNextLevel missions",
                color = textSecondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(iconBg, RoundedCornerShape(9999.dp)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                    .height(10.dp)
                    .shadow(16.dp, RoundedCornerShape(9999.dp), clip = false)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            ),
                        ),
                        RoundedCornerShape(9999.dp),
                    ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Home Screen")
@Composable
private fun HomeScreenPreview() {
    GreenCoinsTheme {
        HomeScreen(onMissionSelect = {})
    }
}
