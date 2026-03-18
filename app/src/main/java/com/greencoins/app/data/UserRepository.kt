package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserRepository {
    private val client = SupabaseManager.client

    /** Fetch total_gc for header display (lifetime earned). */
    suspend fun getTotalGc(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val profile = client.from("users").select() {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<UserProfile>()
            (profile?.totalGc ?: 0).coerceAtLeast(0)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /** Reset streak to 0 when last_mission_date is before yesterday (user missed a day). Call before fetching profile. */
    suspend fun checkAndResetStreak(userId: String) = withContext(Dispatchers.IO) {
        try {
            client.postgrest.rpc("check_and_reset_streak", mapOf("p_user_id" to userId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fetch the detailed profile from 'public.users' table
    suspend fun getProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        try {
            checkAndResetStreak(userId)
            client.from("users").select() {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Apply delta to total_gc (e.g. -cost on redeem). Returns new value or null on failure. */
    suspend fun updateTotalGcDelta(userId: String, delta: Int): Int? = withContext(Dispatchers.IO) {
        try {
            val current = getTotalGc(userId)
            val updated = (current + delta).coerceAtLeast(0)
            client.from("users").update(
                { UserProfile::totalGc setTo updated }
            ) { filter { eq("id", userId) } }
            updated
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fetch weekly streak boolean array via RPC
    suspend fun getWeeklyStreak(userId: String): List<Boolean> = withContext(Dispatchers.IO) {
        try {
            val result = client.postgrest.rpc("get_user_streak", mapOf("p_user_id" to userId))
            result.decodeAs<List<Boolean>>()
        } catch (e: Exception) {
            e.printStackTrace()
            listOf(false, false, false, false, false, false, false)
        }
    }

    /** Upload avatar image to Supabase Storage, returns public URL. */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "$userId/${System.currentTimeMillis()}.jpg"
        val bucket = client.storage.from("avatar")
        bucket.upload(fileName, imageBytes) { upsert = true }
        bucket.publicUrl(fileName)
    }

    /** Update user profile fields (only non-null values are updated). */
    suspend fun updateProfile(userId: String, fullName: String? = null, phone: String? = null, city: String? = null, avatarUrl: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            fullName?.let { client.from("users").update({ UserProfile::fullName setTo it }) { filter { eq("id", userId) } } }
            phone?.let { client.from("users").update({ UserProfile::phone setTo it }) { filter { eq("id", userId) } } }
            city?.let { client.from("users").update({ UserProfile::city setTo it }) { filter { eq("id", userId) } } }
            avatarUrl?.let { client.from("users").update({ UserProfile::avatarUrl setTo it }) { filter { eq("id", userId) } } }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
