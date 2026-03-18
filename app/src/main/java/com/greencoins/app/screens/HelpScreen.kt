package com.greencoins.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.GlassCard
import com.greencoins.app.data.FaqRepository
import com.greencoins.app.theme.AppColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun HelpScreen(onClose: () -> Unit) {
    var faqItems by remember { mutableStateOf<List<com.greencoins.app.data.FaqItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        faqItems = FaqRepository.getFaqItems()
    }

    val displayItems = if (faqItems.isEmpty()) {
        listOf(
            com.greencoins.app.data.FaqItem("", "How are missions verified?", "We use a combination of AI image recognition, metadata validation (GPS/Timestamp), and community peer-review to ensure every action is genuine.", 1),
            com.greencoins.app.data.FaqItem("", "What can I buy with GreenCoins?", "GreenCoins can be redeemed for sustainable products, public transport passes, or converted into direct donations for certified eco-projects.", 2),
            com.greencoins.app.data.FaqItem("", "How do I level up?", "Earn XP by completing missions and challenges. Higher levels unlock exclusive high-reward missions and limited edition rewards.", 3),
        )
    } else {
        faqItems
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 80.dp, bottom = 96.dp, start = 24.dp, end = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textSecondary)
            }
            Text("Help & Support", color = AppColors.white, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
        displayItems.forEach { item ->
            GlassCard(modifier = Modifier.padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(item.question, color = AppColors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.answer, color = AppColors.textSecondary, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        GlassCard(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Need Help?", color = AppColors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "If you have questions about missions, rewards, or your GreenCoins balance, our team is here to help.",
                    color = AppColors.textSecondary,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("support@greencoins.app", color = AppColors.accent.copy(alpha = 0.9f), fontSize = 12.sp)
                Text("hello@greencoins.app", color = AppColors.accent.copy(alpha = 0.9f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("We usually respond within 24 hours.", color = AppColors.gray555, fontSize = 10.sp)
            }
        }
    }
}
