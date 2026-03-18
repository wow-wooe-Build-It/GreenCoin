package com.greencoins.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.GlassCard
import com.greencoins.app.components.ImageWithFallback
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.ChallengeDetailData
import com.greencoins.app.data.ChallengeDetailRepository
import com.greencoins.app.data.LeaderboardEntry
import com.greencoins.app.data.LeaderboardRepository
import com.greencoins.app.theme.AppColors

@Composable
fun ChallengeDetailScreen(
    data: ChallengeDetailData,
    onBack: () -> Unit,
    isJoined: Boolean = false,
    onJoin: () -> Unit = {},
) {
    var showJoinDialog by remember { mutableStateOf(false) }

    var baseLeaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    androidx.compose.runtime.LaunchedEffect(data.id) {
        baseLeaderboard = LeaderboardRepository.getChallengeLeaderboard(
            challengeId = data.id,
            currentUserId = AuthRepository.currentUser?.id,
        )
    }
    val userName = remember {
        AuthRepository.currentUser?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
            ?: AuthRepository.currentUser?.email?.split("@")?.firstOrNull()?.replaceFirstChar { it.uppercase() }
            ?: "Current User"
    }
    val leaderboard = if (isJoined) {
        val alreadyInList = baseLeaderboard.any { it.isCurrentUser }
        if (alreadyInList) {
            baseLeaderboard
        } else {
            baseLeaderboard.map { it.copy(isCurrentUser = false) } +
                LeaderboardEntry(
                    rank = baseLeaderboard.size + 1,
                    username = userName,
                    coins = 0,
                    isCurrentUser = true,
                )
        }
    } else {
        baseLeaderboard
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = {
                showJoinDialog = false
                onJoin()
            },
            title = { Text("Success", color = AppColors.white, fontWeight = FontWeight.Bold) },
            text = { Text("You have successfully joined the challenge", color = AppColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showJoinDialog = false
                    onJoin()
                }) {
                    Text("OK", color = AppColors.accent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = AppColors.border,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            ImageWithFallback(
                src = data.img,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                AppColors.bg
                            )
                        )
                    )
            )
            
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(AppColors.bg.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.white)
                }
                IconButton(
                    onClick = { /* Share */ },
                    modifier = Modifier
                        .background(AppColors.bg.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = AppColors.white)
                }
            }

            // Title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(AppColors.accent, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ACTIVE", color = AppColors.black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    data.title,
                    color = AppColors.white,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .padding(24.dp)
                .padding(bottom = 96.dp),
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Reward", "500 GC")
                StatItem("Participants", "1.2k")
                StatItem("Days Left", "12")
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Text("How to Participate", color = AppColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            data.instructions.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(AppColors.gray333, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = AppColors.white, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(step, color = AppColors.textSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Leaderboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Top Agents", color = AppColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("View All", color = AppColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            leaderboard.forEach { entry ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .height(64.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "#${entry.rank}", 
                            color = if (entry.rank <= 3) AppColors.accent else AppColors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(if (entry.isCurrentUser) AppColors.accent else AppColors.gray333, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(entry.username.take(1), color = if (entry.isCurrentUser) AppColors.black else AppColors.white, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            entry.username, 
                            color = AppColors.white, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AppColors.pendingYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${entry.coins}", color = AppColors.white, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (!isJoined) {
                        showJoinDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    contentColor = AppColors.black,
                    disabledContainerColor = AppColors.border,
                    disabledContentColor = AppColors.textSecondary,
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isJoined,
            ) {
                Text(
                    text = if (isJoined) "Joined" else "Join Challenge",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = AppColors.white, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = AppColors.textSecondary, fontSize = 12.sp)
    }
}
