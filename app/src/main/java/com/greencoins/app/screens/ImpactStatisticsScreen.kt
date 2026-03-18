package com.greencoins.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.GlassCard
import com.greencoins.app.components.ImageWithFallback
import com.greencoins.app.data.CompletedChallengeItem
import com.greencoins.app.data.CompletedMissionItem
import com.greencoins.app.data.ImpactRepository
import com.greencoins.app.theme.AppColors

@Composable
fun ImpactStatisticsScreen(
    userId: String,
    onBack: () -> Unit,
) {
    var missions by remember { mutableStateOf<List<CompletedMissionItem>>(emptyList()) }
    var challenges by remember { mutableStateOf<List<CompletedChallengeItem>>(emptyList()) }

    LaunchedEffect(userId) {
        missions = ImpactRepository.getCompletedMissions(userId)
        challenges = ImpactRepository.getCompletedChallenges(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Impact Statistics", color = AppColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text("Completed Missions", color = AppColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            if (missions.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(20.dp)) {
                        Text("No completed missions yet.", color = AppColors.textSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                missions.forEach { item ->
                    GlassCard(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.imageUrl != null) {
                                    ImageWithFallback(
                                        src = item.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp, 48.dp),
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                }
                                Column {
                                    Text(item.missionTitle, color = AppColors.white, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        item.dateCompleted?.take(10) ?: "-",
                                        color = AppColors.textSecondary,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                            Text("+${item.gcEarned} GC", color = AppColors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Completed Challenges", color = AppColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            if (challenges.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(20.dp)) {
                        Text("No challenges joined yet.", color = AppColors.textSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                challenges.forEach { item ->
                    GlassCard(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(item.challengeName, color = AppColors.white, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    item.joinedDate?.take(10) ?: "Participated",
                                    color = AppColors.textSecondary,
                                    fontSize = 11.sp,
                                )
                            }
                            Text("+${item.gcReward} GC", color = AppColors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
