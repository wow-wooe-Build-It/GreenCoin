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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.MaterialTheme
import com.greencoins.app.theme.AppColors
import com.greencoins.app.components.ImageWithFallback
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.widget.Toast
import com.greencoins.app.RazorpayController
import com.razorpay.Checkout
import org.json.JSONObject
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

    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        // Horizontal category selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) colorScheme.primary else colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(9999.dp),
                        )
                        .clickable { onCategoryChange(category) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vertical list of rewards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isLoading) {
                 Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                     CircularProgressIndicator()
                 }
            } else {
                val context = LocalContext.current
                val activity = context as? Activity
                rewards.forEach { reward ->
                    val actualGcCost = if (reward.gcPrice > 0) reward.gcPrice else reward.gcCost
                    val canRedeem = reward.id !in redeemedIds && userBalance >= actualGcCost
                    RewardCard(
                        reward = reward,
                        isRedeemed = reward.id in redeemedIds,
                        canRedeem = canRedeem,
                        onRedeem = {
                            if (reward.actualCashPricePaise > 0) {
                                // Hybrid flow
                                isLoading = true
                                scope.launch {
                                    val orderInfo = ShopRepository.createHybridOrder(reward.id)
                                    if (orderInfo != null && orderInfo.razorpayOrderId != null) {
                                        RazorpayController.currentAppOrderId = orderInfo.appOrderId
                                        RazorpayController.paymentCallback = { appOrderId, paymentId, rzOrderId, signature, error -> 
                                            if (error == null && paymentId != null && signature != null && rzOrderId != null) {
                                                scope.launch {
                                                    val success = ShopRepository.verifyHybridPayment(
                                                        appOrderId, paymentId, rzOrderId, signature
                                                    )
                                                    if (success) {
                                                        redeemedIds = redeemedIds + reward.id
                                                        onRedeem(reward)
                                                    } else {
                                                        Toast.makeText(context, "Payment verification failed server-side.", Toast.LENGTH_LONG).show()
                                                    }
                                                    isLoading = false
                                                }
                                            } else {
                                                isLoading = false
                                                Toast.makeText(context, "Payment Failed: $error", Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        // Launch razorpay
                                        val checkout = Checkout()
                                        checkout.setKeyID(orderInfo.keyId)
                                        try {
                                            val options = JSONObject()
                                            options.put("name", "GreenCoins")
                                            options.put("description", orderInfo.rewardTitle ?: reward.title)
                                            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
                                            options.put("currency", orderInfo.currency)
                                            options.put("order_id", orderInfo.razorpayOrderId)
                                            options.put("amount", orderInfo.amount)

                                            checkout.open(activity, options)
                                        } catch (e: Exception) {
                                            isLoading = false
                                            e.printStackTrace()
                                            Toast.makeText(context, "Failed to open Checkout", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, orderInfo?.message ?: "Failed to create order on server.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                // Pure GC Flow uses the same Edge Function but it auto-completes
                                isLoading = true
                                scope.launch {
                                    val orderInfo = ShopRepository.createHybridOrder(reward.id)
                                    isLoading = false
                                    if (orderInfo?.success == true) {
                                        redeemedIds = redeemedIds + reward.id
                                        onRedeem(reward)
                                    } else {
                                        Toast.makeText(context, orderInfo?.message ?: "Failed to redeem reward.", Toast.LENGTH_LONG).show()
                                    }
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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceContainer, RoundedCornerShape(24.dp))
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
                        color = colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val actualGcCost = if (reward.gcPrice > 0) reward.gcPrice else reward.gcCost
                        if (reward.actualCashPricePaise > 0) {
                            Text(
                                text = "Rs ${reward.actualCashPricePaise / 100} + ",
                                color = colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "$actualGcCost GC",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            val (btnText, btnColor) = when {
                isRedeemed -> "Claimed" to colorScheme.onSurface
                !canRedeem -> "Insufficient GC" to colorScheme.onSurface
                else -> "Redeem" to colorScheme.onPrimary
            }
            Box(
                modifier = Modifier
                    .background(
                        color = when {
                            isRedeemed -> AppColors.textSecondary
                            !canRedeem -> AppColors.textSecondary.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.primary
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
