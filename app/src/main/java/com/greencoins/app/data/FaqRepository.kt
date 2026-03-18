package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

object FaqRepository {
    private val client = SupabaseManager.client

    suspend fun getFaqItems(): List<FaqItem> = withContext(Dispatchers.IO) {
        try {
            client.from("faq").select {
                order(column = "sort_order", order = Order.ASCENDING)
            }.decodeList<FaqItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
