package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class RewardCategoryRow(
    val name: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

object ShopRepository {
    private val client = SupabaseManager.client

    // Fetch all rewards
    suspend fun getRewards(): List<Reward> = withContext(Dispatchers.IO) {
        try {
            client.from("rewards").select().decodeList<Reward>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Get rewards by category
    suspend fun getRewardsByCategory(category: String): List<Reward> = withContext(Dispatchers.IO) {
        try {
            client.from("rewards").select {
                filter { eq("category", category) }
            }.decodeList<Reward>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Fetch categories from reward_categories table, fallback to distinct from rewards
    suspend fun getCategories(): List<String> = withContext(Dispatchers.IO) {
        try {
            val fromTable = try {
                client.from("reward_categories").select {
                    order(column = "sort_order", order = Order.ASCENDING)
                }.decodeList<RewardCategoryRow>().map { it.name }
            } catch (_: Exception) {
                emptyList()
            }
            if (fromTable.isNotEmpty()) return@withContext fromTable
            // Fallback: distinct categories from rewards
            val rewards = client.from("rewards").select(columns = io.github.jan.supabase.postgrest.query.Columns.list("category")).decodeList<Reward>()
            rewards.map { it.category }.distinct().sorted()
        } catch (e: Exception) {
            e.printStackTrace()
            listOf("Travel", "Eco Store", "Lifestyle", "Direct Donate")
        }
    }

    // Redeem a reward
    // NOTE: In production, this should be a Postgres Function to ensure atomic transaction (check balance -> deduct -> insert tx)
    // For MVP, we perform client-side updates but keep Supabase as the single source of truth.
    suspend fun redeemReward(userId: String, rewardId: String, cost: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Check balance (total_gc) >= cost
            val balance = UserRepository.getTotalGc(userId)
            if (balance < cost) return@withContext false

            // 2. Insert transaction
            val transaction = buildJsonObject {
                put("user_id", userId)
                put("amount", -cost) // Negative for spend
                put("description", "Redeemed reward")
                put("type", "redeem")
                put("related_reward_id", rewardId)
            }
            client.from("transactions").insert(transaction)

            // 3. Deduct total_gc
            val newBalance = UserRepository.updateTotalGcDelta(userId, -cost)
            newBalance != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Fetch reward IDs the user has already redeemed (from transactions). */
    suspend fun getRedeemedRewardIds(userId: String): Set<String> = withContext(Dispatchers.IO) {
        try {
            val rows = client.from("transactions").select(columns = io.github.jan.supabase.postgrest.query.Columns.list("related_reward_id")) {
                filter {
                    eq("user_id", userId)
                    eq("type", "redeem")
                }
            }.decodeList<RelatedRewardRow>()
            rows.mapNotNull { it.relatedRewardId }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }
}

@kotlinx.serialization.Serializable
private data class RelatedRewardRow(
    @kotlinx.serialization.SerialName("related_reward_id") val relatedRewardId: String? = null,
)
