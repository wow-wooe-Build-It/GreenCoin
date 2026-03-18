package com.greencoins.app.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greencoins.app.components.GlassCard
import com.greencoins.app.theme.AppColors

private data class FaqItem(val question: String, val answer: String)

private val FAQ_ITEMS = listOf(
    FaqItem(
        "How do I earn GreenCoins?",
        "Users can earn GreenCoins by completing eco-friendly missions and participating in challenges.",
    ),
    FaqItem(
        "How do missions work?",
        "Select a mission, complete the environmental action, upload proof, and earn GC rewards.",
    ),
    FaqItem(
        "How do I redeem rewards?",
        "Go to the Shop section and redeem your GreenCoins for available rewards.",
    ),
)

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

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
            Text("Help & Support", color = AppColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text("FAQ", color = AppColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            FAQ_ITEMS.forEachIndexed { index, item ->
                val isExpanded = expandedIndex == index
                GlassCard(
                    modifier = Modifier.padding(vertical = 6.dp),
                    onClick = { expandedIndex = if (isExpanded) null else index },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                item.question,
                                color = AppColors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = AppColors.textSecondary,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                item.answer,
                                color = AppColors.textSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Contact Support", color = AppColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "support@greencoins.app",
                        color = AppColors.accent,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@greencoins.app")
                                }
                                context.startActivity(Intent.createChooser(intent, "Send email"))
                            },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "team@greencoins.app",
                        color = AppColors.accent,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:team@greencoins.app")
                                }
                                context.startActivity(Intent.createChooser(intent, "Send email"))
                            },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("About GreenCoins", color = AppColors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "GreenCoins is a platform that rewards users for taking environmentally positive actions such as recycling, cleanups, tree planting, and sustainable lifestyle choices.",
                        color = AppColors.textSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
