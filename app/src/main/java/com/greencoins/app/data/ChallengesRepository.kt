package com.greencoins.app.data

import android.util.Log
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChallengesRepository {
    private const val TAG = "ChallengesRepository"

    private val client = SupabaseManager.client

    suspend fun getChallenges(): List<Challenge> = withContext(Dispatchers.IO) {
        try {
            client.from("challenges").select().decodeList<Challenge>()
        } catch (e: Exception) {
            Log.e(TAG, "getChallenges failed", e)
            emptyList()
        }
    }
}
