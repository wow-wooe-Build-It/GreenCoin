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
import com.greencoins.app.data.Transaction
import com.greencoins.app.data.TransactionRepository
import com.greencoins.app.theme.AppColors

private fun formatTransactionDate(createdAt: String?): String {
    if (createdAt == null) return "-"
    val datePart = createdAt.take(10)
    if (datePart.length < 10) return datePart
    val parts = datePart.split("-")
    if (parts.size != 3) return datePart
    val year = parts[0]
    val monthNum = parts[1].toIntOrNull() ?: return datePart
    val day = parts[2]
    val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = if (monthNum in 1..12) monthNames[monthNum] else monthNum.toString()
    return "$day $month $year"
}

private fun formatTransactionType(type: String): String = when (type.lowercase()) {
    "mission_reward", "mission" -> "Mission reward"
    "challenge_reward", "challenge" -> "Challenge reward"
    "redeem", "redemption" -> "Shop redemption"
    "donation" -> "Donation"
    else -> type.replaceFirstChar { it.uppercase() }
}

@Composable
fun RedemptionHistoryScreen(
    userId: String,
    onBack: () -> Unit,
) {
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    LaunchedEffect(userId) {
        transactions = TransactionRepository.getTransactions(userId)
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
            Text("Redemption History", color = AppColors.white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            if (transactions.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "No transactions yet. Complete missions or redeem rewards to see your history.",
                            color = AppColors.textSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
            } else {
                transactions.forEach { tx ->
                    val isCredit = tx.amount > 0
                    val amountStr = if (isCredit) "+${tx.amount}" else "${tx.amount}"
                    val desc = tx.description ?: formatTransactionType(tx.type)
                    val dateStr = formatTransactionDate(tx.createdAt)
                    val amountColor = if (isCredit) AppColors.accent else AppColors.redLogout

                    GlassCard(modifier = Modifier.padding(vertical = 6.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "$amountStr GC",
                                    color = amountColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(desc, color = AppColors.white, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(dateStr, color = AppColors.textSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
