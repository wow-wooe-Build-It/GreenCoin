package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to handle querying the unified user_coupons collection, including 
 * nested joins for the campaigns and the actual inventory codes.
 */
object MyRewardsRepository {

    suspend fun getMyCoupons(): List<UserCoupon> = withContext(Dispatchers.IO) {
        try {
            val user = AuthRepository.currentUser ?: return@withContext emptyList()
            
            // Join coupon_campaigns and coupon_inventory implicitly to build the rich UI model
            val columns = Columns.raw("""
                *,
                campaign:coupon_campaigns(*),
                inventory:coupon_inventory(*)
            """.trimIndent())
            
            SupabaseManager.client.from("user_coupons")
                .select(columns = columns) {
                    filter {
                        eq("user_id", user.id)
                    }
                    order("assigned_at", Order.DESCENDING)
                }
                .decodeList<UserCoupon>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun scratchCoupon(userCouponId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = AuthRepository.currentUser ?: return@withContext false
            val nowIso = java.time.Instant.now().toString()
            
            SupabaseManager.client.from("user_coupons")
                .update(
                    mapOf(
                        "status" to "scratched",
                        "scratched_at" to nowIso
                    )
                ) {
                    filter {
                        eq("id", userCouponId)
                        eq("user_id", user.id)
                        eq("status", "locked")
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
