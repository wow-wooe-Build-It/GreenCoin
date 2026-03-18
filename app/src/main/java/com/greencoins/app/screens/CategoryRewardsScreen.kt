package com.greencoins.app.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.Reward
import com.greencoins.app.data.ShopRepository
import com.greencoins.app.theme.AppColors
import com.greencoins.app.components.ImageWithFallback
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch

@Composable
fun CategoryRewardsScreen(
    categories: List<String>,
    selectedCategory: String,
    userBalance: Int = 0,
    onCategoryChange: (String) -> Unit,
    onRedeem: (Reward) -> Unit,
    onBack: () -> Unit,
) {
    var rewards by remember { mutableStateOf<List<Reward>>(emptyList()) }
    var redeemedIds by remember { mutableStateOf(setOf<String>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedCategory) {
        isLoading = true
        rewards = ShopRepository.getRewardsByCategory(selectedCategory)
        val userId = AuthRepository.currentUser?.id
        if (userId != null) {
            redeemedIds = ShopRepository.getRedeemedRewardIds(userId)
        }
        isLoading = false
    }
    
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.bg),
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 8.dp, end = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.white,
                )
            }
            Text(
                "Shop",
                color = AppColors.white,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Horizontal category selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) AppColors.accent else AppColors.border,
                            shape = RoundedCornerShape(9999.dp),
                        )
                        .clickable { onCategoryChange(category) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) AppColors.black else AppColors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Vertical list of rewards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isLoading) {
                 Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                     CircularProgressIndicator()
                 }
            } else {
                rewards.forEach { reward ->
                    val canRedeem = reward.id !in redeemedIds && userBalance >= reward.gcCost
                    RewardCard(
                        reward = reward,
                        isRedeemed = reward.id in redeemedIds,
                        canRedeem = canRedeem,
                        onRedeem = {
                            scope.launch {
                                // Call repository to redeem
                                val success = ShopRepository.redeemReward(
                                    userId = AuthRepository.currentUser?.id ?: "",
                                    rewardId = reward.id,
                                    cost = reward.gcCost
                                )
                                if (success) {
                                    redeemedIds = redeemedIds + reward.id
                                    onRedeem(reward)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardCard(
    reward: Reward,
    isRedeemed: Boolean,
    canRedeem: Boolean = true,
    onRedeem: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.border, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (reward.imageUrl != null) {
                    ImageWithFallback(
                        src = reward.imageUrl,
                        contentDescription = reward.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(
                        text = reward.title,
                        color = AppColors.white,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(AppColors.accent, RoundedCornerShape(4.dp)),
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "${reward.gcCost} GC",
                            color = AppColors.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            val (btnText, btnColor) = when {
                isRedeemed -> "Claimed" to AppColors.white
                !canRedeem -> "Insufficient GC" to AppColors.white
                else -> "Redeem" to AppColors.black
            }
            Box(
                modifier = Modifier
                    .background(
                        color = when {
                            isRedeemed -> AppColors.textSecondary
                            !canRedeem -> AppColors.textSecondary.copy(alpha = 0.6f)
                            else -> AppColors.accent
                        },
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clickable(enabled = canRedeem && !isRedeemed) { onRedeem() }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = btnText,
                    color = btnColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
