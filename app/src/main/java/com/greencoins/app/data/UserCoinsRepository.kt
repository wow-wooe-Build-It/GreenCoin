package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class UserCoinsRow(
    val id: String,
    val email: String? = null,
    @SerialName("coins") val coins: Int = 0,
)

object UserCoinsRepository {
    private val client = SupabaseManager.client

    /**
     * Fetch the current spendable coin balance for a user.
     */
    suspend fun getUserCoins(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val row = client.from("users").select(
                columns = Columns.list("id", "email", "coins")
            ) {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<UserCoinsRow>()

            row?.coins ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Update the user's coin balance to an absolute value.
     */
    suspend fun updateUserCoins(userId: String, newBalance: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("users").update(
                {
                    UserCoinsRow::coins setTo newBalance
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Convenience helper to apply a delta (positive or negative) to the user's balance.
     * Returns the new balance on success, or null on failure.
     */
    suspend fun applyDelta(userId: String, delta: Int): Int? = withContext(Dispatchers.IO) {
        try {
            val current = getUserCoins(userId)
            val updated = (current + delta).coerceAtLeast(0)
            val ok = updateUserCoins(userId, updated)
            if (ok) updated else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

