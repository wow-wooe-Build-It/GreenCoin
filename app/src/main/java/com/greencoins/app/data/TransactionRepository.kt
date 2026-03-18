package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TransactionRepository {
    private val client = SupabaseManager.client

    /** Fetch transactions for the user, sorted by most recent first. */
    suspend fun getTransactions(userId: String): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            client.from("transactions").select {
                filter { eq("user_id", userId) }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<Transaction>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
