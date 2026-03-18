package com.greencoins.app.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.greencoins.app.data.ChallengeRepository
import com.greencoins.app.data.Challenge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.GlassCard
import com.greencoins.app.components.ImageWithFallback
import com.greencoins.app.theme.AppColors
import com.greencoins.app.ui.toImageVector

import com.greencoins.app.data.ChallengeDetailData
import com.greencoins.app.data.ChallengeDetailRepository

@Composable
fun ChallengesScreen(onChallengeClick: (ChallengeDetailData) -> Unit = {}) {
    var challenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }

    LaunchedEffect(Unit) {
        challenges = ChallengeRepository.getAllChallenges()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = 96.dp, start = 24.dp, end = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Challenges", color = AppColors.white, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("My Active", color = AppColors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        val activeList = challenges.filter { it.isActive }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            activeList.take(2).forEach { c ->
                val progress = 0 // Future: Calculate actual progress
                GlassCard(
                    modifier = Modifier.width(300.dp).clickable { onChallengeClick(ChallengeDetailRepository.toDetail(c)) }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(AppColors.border, RoundedCornerShape(16.dp)),
                            ) {
                                ImageWithFallback(
                                    src = c.coverImageUrl ?: "",
                                    contentDescription = c.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.title, color = AppColors.white, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${c.endDate?.take(10) ?: "Active"} LEFT", color = AppColors.accent, fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("PROGRESS", color = AppColors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$progress%", color = AppColors.white, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(AppColors.gray333, RoundedCornerShape(3.dp)),
                            color = AppColors.accent,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text("Featured Missions", color = AppColors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        activeList.drop(2).take(2).forEach { c ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(224.dp)
                    .padding(vertical = 12.dp)
                    .background(AppColors.border, RoundedCornerShape(32.dp))
                    .clickable { onChallengeClick(ChallengeDetailRepository.toDetail(c)) },
            ) {
                ImageWithFallback(
                    src = c.coverImageUrl ?: "",
                    contentDescription = c.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color.Transparent,
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f),
                                    androidx.compose.ui.graphics.Color.Black,
                                )
                            )
                        ),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .background(AppColors.accent, RoundedCornerShape(9999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text("${c.rewardGc} GC", color = AppColors.black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(c.title, color = AppColors.white, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("0 agents joined", color = AppColors.white.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                    androidx.compose.material3.Button(
                        onClick = { onChallengeClick(ChallengeDetailRepository.toDetail(c)) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AppColors.white, contentColor = AppColors.black),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Join", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text("Global Network", color = AppColors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        activeList.drop(4).take(3).forEach { c ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(AppColors.border, RoundedCornerShape(24.dp))
                    .clickable { onChallengeClick(ChallengeDetailRepository.toDetail(c)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AppColors.bg)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            com.greencoins.app.data.MissionIcon.Leaf.toImageVector(),
                            contentDescription = null,
                            tint = AppColors.accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Column {
                        Text(c.title, color = AppColors.white, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Global • ${c.description?.take(20) ?: ""}", color = AppColors.textSecondary, fontSize = 10.sp)
                    }
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AppColors.gray555, modifier = Modifier.size(16.dp))
            }
        }
    }
}
