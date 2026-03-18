package com.greencoins.app.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.greencoins.app.data.ShopRepository
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.ImageWithFallback

import com.greencoins.app.data.Reward
import com.greencoins.app.theme.AppColors
import com.greencoins.app.theme.GreenCoinsTheme

@Composable
fun ShopScreen(
    categories: List<String>,
    onCategoryClick: (String) -> Unit,
) {
    var popularRewards by remember { mutableStateOf<List<Reward>>(emptyList()) }

    LaunchedEffect(Unit) {
        popularRewards = ShopRepository.getRewards()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp, bottom = 96.dp, start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            FeaturedRewardCard(onRedeemClick = { onCategoryClick("Travel") })
        }
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(24.dp))
        }
        items(categories) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category) },
            )
        }
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(32.dp))
        }
        item(span = { GridItemSpan(2) }) {
            Text("Popular Rewards", color = AppColors.white, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(popularRewards) { reward ->
            PopularRewardCard(
                reward = reward,
                onClick = { onCategoryClick(reward.category) },
            )
        }
    }
}

private fun mapRewardToCategory(reward: Reward): String = when (reward.category) {
    "Metro Pass" -> "Travel"
    "Eco Store" -> "Eco Store"
    "Lifestyle" -> "Lifestyle"
    else -> "Travel"
}

@Composable
private fun FeaturedRewardCard(onRedeemClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.border, RoundedCornerShape(32.dp)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(AppColors.accent.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Text("FEATURED REWARD", color = AppColors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Travel Saver Pass", color = AppColors.white, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Redeem your coins to unlock\nup to ₹100 off on eco-friendly travel.",
                color = AppColors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 80.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRedeemClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = AppColors.black),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("REDEEM NOW", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PopularRewardCard(
    reward: Reward,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(4f / 5f)
                .fillMaxWidth()
                .background(AppColors.border, RoundedCornerShape(28.dp)),
        ) {
            ImageWithFallback(
                src = reward.imageUrl ?: "",
                contentDescription = reward.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            reward.discountLabel?.let { discount ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(AppColors.accent, RoundedCornerShape(9999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(discount, color = AppColors.black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(reward.title, color = AppColors.white, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(AppColors.accent, RoundedCornerShape(6.dp)))
            Spacer(modifier = Modifier.size(6.dp))
            Text("${reward.gcCost}", color = AppColors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryCard(
    category: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(AppColors.border, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AppColors.accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = AppColors.accent,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category,
                color = AppColors.white,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Preview(showBackground = true, name = "Shop Screen")
@Composable
private fun ShopScreenPreview() {
    GreenCoinsTheme {
        ShopScreen(
            categories = listOf("Travel", "Eco Store", "Lifestyle"),
            onCategoryClick = {},
        )
    }
}
